package com.moveon.service.impl;

import tools.jackson.databind.ObjectMapper;
import com.moveon.dto.EventDTO;
import com.moveon.dto.EventSearchDTO;
import com.moveon.mapper.IAdminMapper;
import com.moveon.service.IAiService;
import com.moveon.service.IEventSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;


/**
 * 행사를 찾는 일.
 * 네이버 검색과 카카오 좌표를 사용함.
 *
 * 검색은 세 가지를 나눠 쓴다.
 *   blog / news  어떤 대회가 있는지 알아낸다
 *   webkr        대회 이름으로 공식 홈페이지를 찾는다
 */
@Slf4j
@Service
public class EventSearchService implements IEventSearchService {

    private static final String NAVER = "https://naverapihub.apigw.ntruss.com/search/v1/";
    private static final String KAKAO = "https://dapi.kakao.com/v2/local/search/keyword.json";

    /** 관리자가 검색어를 안 넣었을 때 훑을 기본 검색어 */
    private static final String[] DEFAULT_QUERIES = {
            "서울 마라톤 대회 접수", "서울 러닝 대회 참가 신청",
            "서울 걷기대회 참가", "서울 나이트런 참가",
            "경기도 마라톤 대회 접수", "경기 러닝 대회 참가 신청"
    };

    /**
     * 대회 공식 사이트가 아닌 곳. 여기 걸리면 후보에서 뺀다.
     *
     * 모음 사이트가 더 위험하다. 어느 대회로 검색해도 걸린다.
     */
    private static final String[] NOT_OFFICIAL = {
            // 블로그 · 커뮤니티 · 뉴스
            "blog.naver.com", "tistory.com", "namu.wiki", "v.daum.net", "ezday.co.kr",
            "cafe.naver.com", "post.naver.com", "brunch.co.kr", "youtube.com",
            "instagram.com", "dcinside.com", "instiz.net", "pann.nate.com",
            "news.naver.com", "sports.naver.com", "in.naver.com", "band.us",
            "facebook.com", "cashwalk.com", "news2day.co.kr", "newsis.com",
            "yna.co.kr", "hankyung.com", "mk.co.kr", "chosun.com", "donga.com",
            // 대회 모음 사이트
            "kormarathon.com", "marathongo.co.kr", "runningwikii.com", "runable.me",
            "rankingmarathon.com", "gorunning.kr", "runningon.co.kr", "kimrunning.com",
            "roadrun.co.kr", "emarathon.or.kr", "runko.kr", "runit.co.kr",
            "mara1080.com", "sfinder.co.kr"};

    /** 찾은 결과를 잠깐 담아 두는 곳. 검색어별로 하나씩 */
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    /** 보관 시간. 이보다 오래되면 버리고 새로 찾는다 */
    private static final long CACHE_MILLIS = 10 * 60 * 1000L;

    private record Cached(long at, List<EventSearchDTO> list) {
        boolean fresh() {
            return System.currentTimeMillis() - at < CACHE_MILLIS;
        }
    }

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IAdminMapper adminMapper;
    private final IAiService aiService;

    /** 네이버 18번을 한꺼번에 부를 때 쓴다. ExternalApiConfig 가 만들어 준다 */
    private final ExecutorService searchExecutor;

    private final String naverId;
    private final String naverSecret;
    private final String kakaoKey;

    public EventSearchService(IAdminMapper adminMapper,
                              IAiService aiService,
                              @Qualifier("externalRestClient") RestClient restClient,
                              ExecutorService searchExecutor,
                              @Value("${naver.hub.key.id:}") String naverId,
                              @Value("${naver.hub.key:}") String naverSecret,
                              @Value("${kakao.rest.key:}") String kakaoKey) {
        this.adminMapper = adminMapper;
        this.aiService = aiService;
        this.naverId = naverId;
        this.naverSecret = naverSecret;
        this.kakaoKey = kakaoKey;
        this.searchExecutor = searchExecutor;
        this.restClient = restClient;
    }

    // =====================================================================
    // 1. 어떤 대회가 있는지 찾음
    // =====================================================================
    @Override
    public List<EventSearchDTO> discover(String keyword, boolean refresh) throws Exception {

        log.info("{}.discover Start! keyword : {} / refresh : {}",
                this.getClass().getName(), keyword, refresh);

        String cacheKey = (keyword == null) ? "" : keyword.trim();

        if (!refresh) {
            Cached hit = cache.get(cacheKey);
            if (hit != null && hit.fresh()) {
                log.info("{}.discover End! 보관해 둔 결과 {}건",
                        this.getClass().getName(), hit.list().size());
                return hit.list();
            }
        }

        String[] queries = (keyword == null || keyword.isBlank())
                ? DEFAULT_QUERIES : new String[]{keyword};

        // 검색 결과 제목을 먼저 다 모은다.
        //
        // webkr(웹문서)을 함께 보는 이유가 있다.
        // 블로그는 같은 대회를 여러 사람이 써서 한 대회가 목록을 다 차지한다.
        // 웹문서는 대회 공식 사이트가 바로 걸리고, 요약에 날짜·장소까지 들어 있다.
        //   예) "제23회 강남국제평화마라톤대회"
        //       -> 2026. 10. 05. 봉은사로 삼성1동주민센터 앞, Full/Half/10km/5km
        //
        // 그래서 제목만 쓰지 않고 요약도 함께 넘긴다. 이름을 더 정확히 잡아낸다.
        // 검색어 6개 x 종류 3가지 = 18번을 한꺼번에 부른다.
        // 하나씩 기다리면 그것만 3초 가까이 걸린다.
        List<String[]> jobs = new ArrayList<>();
        for (String q : queries) {
            for (String kind : new String[]{"webkr", "blog", "news"}) {
                jobs.add(new String[]{kind, q});
            }
        }

        // 정렬은 sim(정확도)으로 고정한다.
        // date(최신순)로 하면 새 글이 올라올 때마다 30건의 내용이 바뀌어,
        // 같은 버튼을 눌러도 찾아지는 대회 수가 매번 달라진다.
        //
        // 요약은 앞부분만 쓴다. 대회 이름을 알아보는 데는 그걸로 충분하고,
        // 통째로 보내면 8만 자가 넘어 Gemini 를 여러 번 나눠 불러야 한다.
        // 같은 글이 검색어 여러 개에 걸리므로 겹치는 것도 버린다.
        //
        // parallelStream() 은 병렬도가 (코어수 - 1) 이라 18개가 3개씩 나뉘어 돌았다
        List<CompletableFuture<List<Map<String, Object>>>> calls = jobs.stream()
                .map(j -> CompletableFuture.supplyAsync(
                        () -> naverItems(j[0], j[1], 30, "sim"), searchExecutor))
                .toList();

        List<String> titles = calls.stream()
                .flatMap(f -> f.join().stream())
                .map(it -> {
                    String t = clean(str(it.get("title")));
                    String d = clean(str(it.get("description")));
                    if (d.length() > 60) {
                        d = d.substring(0, 60);
                    }
                    return d.isEmpty() ? t : t + " — " + d;
                })
                .distinct()
                .toList();

        // 제목에서 대회 이름을 뽑는다. Gemini 가 못 하면 여기서 끝낸다.
        // 규칙으로 뽑던 대비책은 없앴다. 네이버 키가 없으면 제목 자체가 0개라 어차피 못 뽑고,
        // Gemini 가 죽었을 때는 잡음 섞인 목록을 보여주느니 다시 찾게 하는 편이 낫다.
        List<EventSearchDTO> found = aiService.isReady()
                ? aiService.extractNames(titles) : List.of();

        if (found.isEmpty()) {
            log.info("{}.discover End! Gemini 를 못 썼다 : {}", this.getClass().getName(),
                    aiService.isReady() ? "호출이 모두 실패했다" : "키가 없다");
            // 보관하지 않는다. 담아 두면 10분 동안 빈 목록만 보게 된다.
            return List.of();
        }

        // 합치기 전에 지역으로 먼저 거른다. 백 개가 넘는 목록을 한 번에 합치라고 하면
        // 부를 때마다 102개, 53개로 들쭉날쭉해진다.
        List<EventSearchDTO> ours = new ArrayList<>();
        for (EventSearchDTO d : found) {
            if (isOurRegion(d.getRegion()) && isThisYear(d.getName())) {
                ours.add(d);
            }
        }

        // 한 덩어리로 받으면 반려한 대회도 '이미 등록됨' 이 되어 다시 등록할 길이 없다
        List<String> registered = adminMapper.getEventTitles();
        List<String> rejected = adminMapper.getRejectedTitles();

        // 합치면서 이미 등록된 대회인지도 같이 판단하게 한다.
        // 판단하지 못했으면 null 이 온다. 그때는 아래에서 이름 대조로 대신한다.
        List<EventSearchDTO> merged = ours.isEmpty()
                ? null : aiService.mergeNames(ours, registered, rejected);
        if (merged != null) {
            ours = merged;
        }

        // 같은 대회가 두 줄로 남는 것만 막는다. 합치는 일은 Gemini 가 이미 했다.
        List<EventSearchDTO> rList = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (EventSearchDTO d : ours) {
            String key = groupKey(d.getName());
            if (!seen.add(key)) {
                continue;
            }
            if (merged == null) {
                d.setRegistered(matches(registered, key));
                d.setRejected(matches(rejected, key));
            }
            rList.add(d);
        }

        // 아직 안 넣은 대회를 맨 위로. 관리자가 할 일이 그것이기 때문이다.
        // 안정 정렬이라 그 안에서는 Gemini 가 준 차례가 그대로 남는다.
        rList.sort(Comparator.comparing(EventSearchDTO::isRegistered));

        cache.put(cacheKey, new Cached(System.currentTimeMillis(), rList));

        log.info("{}.discover End! {}건", this.getClass().getName(), rList.size());

        return rList;
    }

    // =====================================================================
    // 2. 대회 이름으로 공식 홈페이지 찾기
    // =====================================================================
    @Override
    @SuppressWarnings("unchecked")
    public String findSite(String eventName) throws Exception {

        if (eventName == null || eventName.isBlank()) {
            return null;
        }

        // 대회 이름만으로 찾으면 뉴스·정리글이 위에 오는 일이 많다.
        // '참가신청' 을 붙여 한 번 더 찾는다. 접수 페이지는 공식 사이트에만 있다.
        List<Map<String, Object>> items = new ArrayList<>(
                naverItems("webkr", eventName + " 참가신청", 8, "sim"));
        items.addAll(naverItems("webkr", eventName, 8, "sim"));

        // 확실히 공식이 아닌 곳은 먼저 걷어낸다
        List<Map<String, String>> cands = new ArrayList<>();
        for (Map<String, Object> it : items) {
            String url = str(it.get("link"));
            if (host(url).isEmpty() || isNotOfficial(url)) {
                continue;
            }
            cands.add(Map.of("title", clean(str(it.get("title"))), "link", url));
        }
        if (cands.isEmpty()) {
            return null;
        }

        // 남은 후보 중에서 고르는 건 Gemini 에게 맡긴다.
        //
        // 도메인이 몇 번 나왔는지로만 고르다가 두 번 틀렸다.
        //   '롯데리아 마라톤'      -> 뉴스 기사가 잡혔다
        //   '강남국제평화마라톤'   -> 대행 용역업체 선정 공고가 잡혔다
        // 둘 다 제목을 읽으면 공식이 아닌 게 바로 보이는데, 도메인만 봐서는 알 수 없었다.
        if (aiService.isReady()) {
            // "종류|주소" 로 온다. 억지로 고르지 말라고 시켰으므로 못 골랐으면 null 이다.
            return aiService.pickSite(eventName, cands);
        }

        // 키가 없을 때 : 같은 도메인이 여러 쪽 걸린 것을 공식으로 본다
        Map<String, Integer> hosts = new LinkedHashMap<>();
        Map<String, String> firstUrl = new LinkedHashMap<>();
        for (Map<String, String> c : cands) {
            String h = host(c.get("link"));
            hosts.merge(h, 1, Integer::sum);
            firstUrl.putIfAbsent(h, c.get("link"));
        }
        return hosts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> "INFO|" + firstUrl.get(e.getKey()))
                .orElse(null);
    }

    // =====================================================================
    // 3. 장소 이름으로 좌표 얻기
    // =====================================================================
    @Override
    @SuppressWarnings("unchecked")
    public EventDTO findPlace(String placeName) throws Exception {

        if (placeName == null || placeName.isBlank() || kakaoKey.isBlank()) {
            return null;
        }

        try {
            String body = restClient.get()
                    .uri(URI.create(KAKAO + "?query=" + enc(placeName)))
                    .header("Authorization", "KakaoAK " + kakaoKey)
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                return null;
            }

            Map<String, Object> json = objectMapper.readValue(body, Map.class);
            List<Map<String, Object>> docs = (List<Map<String, Object>>) json.get("documents");
            if (docs == null || docs.isEmpty()) {
                return null;
            }

            Map<String, Object> d = docs.get(0);
            String addr = str(d.get("road_address_name"));
            if (addr.isEmpty()) {
                addr = str(d.get("address_name"));
            }
            // "서울 영등포구 여의동로 330" 에서 두 번째 조각이 자치구다
            String[] parts = addr.split(" ");

            EventDTO rDTO = new EventDTO();
            rDTO.setPlaceName(str(d.get("place_name")));
            rDTO.setSigungu(parts.length > 1 ? parts[1] : null);
            rDTO.setLat(Double.parseDouble(str(d.get("y"))));
            rDTO.setLng(Double.parseDouble(str(d.get("x"))));

            return rDTO;

        } catch (Exception e) {
            log.warn("좌표 조회 실패 : {}", e.getMessage());
            return null;
        }
    }


    // =====================================================================
    // 4. 공식 사이트를 읽어 값 채우기
    // =====================================================================
    @Override
    public EventDTO readSite(String url, String eventName) throws Exception {

        log.info("{}.readSite Start! {}", this.getClass().getName(), url);

        EventDTO rDTO = new EventDTO();

        // 주소를 못 찾았어도 그만두지 않는다.
        // 검색 요약만으로 채워지는 대회가 있어서다.
        boolean hasUrl = url != null && url.startsWith("http");

        // 사이트 글을 가져온다. 못 가져와도 그만두지 않는다.
        //
        // 화면을 자바스크립트로 그리는 사이트가 꽤 있다.
        // 그런 곳은 HTML 에 글자가 없어서 아무것도 못 읽는다.
        //   예) 잠수교 나이트런의 ghkglobal.co.kr
        // 인증서가 안 맞거나 접근을 막아 둔 곳도 있다.
        String text = "";
        try {
            String html = !hasUrl ? null : restClient.get()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .body(String.class);

            if (html != null && !html.isBlank()) {
                text = stripTags(html);

                // 접수기간·참가비는 '대회요강' 같은 하위 쪽에 있는 곳이 많아 한 단계만 더 읽는다.
                for (String sub : guideLinks(html, url)) {
                    try {
                        String subHtml = restClient.get()
                                .uri(URI.create(sub))
                                .header("User-Agent", "Mozilla/5.0")
                                .retrieve()
                                .body(String.class);
                        if (subHtml != null) {
                            text += "\n" + stripTags(subHtml);
                        }
                    } catch (Exception ignore) {
                        // 하위 쪽 하나 못 읽는다고 그만둘 이유는 없다
                    }
                }
            }
        } catch (Exception e) {
            log.warn("사이트를 읽지 못했다 : {}", e.getMessage());
        }

        // 네이버 웹문서 요약에 날짜·장소가 들어 있어 잘 읽었더라도 함께 붙인다.
        text = snippets(eventName) + "\n" + text;

        if (text.isBlank()) {
            return rDTO;
        }

        // ---------- 날짜 ----------
        // 지난 회차 기록이 함께 남아 있는 사이트가 많아 오늘보다 뒤인 것만 쓴다.
        List<LocalDate> dates = findDates(text);
        LocalDate today = LocalDate.now();

        List<LocalDate> future = new ArrayList<>();
        for (LocalDate d : dates) {
            if (!d.isBefore(today)) {
                future.add(d);
            }
        }
        future.sort(Comparator.naturalOrder());

        if (!future.isEmpty()) {
            // 접수 마감은 행사일보다 앞이므로 가장 이른 날을 마감, 가장 늦은 날을 행사일로 본다.
            if (future.size() >= 2) {
                rDTO.setApplyEnd(future.get(0));
                rDTO.setStartDate(future.get(future.size() - 1));
                rDTO.setEndDate(future.get(future.size() - 1));
            } else {
                rDTO.setStartDate(future.get(0));
                rDTO.setEndDate(future.get(0));
            }
        }

        // ---------- 참가비 ----------
        // "70,000원" 처럼 세 자리마다 쉼표가 있고 원으로 끝나는 것만 본다.
        // 만 원 미만은 참가비가 아닐 때가 많아 걸러낸다.
        Matcher m = Pattern.compile("([1-9][0-9]{0,2}(?:,[0-9]{3})+)\\s*원").matcher(text);
        List<String> fees = new ArrayList<>();
        while (m.find()) {
            String num = m.group(1).replace(",", "");
            if (Long.parseLong(num) >= 10000 && !fees.contains(m.group(1) + "원")) {
                fees.add(m.group(1) + "원");
            }
        }
        if (!fees.isEmpty()) {
            rDTO.setFeeText(String.join(" / ", fees.subList(0, Math.min(3, fees.size()))));
        }

        // ---------- Gemini ----------
        // "빈 칸만 채운다" 가 아니라 통째로 갈아 끼운다.
        // 규칙은 기념품 값·환불 수수료를 참가비로, 환불일자를 마감일로 집어 온다.
        // 어느 숫자가 참가비인지는 앞뒤 문장을 읽어야 알 수 있어서 규칙이 못 한다.
        if (aiService.isReady()) {
            EventDTO ai = aiService.extractEvent(text, eventName);

            rDTO.setStartDate(ai.getStartDate());
            rDTO.setEndDate(ai.getEndDate());
            rDTO.setApplyStart(ai.getApplyStart());
            rDTO.setApplyEnd(ai.getApplyEnd());
            rDTO.setFeeText(ai.getFeeText());
            rDTO.setPlaceName(ai.getPlaceName());
            rDTO.setDistances(ai.getDistances());
            rDTO.setTarget(ai.getTarget());
            rDTO.setContact(ai.getContact());
            rDTO.setEventType(ai.getEventType());

        }

        log.info("{}.readSite End! 행사일 {} / 마감 {} / 장소 {} / 참가비 {}",
                this.getClass().getName(),
                rDTO.getStartDate(), rDTO.getApplyEnd(),
                rDTO.getPlaceName(), rDTO.getFeeText());

        return rDTO;
    }



    /** 메뉴 글자에 요강·접수 같은 말이 있는 링크를 골라 온다. 같은 사이트 안에서만, 최대 3개 */
    private static final Pattern LINK =
            Pattern.compile("<a[^>]+href=[\"']([^\"'#]+)[\"'][^>]*>([^<]{1,30})</a>",
                            Pattern.CASE_INSENSITIVE);

    private static final String[] GUIDE_WORDS = {
            "요강", "접수", "안내", "참가", "일정", "개요", "코스", "신청"};

    private List<String> guideLinks(String html, String baseUrl) {
        List<String> rList = new ArrayList<>();
        try {
            URI base = URI.create(baseUrl);
            Matcher m = LINK.matcher(html);
            while (m.find() && rList.size() < 3) {
                String href = m.group(1).trim();
                String label = m.group(2).trim();

                boolean wanted = false;
                for (String w : GUIDE_WORDS) {
                    if (label.contains(w)) {
                        wanted = true;
                        break;
                    }
                }
                if (!wanted || href.endsWith(".css") || href.endsWith(".js")) {
                    continue;
                }

                URI full = base.resolve(href);
                // 남의 사이트로 새어 나가지 않게 같은 호스트만 본다
                if (full.getHost() != null && full.getHost().equals(base.getHost())
                        && !rList.contains(full.toString())) {
                    rList.add(full.toString());
                }
            }
        } catch (Exception e) {
            log.warn("하위 쪽 찾기 실패 : {}", e.getMessage());
        }
        if (!rList.isEmpty()) {
            log.info("하위 쪽 {}개 더 읽는다 : {}", rList.size(), rList);
        }
        return rList;
    }

    /** 그 대회로 웹문서를 검색해 제목과 요약을 이어 붙인다 */
    private String snippets(String eventName) {
        if (eventName == null || eventName.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> it : naverItems("webkr", eventName, 6, "sim")) {
            sb.append(clean(str(it.get("title")))).append(" ")
              .append(clean(str(it.get("description")))).append("\n");
        }
        return sb.toString();
    }

    /** 페이지에 적힌 날짜를 모두 찾는다. 한국 사이트에서 흔한 세 가지 모양을 본다. */
    private List<LocalDate> findDates(String text) {
        List<LocalDate> rList = new ArrayList<>();

        // 2026-09-06 / 2026.09.06 / 2026/9/6
        Matcher m1 = Pattern.compile("(20\\d\\d)[.\\-/](\\d{1,2})[.\\-/](\\d{1,2})").matcher(text);
        while (m1.find()) {
            add(rList, m1.group(1), m1.group(2), m1.group(3));
        }

        // 2026년 9월 6일
        Matcher m2 = Pattern.compile("(20\\d\\d)\\s*년\\s*(\\d{1,2})\\s*월\\s*(\\d{1,2})\\s*일").matcher(text);
        while (m2.find()) {
            add(rList, m2.group(1), m2.group(2), m2.group(3));
        }

        // 9월 6일  (연도가 없으면 올해로 본다)
        Matcher m3 = Pattern.compile("(?<!\\d)(\\d{1,2})\\s*월\\s*(\\d{1,2})\\s*일").matcher(text);
        while (m3.find()) {
            add(rList, String.valueOf(LocalDate.now().getYear()), m3.group(1), m3.group(2));
        }

        return rList;
    }

    private void add(List<LocalDate> list, String y, String mo, String d) {
        try {
            LocalDate date = LocalDate.of(Integer.parseInt(y), Integer.parseInt(mo), Integer.parseInt(d));
            if (!list.contains(date)) {
                list.add(date);
            }
        } catch (Exception ignore) {
            // 2026-13-45 같은 값이 걸릴 수 있다. 날짜가 아니면 버린다.
        }
    }

    /** 태그와 스크립트를 걷어내고 글자만 남긴다 */
    private String stripTags(String html) {
        return html.replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("(?s)<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ");
    }

    // =====================================================================
    // 아래는 안에서만 쓰는 것들
    // =====================================================================

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> naverItems(String kind, String query,
                                                 int display, String sort) {
        if (naverId.isBlank() || naverSecret.isBlank()) {
            log.info("naver.hub.key 가 없어 검색을 건너뛴다");
            return List.of();
        }
        try {
            // 네이버는 JSON 을 text/plain 으로 보내서 .body(Map.class) 가 안 된다.
            // 문자열로 받아 Jackson 으로 직접 읽는다.
            String body = restClient.get()
                    .uri(URI.create(NAVER + kind + "?query=" + enc(query)
                            + "&display=" + display + "&sort=" + sort + "&format=json"))
                    .header("X-NCP-APIGW-API-KEY-ID", naverId)
                    .header("X-NCP-APIGW-API-KEY", naverSecret)
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                return List.of();
            }

            Map<String, Object> json = objectMapper.readValue(body, Map.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) json.get("items");
            return items == null ? List.of() : items;

        } catch (Exception e) {
            // 검색이 안 되어도 관리자 화면은 열려야 한다. 손으로 넣는 길이 남아 있다.
            log.warn("네이버 {} 검색 실패 : {}", kind, e.getMessage());
            return List.of();
        }
    }



    /**
     * 서울·경기 대회인지 볼 때 쓴다. 프롬프트로도 막지만 한 번 더 거른다.
     * 대회 '이름' 이 아니라 Gemini 가 요약을 읽고 알려 준 지역 값을 본다.
     */
    private static final String[] OUR_REGION = {
            "서울", "SEOUL", "Seoul", "경기",
            // 경기도는 시·군 이름만 적혀 오는 경우가 많다
            "수원", "성남", "고양", "용인", "부천", "안산", "안양", "남양주",
            "화성", "평택", "의정부", "시흥", "파주", "김포", "광명", "군포",
            "하남", "오산", "양주", "이천", "구리", "안성", "포천", "의왕",
            "여주", "양평", "동두천", "과천", "가평", "연천", "일산", "분당", "판교"};

    private boolean isOurRegion(String region) {
        if (region == null || region.isBlank()) {
            return false;   // 지역을 모르면 관리자도 판단할 수 없다
        }
        for (String r : OUR_REGION) {
            if (region.contains(r)) {
                return true;
            }
        }
        return false;
    }

    /** 이름에 박힌 연도가 올해인지. 연도가 없으면 통과시킨다. 대회일로 다시 걸러진다 */
    private boolean isThisYear(String name) {
        Matcher m = Pattern.compile("20\\d\\d").matcher(name);
        while (m.find()) {
            if (!m.group().equals(String.valueOf(LocalDate.now().getYear()))) {
                return false;
            }
        }
        return true;
    }

    /** 같은 대회로 묶기 위한 열쇠. 회차·연도·공백·가운뎃점을 지운다 */
    private String groupKey(String name) {
        String key = name.replaceAll("제\\s?\\d+회|20\\d\\d|\\s|·|-|&", "").toLowerCase();

        // "OO 10km대회" 와 "OO 10km마라톤" 은 같은 대회다.
        // 다만 떼고 너무 짧아지면 떼지 않는다. "2026 서울런" -> "서울" 이 되면 아무 데나 걸린다.
        String cut = key.replaceAll("대회$|마라톤대회$|마라톤$|레이스$|런$", "");
        return cut.length() >= 5 ? cut : key;
    }

    /** 이미 DB 에 있는 대회인지. 한쪽이 더 긴 이름인 경우가 흔해 양쪽 다 본다 */
    private boolean matches(List<String> titles, String key) {
        return titles.stream().anyMatch(t -> {
            String other = groupKey(t);
            return other.contains(key) || key.contains(other);
        });
    }

    private boolean isNotOfficial(String url) {
        for (String s : NOT_OFFICIAL) {
            if (url.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private String host(String url) {
        Matcher m = Pattern.compile("https?://([^/]+)").matcher(url == null ? "" : url);
        return m.find() ? m.group(1).replace("www.", "") : "";
    }

    /** 검색 결과 제목에는 &lt;b&gt; 태그와 HTML 기호가 섞여 온다 */
    private String clean(String s) {
        return s.replaceAll("</?b>", "")
                .replace("&quot;", "\"").replace("&amp;", "&")
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("&#39;", "'").trim();
    }

    private String enc(String s) {
        return UriUtils.encode(s, StandardCharsets.UTF_8);
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

}
