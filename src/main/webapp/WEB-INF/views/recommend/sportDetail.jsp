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
         고른 시설에 따라 아래 안내와 버튼이 바뀌므로 JS 없이 링크로 처리한다.

         강좌가 없는 종목이라고 목록을 감추지는 않는다.
         '이 종목을 어디서 할 수 있나' 가 이 화면의 첫 번째 답이라,
         빌리러 가든 배우러 가든 가까운 곳은 알려 줘야 한다. --%>
    <c:choose>
        <%-- 우리 시설 자료에 없을 때만 서울시 공공서비스예약을 뒤져 대신 보여준다.
             그것도 없으면 그때 못 찾았다고 말한다. --%>
        <c:when test="${empty facilities and empty rentals}">
            <h2 class="section-title">가까운 시설</h2>
            <div class="empty-box">
                현위치 주변에 ${sport.name} 을(를) 할 수 있는<br>
                시설을 찾지 못했어요.
            </div>
        </c:when>

        <c:when test="${empty facilities}">
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
        </c:when>

        <c:otherwise>
            <%-- 제목이 이 목록의 성격을 말한다.
                 이 동네에서 이 종목에 강습이 하나도 없으면 배우러 갈 곳이 아니라
                 빌리러 갈 곳들이므로, 제목부터 그렇게 적는다. --%>
            <h2 class="section-title">
                <c:choose>
                    <c:when test="${rentalMode}">지금 빌릴 수 있는 ${facilities.size()}곳</c:when>
                    <c:otherwise>가까운 시설 ${facilities.size()}곳</c:otherwise>
                </c:choose>
            </h2>

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

            <%-- ---------- 강좌 목록은 두지 않는다 ----------
                 원본이 2025년 9월 자료라 지금 열리는 강좌와 너무 어긋났다.
                 요금이 40,000원으로 실려 있는데 홈페이지는 61,000원이고,
                 강좌 이름 자체가 다른 곳도 많았다. 체육 강좌는 분기마다 개편되기 때문이다.

                 요금·시간을 지우고 이름만 남겨 봤지만 이름도 맞지 않았다.
                 틀린 목록을 보여 주느니, 여기서는 '어디로 가면 되는지' 만 말하고
                 실제 강좌는 아래 버튼 너머 신청 페이지에서 보게 한다. --%>
            <c:choose>
                <%-- 예약이라는 절차가 없어서 그냥 가면 되는 곳.
                     '없다' 고만 하면 자료가 빠진 것처럼 읽힌다. --%>
                <c:when test="${openAccess}">
                    <div class="empty-box">
                        예약 없이 바로 쓸 수 있는 곳이에요.<br>
                        가서 비어 있으면 이용하시면 됩니다.
                    </div>
                </c:when>
                <c:when test="${empty linkUrl and empty rentals}">
                    <div class="empty-box">
                        이 시설의 신청 창구를 아직 못 찾았어요.<br>
                        아래 지도로 위치만 확인해 주세요.
                    </div>
                </c:when>
                <%-- 안내 문구도 버튼과 같은 이야기를 해야 한다.
                     빌리러 가는 곳에 '여는 강좌' 를 말하면 앞뒤가 어긋난다. --%>
                <c:when test="${not empty linkUrl and toRental}">
                    <p class="data-notice">
                        여기는 빌려서 쓰는 곳이에요.
                        요금과 빈 시간은 아래에서 확인해 주세요.
                    </p>
                </c:when>
                <c:when test="${not empty linkUrl}">
                    <p class="data-notice">
                        여는 강좌와 요금은 자주 바뀌어요.
                        아래에서 지금 열리는 것을 확인해 주세요.
                    </p>
                </c:when>
            </c:choose>

            <%-- ---------- 아래 버튼 ----------
                 지도에서 보기는 고른 시설 주소로 카카오맵을 연다.
                 예약·안내 링크는 컨트롤러가 네 순위로 정해 linkUrl 에 담아 준다. --%>
            <c:if test="${not empty pick}">
                <div class="detail-actions">
                    <%-- 카카오맵 검색 탭으로 보내되 '이름' 이 아니라 '주소' 로 찾는다.
                         시설명은 지자체 관리대장 기준이라 지도 검색과 어긋난다.
                         예) '반월공원' 으로 검색하면 26km 떨어진 안산 반월공원이 나온다.
                         주소는 표본 4곳 모두 제자리를 찾았다. --%>
                    <a class="outline-button"
                       href="https://map.kakao.com/link/search/${not empty pick.roadAddr ? pick.roadAddr : pick.lotAddr}"
                       target="_blank" rel="noopener">지도에서 보기</a>

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
