package com.moveon.service.impl;

import tools.jackson.databind.ObjectMapper;
import com.moveon.dto.EventDTO;
import com.moveon.dto.EventSearchDTO;
import com.moveon.mapper.IAdminMapper;
import com.moveon.service.IAiService;
import com.moveon.service.IEventSearchService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 행사를 찾는 일. 네이버 검색과 카카오 좌표를 쓴다.
 *
 * 왜 검색으로 찾느냐면, 공공데이터에 스포츠 행사가 거의 없어서다.
 * 서울시 문화행사·전국공연행사 표준데이터·TourAPI·경기도 문화축제·대한체육회를
 * 전부 열어 봤지만 지금 서울에서 열리는 것으로 얻어지는 건 4건뿐이었다.
 * 마라톤은 대부분 민간이 주최해서 공공데이터로 올라갈 이유가 없기 때문이다.
 *
 * 검색은 세 가지를 나눠 쓴다.
 *   blog / news  어떤 대회가 있는지 알아낸다
 *   webkr        대회 이름으로 공식 홈페이지를 찾는다
 *
 * 카카오 검색으로도 같은 시험을 했는데 공식 사이트를 하나도 못 찾았다(0/5).
 * 네이버는 5/5 였다. 그래서 검색은 네이버로 가고, 카카오는 좌표 변환에만 쓴다.
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
     * 제목에서 대회 이름으로 보이는 구절을 뽑는 규칙.
     *
     * 이름이 '마라톤' 으로 끝난다고 생각하면 크게 놓친다.
     * 요즘 대회는 '런' 으로 끝나는 게 많다 (인사이더런, 나이트런, 런서울런, 고프리런).
     * 실제로 '런' 계열을 넣지 않았을 때 제목 240개에서 63개가 나왔는데,
     * 넣으니 103개가 됐고 그중에는 가장 많이 언급된 '잠수교 10K 나이트런' 이 있었다.
     *
     * 뒤에 한글이 이어지면 뺀다. 안 그러면 '런던' 같은 낱말이 걸린다.
     */
    private static final Pattern NAME = Pattern.compile(
            "(?:제\\s?\\d+회\\s*)?(?:20\\d\\d\\s*)?"
            + "[가-힣A-Za-z][가-힣A-Za-z0-9\\-·&\\s]{1,22}?"
            + "(?:하프\\s?마라톤대회|하프\\s?마라톤|마라톤대회|마라톤|걷기대회|러닝대회"
            + "|러닝|레이스|[Rr][Aa][Cc][Ee]|[Rr][Uu][Nn]|런)(?![가-힣])");

    /** 이름 앞에 딸려 오는 잡음 낱말 */
    private static final String[] HEAD_NOISE = {
            "코스", "일정", "총정리", "후기", "방법", "정보", "준비", "기념품",
            "꿀팁", "추천", "접수", "신청", "대회", "참가"};

    /** 종목명이거나 문장 조각이라 대회 이름이 될 수 없는 것 */
    private static final String[] DROP = {
            "트레일러닝", "도심 러닝", "비대면", "배불", "즐기는", "하프 코스",
            "된 마라톤", "감동", "산악마라톤"};

    /**
     * 대회 공식 사이트가 아닌 곳. 여기 걸리면 후보에서 뺀다.
     *
     * 두 갈래다.
     *   1) 블로그·커뮤니티·뉴스   남이 쓴 글이지 주최자의 안내가 아니다
     *   2) 대회 모음 사이트        여러 대회를 한데 모아 두는 곳이다.
     *      이쪽이 더 위험하다. 어느 대회로 검색해도 걸리기 때문에
     *      "롯데리아 마라톤" 을 찾았는데 엉뚱한 대회 쪽이 잡히는 일이 생긴다.
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

    private final String naverId;
    private final String naverSecret;
    private final String kakaoKey;

    public EventSearchService(IAdminMapper adminMapper,
                              IAiService aiService,
                              @Value("${naver.hub.key.id:}") String naverId,
                              @Value("${naver.hub.key:}") String naverSecret,
                              @Value("${kakao.rest.key:}") String kakaoKey) {
        this.adminMapper = adminMapper;
        this.aiService = aiService;
        this.naverId = naverId;
        this.naverSecret = naverSecret;
        this.kakaoKey = kakaoKey;
        this.restClient = RestClient.create();
    }

    // =====================================================================
    // 1. 어떤 대회가 있는지 찾는다
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
        List<String> titles = jobs.parallelStream()
                .flatMap(j -> naverItems(j[0], j[1], 30, "sim").stream())
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

        // 제목에서 대회 이름을 뽑아 같은 대회끼리 묶는다.
        //
        // Gemini 가 있으면 그쪽에 맡긴다. 정규식은 세 가지를 못 했다.
        //   - "리아는 배불런 롯데리아 마라톤" 에서 뒤쪽만 잘라 이름을 망가뜨렸다
        //   - "잠수교 나이트런" 과 "2025 서울 잠수교 나이트런" 을 다른 대회로 봤다
        //   - "배불런", "감동의 마라톤" 같은 문장 조각을 못 걸러냈다
        //
        // 키가 없으면 예전 규칙으로 돌아간다. 정확하진 않아도 화면은 돌아가야 한다.
        Map<String, List<String>> groups = new LinkedHashMap<>();

        // 이름과 함께 지역도 받는다. 지역은 거르는 데만 쓰고 화면에도 보여준다.
        Map<String, String> regionOf = new LinkedHashMap<>();

        List<EventSearchDTO> found = aiService.isReady()
                ? aiService.extractNames(titles) : List.of();

        // 합치기 전에 지역으로 먼저 거른다.
        //
        // 순서가 중요하다. 백 개가 넘는 목록을 한 번에 합치라고 하면
        // 부를 때마다 102개, 53개로 들쭉날쭉해진다. 판단할 쌍이 너무 많아서다.
        // 서울·경기만 남겨 절반으로 줄이면 결과가 안정된다.
        List<EventSearchDTO> ours = new ArrayList<>();
        for (EventSearchDTO d : found) {
            if (isOurRegion(d.getRegion()) && isThisYear(d.getName())) {
                ours.add(d);
            }
        }
        if (!ours.isEmpty()) {
            ours = aiService.mergeNames(ours);
        }

        List<String> names = new ArrayList<>();
        for (EventSearchDTO d : ours) {
            names.add(d.getName());
            regionOf.put(d.getName(), d.getRegion());
        }

        if (!names.isEmpty()) {
            // Gemini 가 이미 같은 대회를 하나로 합쳐 준다.
            // 언급 횟수는 그 이름이 제목 몇 개에 나오는지 세어 붙인다.
            // Gemini 가 이미 같은 대회를 하나로 합쳐 주고 잡음도 걸러 준다.
            // 그래서 몇 번 언급됐는지는 세지 않는다.
            //
            // 세어 봤다가 접었다. 이름이 짧으면 다른 대회 제목까지 먹는다.
            //   "서울마라톤" 은 groupKey 가 공백을 지우는 탓에
            //   "2026 서울 마라톤 일정" 같은 제목에도 걸려 51회로 세어졌다.
            // 목록 순서는 '아직 등록 안 한 것 먼저' 로 정하므로 숫자가 없어도 된다.
            for (String name : names) {
                groups.computeIfAbsent(groupKey(name), k -> new ArrayList<>()).add(name);
            }
        } else {
            // Gemini 를 못 쓰는 상황(키가 없거나 과부하)에서 쓰는 대비책이다.
            // 규칙만으로는 잡음이 많아 수백 건이 나온다.
            // 그래서 여러 글에서 언급된 것만 남긴다. 한 번짜리는 대개 문장 조각이다.
            log.info("Gemini 를 못 써서 규칙으로 뽑는다");
            Map<String, List<String>> rough = new LinkedHashMap<>();
            for (String title : titles) {
                for (String name : extractNamesByRule(title)) {
                    rough.computeIfAbsent(groupKey(name), k -> new ArrayList<>()).add(name);
                }
            }
            for (Map.Entry<String, List<String>> e : rough.entrySet()) {
                if (e.getValue().size() >= 2) {
                    groups.put(e.getKey(), e.getValue());
                }
            }
        }

        List<String> registered = adminMapper.getEventTitles();

        List<EventSearchDTO> rList = new ArrayList<>();
        for (List<String> group : groups.values()) {
            String name = mostCommon(group);
            String region = regionOf.get(name);

            // Gemini 로 뽑은 것은 위에서 이미 걸렀다.
            // 규칙으로 뽑은 경우에만 여기서 이름을 보고 거른다. 지역을 알 길이 없어서다.
            if (regionOf.isEmpty() && (!isOurRegion(name) || !isThisYear(name))) {
                continue;
            }
            EventSearchDTO dto = new EventSearchDTO();
            dto.setName(name);
            dto.setRegion(region);
            // Gemini 로 뽑았으면 1 이라 셈이 의미 없다. 화면에서 숫자를 감추는 데 쓴다.
            dto.setMentions(group.size() > 1 ? group.size() : 0);
            // 이미 넣은 대회는 화면에서 흐리게 보여준다. 같은 걸 두 번 넣지 않도록.
            String key = groupKey(dto.getName());
            dto.setRegistered(registered.stream().anyMatch(t -> groupKey(t).contains(key)
                    || key.contains(groupKey(t))));
            rList.add(dto);
        }

        // 아직 안 넣은 대회를 맨 위로 올린다. 관리자가 할 일이 그것이기 때문이다.
        // 그 안에서는 손대지 않는다.
        //   Gemini 를 쓸 때는 Gemini 가 준 차례가 그대로 남고,
        //   못 쓸 때는 여러 글에서 언급된 것이 위로 온다.
        // sort 는 순서가 같은 것끼리 자리를 안 바꾸므로(안정 정렬) 그렇게 된다.
        rList.sort(Comparator.comparing(EventSearchDTO::isRegistered)
                .thenComparing(Comparator.comparingInt(EventSearchDTO::getMentions).reversed()));

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
            String[] parts = addr.split(" ");

            EventDTO rDTO = new EventDTO();
            rDTO.setPlaceName(str(d.get("place_name")));
            rDTO.setSido(parts.length > 0 ? parts[0] : null);
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

                // 첫 화면에는 대회 이름과 사진만 있고,
                // 접수기간·참가비는 '대회요강' 같은 하위 쪽에 있는 곳이 많다.
                //   예) 고프리런은 /guide/mainpoints 와 /sub/receivenotice 에 들어 있다.
                // 그래서 메뉴에서 그런 쪽을 찾아 한 단계만 더 읽는다.
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

        // 사이트에서 못 읽었으면 검색 요약으로 대신한다.
        //
        // 네이버 웹문서 검색은 요약에 알맹이를 담아 준다.
        //   "2026. 10. 05. (월), 봉은사로 삼성1동주민센터 앞, Full, Half, 10km, 5km"
        // 사이트를 못 읽는 대회도 이걸로는 채워지는 경우가 많다.
        // 사이트를 잘 읽었더라도 요약을 함께 붙인다.
        //
        // 첫 화면에는 대회 이름만 있고 접수기간·참가비는 '대회요강' 같은 하위 쪽에 있는 곳이 많다.
        // 그 하위 쪽이 검색에는 따로 걸려서, 요약에 그 내용이 들어온다.
        //   예) 고프리런은 첫 화면에 접수기간이 없고 대회요강 쪽에만 있다.
        text = snippets(eventName) + "\n" + text;

        if (text.isBlank()) {
            return rDTO;
        }

        // ---------- 날짜 ----------
        // 페이지에 적힌 날짜를 모두 모은 뒤,
        // 앞으로 올 날 중 가장 이른 것을 행사일로 본다.
        // 대회 사이트에는 지난 회차 기록이 함께 남아 있는 경우가 많아 오늘보다 뒤인 것만 쓴다.
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
            // 접수 마감은 행사일보다 앞이다. 그래서 가장 이른 날을 마감,
            // 그보다 뒤에 있는 날 중 하나를 행사일로 본다.
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

        // ---------- 규칙으로 못 채운 칸만 Gemini 에게 ----------
        //
        // 규칙을 먼저 돌리는 이유가 두 가지다.
        //   1) 날짜·금액은 규칙이 이미 잘 잡는다. 굳이 부를 이유가 없다.
        //   2) 무료 한도가 하루 1,000건이라 아껴 쓰는 편이 좋다.
        //
        // 장소는 규칙으로 아예 못 뽑는다.
        // "여의도 한강공원 물빛광장에서 출발하여" 처럼 문장 속에 섞여 있어서다.
        // 그래서 대부분 이 단계에서 채워진다.
        if (aiService.isReady()) {
            EventDTO ai = aiService.extractEvent(text, eventName);

            // Gemini 값을 규칙 값보다 앞세운다.
            //
            // 처음에는 반대로 했다가 틀린 값이 남는 걸 봤다.
            // 규칙은 페이지에 적힌 금액을 아무거나 집어 온다.
            //   고프리런에서 실제 참가비는 "HALF 70,000원 / 10km 70,000원" 인데
            //   규칙은 "65,000원 / 55,000원 / 70,000원" 을 넣었다.
            //   기념품 값이나 환불 수수료까지 같이 긁어 온 것이다.
            // 날짜도 마찬가지다. 환불일자·접수마감시각이 함께 적혀 있다.
            //
            // 어느 숫자가 '참가비' 인지는 앞뒤 문장을 읽어야 알 수 있고, 그건 규칙이 못 한다.
            // 규칙은 이제 Gemini 를 못 쓸 때의 대비책이다.
            // Gemini 를 쓸 수 있으면 규칙 결과를 통째로 갈아 끼운다.
            //
            // "빈 칸만 채운다" 로 두면 규칙이 넣은 틀린 값이 그대로 남는다.
            //   고프리런은 '2026년 9월 1일 10:00 ~ 선착순마감' 이라 마감 날짜가 없는데,
            //   규칙은 페이지에 있는 날짜 중 이른 것을 마감으로 넣어 버렸다.
            //   Gemini 가 비워도 그 값이 살아남아 화면에 잘못 표시됐다.
            //
            // 어느 날짜가 '접수 마감' 인지는 앞뒤 문장을 읽어야 알 수 있고 규칙은 그걸 못 한다.
            // 그래서 Gemini 가 있으면 그쪽 말만 듣는다. 규칙은 키가 없을 때의 대비책이다.
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

    /** 네이버 검색 결과에서 제목만 뽑아 온다 */
    private List<String> naverTitles(String kind, String query) {
        List<String> rList = new ArrayList<>();
        for (Map<String, Object> it : naverItems(kind, query, 30, "date")) {
            rList.add(clean(str(it.get("title"))));
        }
        return rList;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> naverItems(String kind, String query,
                                                 int display, String sort) {
        if (naverId.isBlank() || naverSecret.isBlank()) {
            log.info("naver.hub.key 가 없어 검색을 건너뛴다");
            return List.of();
        }
        try {
            // 네이버는 JSON 을 보내면서 Content-Type 을 text/plain 으로 적어 준다.
            // 그래서 .body(Map.class) 로 받으면 "no suitable HttpMessageConverter" 가 난다.
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

    /** 제목에서 대회 이름으로 보이는 구절을 뽑는다 */
    private List<String> extractNamesByRule(String title) {
        List<String> rList = new ArrayList<>();
        Matcher m = NAME.matcher(title);
        while (m.find()) {
            String s = m.group().trim().replaceAll("\\s+", " ");

            // 앞에 붙은 잡음 낱말을 더 없을 때까지 떼어 낸다
            boolean changed = true;
            while (changed) {
                changed = false;
                for (String w : HEAD_NOISE) {
                    if (s.startsWith(w + " ") || s.equals(w)) {
                        s = s.substring(w.length()).trim();
                        changed = true;
                    }
                }
            }

            if (s.length() < 5 || s.length() > 34) {
                continue;
            }
            boolean drop = false;
            for (String d : DROP) {
                if (s.contains(d)) {
                    drop = true;
                    break;
                }
            }
            if (!drop) {
                rList.add(s);
            }
        }
        return rList;
    }


    /**
     * 서울 대회가 아닌 것을 걸러낸다.
     *
     * 검색 결과에 전국 대회 목록을 실은 쪽이 섞여 들어와
     * '2026대구세계마스터즈 10km대회' 같은 다른 지역 대회가 딸려 온다.
     * 프롬프트로도 막지만 한 번 더 거른다.
     */
    /**
     * 서울·경기 대회인지 본다.
     *
     * 처음에는 대회 '이름' 에 지역 낱말이 있는지로 걸렀는데 두 가지가 어긋났다.
     *   - '2026 인사이더런' 처럼 이름에 지역이 없는 대회가 통째로 빠졌다
     *   - '아식스 서울신문 고프리런' 은 주최사 이름의 '서울' 때문에 통과했다.
     *     실제로 서울 대회가 맞긴 하지만, 맞은 이유가 틀렸다.
     *
     * 그래서 지금은 Gemini 가 검색 요약을 읽고 알려 준 지역 값을 본다.
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
            // 지역을 모르는 대회는 넣지 않는다.
            // 관리자도 어디서 열리는지 모르면 판단할 수 없다.
            return false;
        }
        for (String r : OUR_REGION) {
            if (region.contains(r)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 이름에 박힌 연도가 올해인지 본다.
     *
     * '2025 서울 잠수교 나이트런' 처럼 지난해 대회가 검색에 남아 있다.
     * 연도가 안 적힌 이름은 그대로 통과시킨다. 대회일로 다시 걸러지기 때문이다.
     */
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

        // 뒤에 붙는 말이 달라도 같은 대회인 경우가 많아 떼어 낸다.
        //   "OO 10km대회" 와 "OO 10km마라톤" 은 같은 대회다.
        //
        // 다만 떼고 나서 너무 짧아지면 떼지 않는다.
        // "2026 서울런" 에서 '런' 을 떼면 "서울" 만 남아,
        // 서울이 들어간 제목 전부에 걸려 언급 324회로 세어졌다.
        String cut = key.replaceAll("대회$|마라톤대회$|마라톤$|레이스$|런$", "");
        return cut.length() >= 5 ? cut : key;
    }

    private String mostCommon(List<String> names) {
        Map<String, Integer> count = new LinkedHashMap<>();
        for (String n : names) {
            count.merge(n, 1, Integer::sum);
        }
        return count.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(names.get(0));
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
