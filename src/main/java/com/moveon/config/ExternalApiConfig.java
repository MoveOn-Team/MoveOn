package com.moveon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 밖에 나가는 호출을 한군데서 정한다. (네이버 · 카카오 · Gemini · 대회 홈페이지)
 *
 * RestClient.create() 는 타임아웃이 없다. 응답 없는 대회 사이트 하나가
 * 톰캣 스레드를 무기한 잡는다.
 */
@Configuration
public class ExternalApiConfig {

    /** 네이버 · 카카오 · 대회 홈페이지 */
    @Bean
    public RestClient externalRestClient() {
        return RestClient.builder()
                .requestFactory(factory(Duration.ofSeconds(3), Duration.ofSeconds(8)))
                .build();
    }

    /** Gemini 전용. 제목 100개를 읽고 답하느라 20초가 걸리는 일이 있어 읽기를 길게 준다 */
    @Bean
    public RestClient aiRestClient() {
        return RestClient.builder()
                .requestFactory(factory(Duration.ofSeconds(3), Duration.ofSeconds(30)))
                .build();
    }

    /**
     * 검색을 나눠 부를 때 쓴다.
     *
     * parallelStream() 의 common ForkJoinPool 은 병렬도가 (코어수 - 1) 이라
     * 네이버 18번이 3개씩 나뉘어 돌았다. 남의 서버를 기다리는 일이라 넉넉히 둔다.
     */
    @Bean
    public ExecutorService searchExecutor() {
        AtomicInteger seq = new AtomicInteger();
        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "event-search-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
        return Executors.newFixedThreadPool(12, tf);
    }

    /** JDK HttpClient 는 기본이 '리다이렉트 안 따라감' 이라 본문 대신 빈 302 를 읽는다 */
    private JdkClientHttpRequestFactory factory(Duration connect, Duration read) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(connect)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build());
        factory.setReadTimeout(read);
        return factory;
    }
}
