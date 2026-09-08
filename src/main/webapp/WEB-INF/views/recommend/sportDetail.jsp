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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/recommend.css?v=1.2">
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
         고른 시설에 따라 아래 안내와 버튼이 바뀌므로 JS 없이 링크로 처리한다.

         강좌가 없는 종목이라고 목록을 감추지는 않는다.
         '이 종목을 어디서 할 수 있나' 가 이 화면의 첫 번째 답이라,
         빌리러 가든 배우러 가든 가까운 곳은 알려 줘야 한다. --%>
    <c:choose>
        <%-- 걷기·등산. 시설이 아니라 코스로 답하는 종목이다.
             즉시운동 탭 '야외에서' 와 같은 서울두드림길 자료를 쓴다.
             예약이라는 절차가 없는 길이라 아래 버튼도 필요 없다. --%>
        <c:when test="${not empty courses}">
            <h2 class="section-title">가까운 코스 ${courses.size()}곳 <small>가까운 순</small></h2>
            <ul class="facility-list">
                <c:forEach var="c" items="${courses}">
                    <li>
                        <%-- 코스 상세는 즉시운동 탭 것을 그대로 쓴다. 같은 자료라 화면을 또 만들 이유가 없다.
                             다만 from 을 달아 준다. 이게 없으면 뒤로가기가 즉시운동 목록으로 가서,
                             추천을 보다가 누른 사람이 엉뚱한 탭에 떨어진다. --%>
                        <a class="facility-item"
                           href="${pageContext.request.contextPath}/workout/courseDetail/${c.courseId}?lat=${lat}&lng=${lng}&from=recommend&sportId=${sport.sportId}">
                            <span class="facility-body">
                                <span class="facility-name">${c.name}</span>
                                <span class="facility-addr">
                                    <c:if test="${c.distanceKm ne null}">
                                        <fmt:formatNumber value="${c.distanceKm}" maxFractionDigits="1"/>km
                                    </c:if>
                                    <c:if test="${c.durationMin ne null}"> · 약 ${c.durationMin}분</c:if>
                                    <c:if test="${not empty c.guName}"> · ${c.guName}</c:if>
                                </span>
                            </span>
                            <span class="facility-km">
                                <c:choose>
                                    <c:when test="${c.distanceFromMe lt 1}">
                                        <fmt:formatNumber value="${c.distanceFromMe * 1000}" maxFractionDigits="0"/>m
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatNumber value="${c.distanceFromMe}" minFractionDigits="1"
                                                          maxFractionDigits="1"/>km
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </a>
                    </li>
                </c:forEach>
            </ul>
            <%-- 코스 길이와 현위치까지의 거리가 나란히 나오면 헷갈린다.
                 왼쪽은 '이 길이 몇 km 인지', 오른쪽 뱃지는 '시작점이 얼마나 먼지'다. --%>
            <p class="data-notice">
                누르면 소요시간과 코스안내를 볼 수 있어요.
            </p>
        </c:when>

        <%-- 이 탭은 신청·예약 창구가 있는 곳만 본다.
             그래서 비었다는 말은 '할 데가 없다' 가 아니라 '신청할 데가 없다' 는 뜻이다.
             동네 공원 코트는 즉시운동 탭에 그대로 있으므로 그리로 보낸다.
             축구는 890쌍 중 851쌍이 그런 곳이라 여기가 비는 일이 드물지 않다. --%>
        <c:when test="${empty facilities and empty rentals}">
            <h2 class="section-title">가까운 시설</h2>
            <div class="empty-box">
                주변에 ${sport.name} 을(를) 신청하거나 예약할 수 있는<br>
                곳을 찾지 못했어요.
            </div>
            <p class="data-notice">
                예약 없이 그냥 쓰는 동네 코트는
                <a href="${pageContext.request.contextPath}/workout/workoutList?tab=facility&sportId=${sport.sportId}&lat=${lat}&lng=${lng}">즉시운동 탭</a>
                에서 볼 수 있어요.
            </p>
        </c:when>

        <c:otherwise>
            <%-- 시설 목록이 비어 있을 수 있다. 아래 대관 목록만 나오는 경우다.
                 서울시 공공서비스예약에는 있는데 공공체육시설 관리대장에는 없는 곳이
                 인재개발원 축구장·서남물재생센터 테니스장처럼 꽤 있다. --%>
            <c:if test="${not empty learnFacilities}">
            <%-- 제목이 이 목록의 성격을 말한다.
                 이 동네에서 이 종목에 강습이 하나도 없으면 배우러 갈 곳이 아니라
                 그냥 이용하러 갈 곳들이므로, 제목부터 그렇게 적는다. --%>
            <h2 class="section-title">
                <%-- '빌릴 수 있는' 이라고 못 박지 않는다. 강습이 없는 시설이라고
                     다 대관은 아니다. 자유수영처럼 표를 끊고 혼자 쓰는 곳이 더 많다.
                     장소를 시간대로 빌리는 것은 아래 대관 목록이 따로 맡는다. --%>
                <c:choose>
                    <c:when test="${rentalMode}">지금 이용할 수 있는 ${learnFacilities.size()}곳</c:when>
                    <c:otherwise>가까운 시설 ${learnFacilities.size()}곳</c:otherwise>
                </c:choose>
            </h2>

            <ul class="facility-list">
                <c:forEach var="f" items="${learnFacilities}">
                    <li>
                        <a class="facility-item ${f.facilityId == pick.facilityId and empty pickPlace ? 'is-pick' : ''}"
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

            <%-- ---------- 강좌 목록은 두지 않는다 ----------
                 원본이 2025년 9월 자료라 지금 열리는 강좌와 너무 어긋났다.
                 요금이 40,000원으로 실려 있는데 홈페이지는 61,000원이고,
                 강좌 이름 자체가 다른 곳도 많았다. 체육 강좌는 분기마다 개편되기 때문이다.

                 요금·시간을 지우고 이름만 남겨 봤지만 이름도 맞지 않았다.
                 틀린 목록을 보여 주느니, 여기서는 '어디로 가면 되는지' 만 말하고
                 실제 강좌는 아래 버튼 너머 신청 페이지에서 보게 한다. --%>
            <c:choose>
                <%-- 예약이라는 절차가 없어서 그냥 가면 되는 곳. 아무 말도 하지 않는다.
                     '그냥 가서 쓰세요' 는 알려 주는 말이 아니라 빈자리를 메우는 말이었다.
                     여기서 할 일은 위치를 알려 주는 것뿐이고 그건 아래 지도 단추가 한다.

                     그래도 이 가지를 지우지는 않는다.
                     지우면 아래 '창구를 못 찾았어요' 로 흘러가는데,
                     개방형 코트는 못 찾은 게 아니라 찾을 것이 없는 곳이다. --%>
                <c:when test="${openAccess}"></c:when>
                <c:when test="${empty linkUrl and empty rentals}">
                    <div class="empty-box">
                        이 시설의 신청 창구를 아직 못 찾았어요.<br>
                        아래 지도로 위치만 확인해 주세요.
                    </div>
                </c:when>
                <%-- 방문 접수만 받는 곳. 버튼 너머에서 신청이 안 되니 미리 말해 준다.
                     노인복지관·사회복지관이 여기 해당한다. --%>
                <c:when test="${visitOnly}">
                    <p class="data-notice">
                        여기는 직접 찾아가 접수하는 곳이에요.</br>
                        <c:choose>
                            <c:when test="${not empty linkUrl}">아래에서 어떤 프로그램이 있는지 먼저 보세요.</c:when>
                            <%-- 온라인 창구가 없으면 남는 답은 전화뿐이다.
                                 목동테니스장은 접수 방법이 아예 '전화문의' 로 적혀 있다.
                                 눌러서 걸 수 있게 tel: 로 건다. --%>
                            <c:when test="${not empty pick.phone}">
                                접수 기간은 <a href="tel:${pick.phone}">${pick.phone}</a> 로 확인해 주세요.
                            </c:when>
                            <c:otherwise>가시기 전에 접수 기간을 확인해 주세요.</c:otherwise>
                        </c:choose>
                    </p>
                </c:when>

                <%-- 안내 문구도 버튼과 같은 이야기를 해야 한다.
                     빌리러 가는 곳에 '여는 강좌' 를 말하면 앞뒤가 어긋난다. --%>
                <c:when test="${not empty linkUrl and toRental}">
                    <p class="data-notice">
                        여기는 시간을 잡아 빌리는 곳이에요.
                        요금과 빈 시간은 아래에서 확인해 주세요.
                    </p>
                </c:when>

                <c:when test="${not empty linkUrl}">
                    <p class="data-notice">
                        여는 강좌와 요금은 자주 바뀌어요.</br>
                        아래에서 지금 열리는 것을 확인해 주세요.
                    </p>
                </c:when>
            </c:choose>

            </c:if>


            <%-- ---------- 대관 ----------
                 우리 시설 목록과 따로 세운다. 붙이려 하지 않는다.
                 서울시 공공서비스예약의 장소 175곳 중 절반 넘게가
                 인재개발원·서남물재생센터·에코파크처럼 공공체육시설 관리대장에 없는 곳이라,
                 이름으로도 좌표로도 우리 시설에 못 붙는다. 억지로 붙이면 옆 시설을 문다.

                 배드민턴·탁구·테니스·농구·축구/풋살 다섯 종목에만 나온다.
                 장소를 시간대로 빌리는 것이 이 다섯뿐이기 때문이다.
                 수영장을 통째로 빌리는 사람은 없고, 자유수영은 위 시설 쪽이 맡는다. --%>
            <c:if test="${not empty rentals}">
                <h2 class="section-title">
                    지금 빌릴 수 있는 ${rentals.size()}곳 <small>가까운 순</small>
                </h2>
                <ul class="facility-list">
                    <c:forEach var="r" items="${rentals}">
                        <li>
                            <%-- 두 줄 다 우리 화면에 머문다.
                                 전에는 서울시 예약 장소를 바로 바깥으로 내보냈는데,
                                 그러면 길찾기를 쓸 기회가 없이 예약 페이지로 튕겨 나갔다.
                                 고르는 것과 나가는 것을 나눠, 나가는 일은 아래 단추가 맡는다. --%>
                            <c:choose>
                                <c:when test="${r.facilityId > 0}">
                                    <c:set var="href"
                                           value="${pageContext.request.contextPath}/recommend/sportDetail/${sport.sportId}?facilityId=${r.facilityId}&lat=${lat}&lng=${lng}"/>
                                    <c:set var="on" value="${r.facilityId == pick.facilityId and empty pickPlace}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="href"
                                           value="${pageContext.request.contextPath}/recommend/sportDetail/${sport.sportId}?place=${r.placeName}&lat=${lat}&lng=${lng}"/>
                                    <c:set var="on" value="${r.placeName eq pickPlace}"/>
                                </c:otherwise>
                            </c:choose>
                            <a class="facility-item ${on ? 'is-pick' : ''}" href="${href}">
                                <span class="facility-body">
                                    <span class="facility-name">${r.placeName}</span>
                                    <span class="facility-addr">
                                        ${r.minClass}<c:if test="${not empty r.payYn}"> · ${r.payYn}</c:if>
                                    </span>
                                </span>
                                <span class="facility-km">
                                    <c:choose>
                                        <c:when test="${r.distanceKm < 1}">
                                            <fmt:formatNumber value="${r.distanceKm * 1000}" maxFractionDigits="0"/>m
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${r.distanceKm}" minFractionDigits="1"
                                                              maxFractionDigits="1"/>km
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </c:if>

            <%-- ---------- 고른 시설의 단추 ----------
                 목록을 다 보여준 뒤 맨 아래에 한 번만 둔다.

                 전에는 '가까운 시설' 블록 안에 있었다. 그러면 자리가 오락가락한다.
                 테니스는 그 목록이 있어 단추가 목록 아래에 붙지만,
                 축구는 강좌 시설이 없어 목록이 통째로 빠지면서
                 단추가 제목 바로 밑으로 올라와 무엇에 대한 단추인지 알 수 없었다.

                 고른 시설이 위 목록에 있을 수도, 아래 대관 목록에 있을 수도 있어
                 어느 한쪽에 붙일 수 없다. 그래서 밖으로 빼고 이름을 함께 적는다. --%>
            <c:if test="${not empty pickName}">
                <%-- 이름은 적지 않는다.
                     고른 줄에 이미 파란 테두리가 들어가 있어 두 번 말하는 셈이고,
                     '난지물재생센터>난지물재생센터 테니스장' 처럼 긴 이름은
                     단추 위에서 두 줄로 접혀 더 어수선해진다. --%>
                <div class="detail-actions">
                    <%-- 길찾기. 도착지를 좌표로 넘긴다.
                         link/to 는 그 자리를 도착지로 놓고 길찾기를 열어 주고,
                         출발지는 카카오맵이 회원의 현위치로 잡는다.

                         이름이 아니라 좌표를 넘기는 것이 중요하다.
                         시설명은 지자체 관리대장 기준이라 지도 검색과 어긋난다.
                         예) '반월공원' 으로 검색하면 26km 떨어진 안산 반월공원이 나온다.
                         여기서 이름은 도착지에 붙는 이름표일 뿐이라 엉뚱한 곳을 찍지 않는다. --%>
                    <a class="outline-button"
                       href="https://map.kakao.com/link/to/${pickName},${pickLat},${pickLng}"
                       target="_blank" rel="noopener">길찾기</a>

                    <c:if test="${not empty linkUrl}">
                        <a class="primary-button" href="${linkUrl}" target="_blank" rel="noopener">${linkLabel}</a>
                    </c:if>
                </div>
            </c:if>
        </c:otherwise>
    </c:choose>

</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp" />

<script src="${pageContext.request.contextPath}/js/geo.js"></script>

</body>
</html>
