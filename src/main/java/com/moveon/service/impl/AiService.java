package com.moveon.service.impl;

import com.moveon.dto.EventDTO;
import com.moveon.dto.EventSearchDTO;
import com.moveon.service.IAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gemini 로 글에서 행사 정보를 뽑는다.
 *
 * 모델은 flash-lite 를 쓴다. 무료 한도가 가장 넉넉해서다(하루 1,000건).
 * 우리는 관리자가 대회 하나 등록할 때 한 번 부르므로 이걸로 충분하다.
 *
 * 주소는 v1beta 의 generateContent 를 쓴다.
 *   문서 첫 쪽에 나오는 /v1beta/interactions 는 권한이 없어 403 이 났고,
 *   gemini-2.5-* 모델은 "신규 사용자에게 더 이상 제공되지 않는다" 며 3.x 로 안내한다.
 */
@Slf4j
@Service
public class AiService implements IAiService {

    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
            + "gemini-3.5-flash-lite:generateContent";

    /** 대회 홈페이지 한 곳에서 읽어 보낼 글 길이. 안내는 앞쪽에 몰려 있어 이 정도면 넉넉하다 */
    private static final int MAX_TEXT = 8000;

    /** 검색 결과를 나눠 보낼 때 한 묶음 크기. 제목·요약을 다 합치면 8만 자가 넘는다 */
    private static final int CHUNK = 14000;

    private static final String PROMPT = """
            아래는 마라톤·러닝 대회 홈페이지에서 태그를 걷어낸 글이다.
            '%s' 대회의 정보만 뽑아 JSON 하나로 답하라.

            {
              "startDate":  "행사 시작일 YYYY-MM-DD",
              "endDate":    "행사 종료일 YYYY-MM-DD. 하루짜리면 시작일과 같게",
              "applyStart": "접수 시작일 YYYY-MM-DD",
              "applyEnd":   "접수 마감일 YYYY-MM-DD. '선착순 마감' 처럼 날짜가 없으면 빈 문자열",
              "placeName":  "출발 장소 이름. 예) 여의도 한강공원 물빛광장",
              "distances":  "종목 거리를 쉼표로. 예) 5km,10km,하프",
              "feeText":    "참가비를 적힌 그대로. 예) 하프 80,000원 / 10km 70,000원",
              "target":     "참가 자격이나 대상. 제한이 없으면 빈 문자열",
              "contact":    "문의 전화나 이메일",
              "eventType":  "마라톤 / 걷기 / 자전거 중 하나"
            }

            지켜야 할 것
            - 글에 없는 값은 반드시 빈 문자열 "" 로 두어라. 지어내지 마라.
            - 참가비는 '참가비' 라고 적힌 것만 써라.
              기념품 값, 환불 수수료, 후원 금액은 참가비가 아니다.
            - 날짜도 마찬가지다. 환불일자나 결과 발표일을 행사일로 쓰지 마라.
            - 지난 회차 날짜가 함께 적혀 있으면 앞으로 열리는 회차를 골라라.
            - 설명이나 코드블록 없이 JSON 만 답하라.

            글:
            %s
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;

    public AiService(@Value("${gemini.api.key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create();
    }

    @Override
    public boolean isReady() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    @SuppressWarnings("unchecked")
    public EventDTO extractEvent(String pageText, String eventName) {

        EventDTO rDTO = new EventDTO();

        if (!isReady() || pageText == null || pageText.isBlank()) {
            return rDTO;
        }

        try {
            String text = pageText.length() > MAX_TEXT
                    ? pageText.substring(0, MAX_TEXT) : pageText;

            String json = ask(PROMPT.formatted(eventName, text));
            if (json == null) {
                return rDTO;
            }

            Map<String, Object> v = objectMapper.readValue(json, Map.class);

            rDTO.setStartDate(date(v.get("startDate")));
            rDTO.setEndDate(date(v.get("endDate")));
            rDTO.setApplyStart(date(v.get("applyStart")));
            rDTO.setApplyEnd(date(v.get("applyEnd")));

            // 지난 회차 날짜를 가져오는 일이 실제로 있다.
            // 대회 사이트에 작년 기록이 함께 남아 있고, 프롬프트로 막아도 새어 나온다.
            // 행사일이 오늘보다 앞이면 잘못 읽은 것으로 보고 날짜를 통째로 버린다.
            // 사람이 직접 넣게 두는 편이, 작년 날짜가 그대로 저장되는 것보다 낫다.
            // 접수 시작과 마감이 같은 날로 오면 마감을 비운다.
            // '2026년 9월 1일 10:00 ~ 선착순마감' 처럼 마감 날짜가 없는 대회에서
            // 시작일을 마감일 자리에도 넣어 버리는 일이 있다.
            // 비워 두면 화면에 '사이트에서 확인' 으로 나오므로 그편이 안전하다.
            if (rDTO.getApplyEnd() != null && rDTO.getApplyEnd().equals(rDTO.getApplyStart())) {
                log.info("접수 시작·마감이 같아 마감을 비운다 : {}", rDTO.getApplyEnd());
                rDTO.setApplyEnd(null);
            }

            if (rDTO.getStartDate() != null && rDTO.getStartDate().isBefore(LocalDate.now())) {
                log.warn("지난 회차 날짜로 보여 버린다 : {}", rDTO.getStartDate());
                rDTO.setStartDate(null);
                rDTO.setEndDate(null);
                rDTO.setApplyStart(null);
                rDTO.setApplyEnd(null);
            }
            rDTO.setPlaceName(str(v.get("placeName")));
            rDTO.setDistances(str(v.get("distances")));
            rDTO.setFeeText(str(v.get("feeText")));
            rDTO.setTarget(str(v.get("target")));
            rDTO.setContact(str(v.get("contact")));
            rDTO.setEventType(str(v.get("eventType")));

            log.info("Gemini 추출 : {} / {} / {}",
                    rDTO.getStartDate(), rDTO.getPlaceName(), rDTO.getFeeText());

        } catch (Exception e) {
            // 한도를 넘겼거나 응답이 이상해도 관리자 화면은 열려야 한다.
            // 규칙으로 뽑은 값이 이미 있고, 없으면 사람이 넣으면 된다.
            log.warn("Gemini 호출 실패 : {}", e.getMessage());
        }

        return rDTO;
    }


    private static final String NAME_PROMPT = """
            아래는 러닝·마라톤 대회를 다룬 글 제목들이다.
            여기서 %d년에 앞으로 열릴 대회를 뽑아 아래 모양의 JSON 배열로 답하라.

            [{"name": "대회 정식 이름", "region": "열리는 지역"}]

            지켜야 할 것
            - region 은 **대회가 열리는 곳**을 적어라. 주최사 이름이 아니다.
              '아식스 서울신문 고프리런' 은 서울신문이 주최하지만 여의도에서 열리므로 "서울" 이다.
              이름에 지역이 없어도 글 내용을 보고 판단하라.
              예) "2026 인사이더런" -> 글에 잠실이라고 적혀 있으면 "서울"
              정말 알 수 없으면 "" 로 두어라.
            - 대회의 정식 이름을 그대로 적어라. 제목 일부를 잘라 붙이지 마라.
            - 같은 대회는 반드시 하나로 합쳐라. 아래는 모두 같은 대회다.
              "잠수교 나이트런" = "잠수교 K10 나이트 런" = "2025 서울 잠수교 나이트런"
              "OO 10km대회" = "OO 10km마라톤" = "OO 10K 마라톤대회"
              뒤에 붙는 대회/마라톤/레이스 같은 말이 달라도 앞부분이 같으면 같은 대회다.
            - 대회가 아닌 것은 빼라. 맛집 글, 후기, 종목 이름, 문장 조각은 대회가 아니다.
            - **이미 열린 대회는 빼라.** 오늘은 %s 다.
              글이 지난 대회의 후기나 결과를 다루고 있으면 넣지 마라.
            - 설명 없이 배열만 답하라.

            제목:
            %s
            """;

    @Override
    @SuppressWarnings("unchecked")
    public List<EventSearchDTO> extractNames(List<String> titles) {

        List<EventSearchDTO> rList = new ArrayList<>();

        if (!isReady() || titles == null || titles.isEmpty()) {
            return rList;
        }

        // 제목과 요약을 다 이어 붙이면 8만 자가 넘는다.
        // 한 번에 다 보낼 수 없어 나눠서 여러 번 묻는다.
        //
        // 처음에는 앞 8,000자만 잘라 보냈다.
        // 그랬더니 검색어 여섯 개 중 첫 번째 것도 다 못 들어가서,
        // 뒤에 있던 경기도 검색 결과는 아예 도달하지 못했다.
        // 그래서 경기 대회가 한 건도 안 나왔다.
        List<String> chunks = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String t : titles) {
            if (sb.length() + t.length() > CHUNK) {
                chunks.add(sb.toString());
                sb = new StringBuilder();
            }
            sb.append("- ").append(t).append("\n");
        }
        if (sb.length() > 0) {
            chunks.add(sb.toString());
        }

        // 묶음도 한꺼번에 묻는다. 하나씩 기다리면 묶음마다 2초씩 쌓인다.
        // flash-lite 는 분당 15번까지라 서너 개를 같이 보내도 여유가 있다.
        int year = LocalDate.now().getYear();
        List<String> answers = chunks.parallelStream()
                .map(chunk -> ask(NAME_PROMPT.formatted(year, LocalDate.now(), chunk)))
                .filter(java.util.Objects::nonNull)
                .toList();

        for (String json : answers) {
            try {
                for (Object o : objectMapper.readValue(json, List.class)) {
                    Map<String, Object> v = (Map<String, Object>) o;
                    String name = str(v.get("name"));
                    if (name == null || name.length() < 4 || name.length() > 40) {
                        continue;
                    }
                    boolean dup = rList.stream().anyMatch(x -> name.equals(x.getName()));
                    if (dup) {
                        continue;
                    }
                    EventSearchDTO dto = new EventSearchDTO();
                    dto.setName(name);
                    dto.setRegion(str(v.get("region")));
                    rList.add(dto);
                }
            } catch (Exception e) {
                log.warn("대회 이름 읽기 실패 : {}", e.getMessage());
            }
        }

        log.info("Gemini 이름 추출 : 제목 {}개 -> 대회 {}개", titles.size(), rList.size());

        // 합치기는 여기서 하지 않는다.
        // 지역으로 먼저 걸러 목록을 줄인 뒤에 합쳐야 결과가 안정적이다.
        // 109개를 한 번에 합치라고 하면 부를 때마다 102개, 53개로 들쭉날쭉해진다.
        return rList;
    }


    private static final String MERGE_PROMPT = """
            아래는 여러 번에 나눠 뽑은 대회 이름 목록이다. 같은 대회가 여러 번 들어 있다.
            중복을 합쳐 최종 목록을 JSON 배열로 답하라.

            합치는 기준
            - 같은 대회면 하나로. 아래는 모두 같은 대회다.
              "2026 잠수교 10K 나이트런" = "잠수교 10k 마라톤대회" = "서울 한강 나이트런"
              "OO 하프마라톤" = "OO 하프 마라톤대회" = "제3회 OO 하프마라톤"
            - 합칠 때는 가장 온전한 이름을 남겨라.
              회차와 연도가 붙은 정식 이름이 좋다.

            빼야 할 것
            - 대회가 아닌 것. "상품상세 - OO", "요즘 러닝" 같은 글 제목 조각
            - %d년이 아닌 대회. 오늘은 %s 다
            - 이미 열린 대회

            설명 없이 [{"name":"이름","region":"지역"}] 형태의 배열만 답하라.

            목록:
            %s
            """;

    @Override
    @SuppressWarnings("unchecked")
    public List<EventSearchDTO> mergeNames(List<EventSearchDTO> names) {

        if (!isReady() || names.size() < 2) {
            return names;
        }

        try {
            StringBuilder sb = new StringBuilder();
            for (EventSearchDTO n : names) {
                sb.append("- ").append(n.getName())
                  .append(" (지역: ").append(n.getRegion()).append(")\n");
            }

            String json = ask(MERGE_PROMPT.formatted(
                    LocalDate.now().getYear(), LocalDate.now(), sb));
            if (json == null) {
                return names;
            }

            List<EventSearchDTO> merged = new ArrayList<>();
            for (Object o : objectMapper.readValue(json, List.class)) {
                Map<String, Object> v = (Map<String, Object>) o;
                String name = str(v.get("name"));
                if (name == null || name.length() < 4 || name.length() > 40) {
                    continue;
                }
                if (merged.stream().anyMatch(x -> name.equals(x.getName()))) {
                    continue;
                }
                EventSearchDTO dto = new EventSearchDTO();
                dto.setName(name);
                dto.setRegion(str(v.get("region")));
                merged.add(dto);
            }

            if (merged.isEmpty()) {
                return names;
            }

            log.info("이름 합치기 : {}개 -> {}개", names.size(), merged.size());
            return merged;

        } catch (Exception e) {
            log.warn("이름 합치기 실패 : {}", e.getMessage());
            return names;
        }
    }

    private static final String SITE_PROMPT = """
            '%s' 대회의 공식 홈페이지를 아래 검색 결과에서 하나 고르라.

            참가하려는 사람을 어디로 보내야 할지 고르는 일이다. 아래 차례로 본다.

            1. OFFICIAL  그 대회 전용 홈페이지.
                         대회 이름을 단 도메인이고 대회요강·코스안내 쪽이 함께 있다.
            2. APPLY     대회 전용 홈페이지는 없지만 실제로 접수를 받는 곳.
                         브랜드가 여는 행사는 전용 사이트 없이 접수 플랫폼만 쓰는 경우가 많다.
                         주최사가 올린 공지·이벤트 페이지도 여기에 든다.
            3. INFO      위 둘이 없을 때만. 대회 정보를 정리해 둔 곳.

            아래는 어느 쪽으로도 고르지 마라.
            - 뉴스 기사
            - 입찰 공고, 용역업체 선정, 채용 같은 행정 문서
            - 개인 블로그 후기, 커뮤니티 글, 지식iN
            - 이 대회가 아닌 다른 대회의 페이지
            - 대회와 상관없는 회사·기관의 일반 홈페이지

            쓸 만한 것이 하나도 없으면 kind 를 "NONE" 으로, url 을 빈 문자열로 답하라.
            잘못된 주소는 아무것도 없는 것보다 나쁘다. 억지로 고르지 마라.

            {"url":"고른 주소","kind":"OFFICIAL 또는 APPLY 또는 INFO 또는 NONE"} 형태의 JSON 하나만 답하라.

            검색 결과:
            %s
            """;

    @Override
    @SuppressWarnings("unchecked")
    public String pickSite(String eventName, List<Map<String, String>> candidates) {

        if (!isReady() || candidates == null || candidates.isEmpty()) {
            return null;
        }

        try {
            StringBuilder sb = new StringBuilder();
            for (Map<String, String> c : candidates) {
                sb.append("- 제목: ").append(c.get("title"))
                  .append("\n  주소: ").append(c.get("link")).append("\n");
            }

            String json = ask(SITE_PROMPT.formatted(eventName, sb));
            if (json == null) {
                return null;
            }

            Map<String, Object> v = objectMapper.readValue(json, Map.class);
            String url = str(v.get("url"));
            String kind = str(v.get("kind"));

            log.info("Gemini 링크 : {} -> [{}] {}", eventName, kind, url);

            if (url == null || !url.startsWith("http") || "NONE".equals(kind)) {
                return null;
            }
            // 종류를 앞에 붙여 돌려준다. 관리자에게 무엇인지 알려 줘야 하기 때문이다.
            return (kind == null ? "INFO" : kind) + "|" + url;

        } catch (Exception e) {
            log.warn("공식 사이트 고르기 실패 : {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gemini 에 물어 본문 글자만 돌려준다. 실패하면 null.
     *
     * 503(과부하)이 제법 자주 온다. 무료 등급이라 그렇다.
     * 한 번 실패했다고 규칙으로 돌아가면 결과가 크게 나빠지므로 두 번까지 다시 부른다.
     */
    private String ask(String prompt) {
        for (int i = 0; i < 3; i++) {
            String res = askOnce(prompt);
            if (res != null) {
                return res;
            }
            if (i < 2) {
                try {
                    // 잠깐 쉬었다 다시. 과부하는 대개 곧 풀린다.
                    Thread.sleep(1200L * (i + 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String askOnce(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    // temperature 0 : 같은 물음에 같은 답을 하게 한다.
                    // 기본값이면 부를 때마다 뽑히는 대회 수가 달라진다.
                    // temperature 0 : 같은 물음에 같은 답을 하게 한다.
                    // 기본값이면 부를 때마다 뽑히는 대회 수가 달라진다.
                    // maxOutputTokens : 목록이 길면 응답이 중간에 잘려 결과가 들쭉날쭉해진다.
                    "generationConfig", Map.of("responseMimeType", "application/json",
                                               "temperature", 0.0,
                                               "maxOutputTokens", 8192));

            String res = restClient.post()
                    .uri(URI.create(URL))
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (res == null || res.isBlank()) {
                return null;
            }

            Map<String, Object> root = objectMapper.readValue(res, Map.class);
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) root.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return null;
            }
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return String.valueOf(parts.get(0).get("text")).trim()
                    .replaceAll("^```(?:json)?", "").replaceAll("```$", "").trim();

        } catch (Exception e) {
            log.warn("Gemini 호출 실패 : {}", e.getMessage());
            return null;
        }
    }

    /** "" 나 "없음" 같은 값은 안 채운 것으로 본다 */
    private String str(Object o) {
        if (o == null) {
            return null;
        }
        String s = o.toString().trim();
        if (s.isEmpty() || "없음".equals(s) || "null".equalsIgnoreCase(s)) {
            return null;
        }
        return s;
    }

    private LocalDate date(Object o) {
        String s = str(o);
        if (s == null) {
            return null;
        }
        try {
            return LocalDate.parse(s.substring(0, 10));
        } catch (Exception e) {
            // 모델이 "2026년 8월" 처럼 날짜가 아닌 값을 줄 때가 있다. 그때는 비워 둔다.
            return null;
        }
    }

}
