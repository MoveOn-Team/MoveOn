<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON ${sport.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/recommend.css?v=1.1">
</head>
<body class="auth-page">
<main class="auth-shell detail-shell">

    <div class="detail-top">
        <a class="back-button" href="${pageContext.request.contextPath}/recommend/recommendList"
           aria-label="뒤로 가기">&#8249;</a>
        <span>지속 적합도 ${sport.rank}위</span>
    </div>

    <h1 class="detail-name">${sport.name}</h1>

    <%-- 회원 성향을 그대로 문장으로 보여준다. 왜 이 종목이 올라왔는지 알려주는 줄이다. --%>
    <c:if test="${not empty profile}">
        <p class="detail-trait">${profile.traitSummary}에 잘 맞아요</p>
    </c:if>

    <div class="stat-row">
        <div class="stat-box">
            <small>지속 적합도</small>
            <b>${sport.totalScore}%</b>
        </div>
        <div class="stat-box">
            <small>강도</small>
            <b>MET <fmt:formatNumber value="${sport.metValue}" minFractionDigits="1" maxFractionDigits="1"/></b>
        </div>
        <%-- 회원 몸무게로 계산한 값이라 사람마다 다르다.
             몸무게는 온보딩에서 필수라 이 화면까지 왔으면 반드시 있다. --%>
        <div class="stat-box">
            <small>30분 소모</small>
            <b><fmt:formatNumber value="${sport.estimatedKcal}" type="number"/>kcal</b>
        </div>
    </div>

    <%-- ---------- 가까운 시설 ----------
         시설을 누르면 facilityId 를 달고 같은 화면을 다시 부른다.
         강좌 목록만 바뀌므로 JS 없이 링크로 처리한다.

         단, 강좌가 없고 대관만 있는 종목(축구·풋살 등)은 시설 목록을 통째로 감춘다.
         시설을 눌러 봐야 어디도 강좌가 없어 헛걸음만 시키기 때문이다.
         그때는 아래 '지금 빌릴 수 있는 곳' 만 보여준다. --%>
    <c:set var="rentalOnly" value="${empty programs and not empty rentals}"/>

    <c:choose>
        <c:when test="${rentalOnly}">
            <%-- 시설 목록을 건너뛴다 --%>
        </c:when>

        <c:when test="${empty facilities}">
            <h2 class="section-title">가까운 시설</h2>
            <div class="empty-box">
                현위치 주변에 ${sport.name} 을(를) 할 수 있는<br>
                시설을 찾지 못했어요.
            </div>
        </c:when>

        <c:otherwise>
            <h2 class="section-title">가까운 시설 ${facilities.size()}곳</h2>

            <ul class="facility-list">
                <c:forEach var="f" items="${facilities}">
                    <li>
                        <a class="facility-item ${f.facilityId == pickId ? 'is-pick' : ''}"
                           href="${pageContext.request.contextPath}/recommend/sportDetail/${sport.sportId}?facilityId=${f.facilityId}&lat=${lat}&lng=${lng}">
                            <span class="facility-body">
                                <span class="facility-name">${f.name}</span>
                                <span class="facility-addr">
                                    <c:choose>
                                        <c:when test="${not empty f.roadAddr}">${f.roadAddr}</c:when>
                                        <c:when test="${not empty f.lotAddr}">${f.lotAddr}</c:when>
                                        <c:otherwise>${f.guName}</c:otherwise>
                                    </c:choose>
                                </span>
                            </span>
                            <span class="facility-km">
                                <c:choose>
                                    <c:when test="${f.distanceKm < 1}">
                                        <fmt:formatNumber value="${f.distanceKm * 1000}" maxFractionDigits="0"/>m
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatNumber value="${f.distanceKm}" minFractionDigits="1"
                                                          maxFractionDigits="1"/>km
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </a>
                    </li>
                </c:forEach>
            </ul>

            <%-- ---------- 선택한 시설의 강좌 ----------
                 공공데이터라 비어 있는 값이 많다. 시작시간 22%, 정원 11%, 요일 14% 가 없다.
                 정원은 0 으로 들어오는 것도 '모름' 이라 함께 걸러낸다. --%>
            <%-- 강좌가 없으면 제목도 빈 상자도 만들지 않는다.
                 아래 '지금 빌릴 수 있는 곳' 이 그 자리를 대신한다. --%>
            <c:if test="${not empty programs}">
                <c:forEach var="f" items="${facilities}">
                    <c:if test="${f.facilityId == pickId}">
                        <h2 class="section-title">
                            ${f.name} 운영 강좌 <small>${programs.size()}개</small>
                        </h2>
                    </c:if>
                </c:forEach>
            </c:if>

            <c:choose>
                <c:when test="${empty programs}">
                    <%-- 강좌도 대관도 없을 때만 안내한다. 화면이 통째로 비면 고장으로 보인다.
                         대관이 있으면 아래 '지금 빌릴 수 있는 곳' 이 그 자리를 대신한다. --%>
                    <c:if test="${empty rentals}">
                        <div class="empty-box">
                            <c:if test="${not empty profile}">만 ${profile.age}세가 </c:if>신청할 수 있는
                            ${sport.name} 강좌가 없어요.<br>
                            다른 시설을 눌러 보세요.
                        </div>
                    </c:if>
                </c:when>

                <c:otherwise>
                    <%-- 강좌가 수십 개인 시설이 있어 처음에는 3개만 보여준다.
                         4번째부터는 is-more 를 달아 숨기고, 더보기 버튼이 그 표시를 걷어낸다. --%>
                    <ul class="program-list" id="programList">
                        <c:forEach var="p" items="${programs}" varStatus="loop">
                            <li class="program-item ${loop.index >= 3 ? 'is-more' : ''}">
                                <span class="program-body">
                                    <span class="program-name">${p.name}</span>
                                    <span class="program-meta">
                                        <c:set var="parts" value="${false}"/>
                                        <c:if test="${not empty p.target}">${p.target}<c:set var="parts" value="${true}"/></c:if>
                                        <c:if test="${not empty p.dayOfWeek}"><c:if test="${parts}"> · </c:if>${p.dayOfWeek}<c:set var="parts" value="${true}"/></c:if>
                                        <c:if test="${not empty p.startTime}"><c:if test="${parts}"> · </c:if>${p.startTime}<c:if test="${not empty p.endTime}">~${p.endTime}</c:if><c:set var="parts" value="${true}"/></c:if>
                                        <c:if test="${not empty p.capacity and p.capacity > 0}"><c:if test="${parts}"> · </c:if>정원 ${p.capacity}명</c:if>
                                    </span>
                                </span>
                                <span class="program-fee ${p.fee == 0 ? 'free' : ''}">
                                    <c:choose>
                                        <c:when test="${empty p.fee}">문의</c:when>
                                        <c:when test="${p.fee == 0}">무료</c:when>
                                        <c:otherwise><fmt:formatNumber value="${p.fee}" type="number"/>원</c:otherwise>
                                    </c:choose>
                                </span>
                            </li>
                        </c:forEach>
                    </ul>

                    <c:if test="${programs.size() > 3}">
                        <button type="button" class="more-button" id="programMore"
                                data-rest="${programs.size() - 3}">
                            강좌 ${programs.size() - 3}개 더 보기
                        </button>
                    </c:if>

                    <%-- 공공데이터를 모아 온 것이라 지금도 열리는 강좌인지는 보장할 수 없다.
                         아래 안내 버튼으로 원본을 확인하도록 유도한다. --%>
                    <p class="data-notice">
                        수집 시점 기준이라 지금과 다를 수 있어요. 신청 전 아래에서 확인해 주세요.
                    </p>
                </c:otherwise>
            </c:choose>

            <%-- ---------- 아래 버튼 ----------
                 길찾기는 선택한 시설 좌표로 카카오맵을 연다.
                 예약·안내 링크는 컨트롤러가 정한다.
                   1순위 강좌 예약 페이지 → 2순위 시설 홈페이지
                 둘 다 없으면 버튼을 만들지 않는다. 실제로 그런 시설이 대부분이다. --%>
            <c:forEach var="f" items="${facilities}">
                <c:if test="${f.facilityId == pickId}">
                    <div class="detail-actions">
                        <%-- 카카오맵 검색 탭으로 보내되 '이름' 이 아니라 '주소' 로 찾는다.
                             시설명은 지자체 관리대장 기준이라 지도 검색과 어긋난다.
                             예) '반월공원' 으로 검색하면 26km 떨어진 안산 반월공원이 나온다.
                             주소는 표본 4곳 모두 제자리를 찾았다. --%>
                        <a class="outline-button"
                           href="https://map.kakao.com/link/search/${not empty f.roadAddr ? f.roadAddr : f.lotAddr}"
                           target="_blank" rel="noopener">지도에서 보기</a>

                        <c:if test="${not empty linkUrl}">
                            <a class="primary-button" href="${linkUrl}" target="_blank" rel="noopener">${linkLabel}</a>
                        </c:if>
                    </div>
                </c:if>
            </c:forEach>
        </c:otherwise>
    </c:choose>

    <%-- ---------- 대관 가능한 곳 ----------
         강좌가 없을 때 대신 보여준다.
         축구·풋살은 강좌 185개 중 성인 대상이 30개뿐이고,
         원래 '수강' 이 아니라 '구장 대관' 으로 하는 종목이기 때문이다.

         시설 목록 바깥에 둔다. 강좌가 없어 시설 목록을 감춘 경우에도 이건 보여야 한다.
         서울시 공공서비스예약 자료이고 누르면 그 예약 페이지로 바로 간다. --%>
    <c:if test="${empty programs and not empty rentals}">
        <h2 class="section-title">지금 빌릴 수 있는 곳 <small>가까운 순</small></h2>

        <ul class="facility-list">
            <c:forEach var="r" items="${rentals}">
                <li>
                    <a class="facility-item" href="${r.svcUrl}" target="_blank" rel="noopener">
                        <span class="facility-body">
                            <span class="facility-name">${r.placeName}</span>
                            <span class="facility-addr">
                                ${r.minClass}<c:if test="${not empty r.payYn}"> · ${r.payYn}</c:if>
                            </span>
                        </span>
                        <span class="facility-km">
                            <fmt:formatNumber value="${r.distanceKm}" minFractionDigits="1"
                                              maxFractionDigits="1"/>km
                        </span>
                    </a>
                </li>
            </c:forEach>
        </ul>

        <p class="data-notice">누르면 서울시 공공서비스예약으로 이동해요.</p>
    </c:if>

</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp" />

<script src="${pageContext.request.contextPath}/js/recommend.js"></script>
<script>
    // 강좌 더보기 : 숨겨둔 항목의 표시만 걷어낸다. 서버를 다시 부르지 않는다.
    (function () {
        var button = document.getElementById("programMore");
        if (!button) return;

        button.addEventListener("click", function () {
            document.querySelectorAll("#programList .is-more")
                .forEach(function (li) { li.classList.remove("is-more"); });
            button.remove();
        });
    })();
</script>
</body>
</html>
