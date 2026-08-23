<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>즉시 운동하기</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.2">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}"
      data-lat="${lat}" data-lng="${lng}">

<%-- 난이도·순환형은 표에 영문 코드로 들어 있다. 화면에 그대로 쓸 수 없어 여기서 바꾼다. --%>
<c:set var="DIFF_EASY" value="쉬움"/>
<c:set var="DIFF_NORMAL" value="보통"/>
<c:set var="DIFF_HARD" value="어려움"/>

<main class="auth-shell workout-shell">
    <header class="workout-header">
        <h1>즉시 운동하기</h1>
        <p class="workout-subtext">
            지금 갈 수 있는 곳만 골라드려요 ·
            <c:choose>
                <c:when test="${usingGps}">현위치 기준</c:when>
                <c:otherwise>서울시청 기준</c:otherwise>
            </c:choose>
        </p>
    </header>

    <%-- 탭을 옮겨도 현위치는 그대로 이어져야 한다.
         안 이어주면 GPS 로 보던 사람이 탭 한 번에 서울시청 기준으로 떨어진다. --%>
    <c:set var="pos" value="&lat=${lat}&lng=${lng}"/>

    <nav class="main-tab-group">
        <a href="?tab=facility${pos}"
           class="main-tab-btn ${currentTab == 'facility' || empty currentTab ? 'is-active' : ''}">시설에서</a>
        <a href="?tab=outdoor${pos}"
           class="main-tab-btn ${currentTab == 'outdoor' ? 'is-active' : ''}">야외에서</a>
        <a href="?tab=home${pos}"
           class="main-tab-btn ${currentTab == 'home' ? 'is-active' : ''}">집에서</a>
    </nav>

    <c:choose>

        <%-- ========================= 집에서 ========================= --%>
        <c:when test="${currentTab == 'home'}">
            <%-- 아직 동작 자료가 없다. 계획을 만들 재료가 없으므로
                 되는 척 보여주지 않고 지금 상태를 그대로 알린다. --%>
            <div class="empty-box">
                집에서 하는 운동은 준비 중이에요.<br>
                먼저 '시설에서' 와 '야외에서' 를 써 보세요.
            </div>
        </c:when>

        <%-- ========================= 야외에서 ========================= --%>
        <c:when test="${currentTab == 'outdoor'}">

            <%-- '걷기 / 등산' 이 아니라 '평지 / 산길' 이다.
                 여기 있는 코스는 서울두드림길, 곧 '걸어서 즐기는 길' 자료라
                 본격 등산로가 아니라 동네 뒷산을 도는 자락길이다.
                 '등산' 이라고 하면 장비를 챙겨야 하는 산행으로 읽혀 실제와 어긋난다. --%>
            <div class="sub-tag-group">
                <a href="?tab=outdoor&type=WALK${pos}"
                   class="sub-tag-btn ${courseType == 'WALK' ? 'is-active' : ''}">평지</a>
                <a href="?tab=outdoor&type=HIKE${pos}"
                   class="sub-tag-btn ${courseType == 'HIKE' ? 'is-active' : ''}">산길</a>
            </div>

            <c:choose>
                <c:when test="${empty courses}">
                    <div class="empty-box">
                        가까운 코스를 찾지 못했어요.<br>
                        잠시 뒤 다시 열어 주세요.
                    </div>
                </c:when>

                <c:otherwise>
                    <%-- 처음에는 다섯 개만 보여준다.
                         스무 개를 한 번에 늘어놓으면 '지금 갈 곳' 을 고르는 화면이
                         스크롤부터 해야 하는 목록이 된다.
                         나머지는 이미 받아 왔으니 '더보기' 는 서버에 다시 묻지 않는다. --%>
                    <div class="facility-list">
                        <c:forEach var="c" items="${courses}" varStatus="st">

                            <%-- 가장 가까운 한 곳만 테두리로 표시한다.
                                 이미 거리순이라 맨 위가 가장 가까운 코스다. --%>
                            <a href="${pageContext.request.contextPath}/workout/courseDetail/${c.courseId}?lat=${lat}&lng=${lng}"
                               class="facility-card ${st.first ? 'is-highlight' : ''} ${st.index >= 5 ? 'is-folded' : ''}">
                                <div class="card-body">
                                    <h3 class="facility-name">${c.name}</h3>

                                    <%-- 길이 · 시간 · 난이도.
                                         셋 다 값이 있는 자료만 들어 있지만, 없을 때를 대비해 하나씩 본다. --%>
                                    <p class="trail-info">
                                        <c:if test="${c.distanceKm ne null}">
                                            <fmt:formatNumber value="${c.distanceKm}" maxFractionDigits="1"/>km
                                        </c:if>
                                        <c:if test="${c.durationMin ne null}">
                                            · 약 ${c.durationMin}분
                                        </c:if>
                                        <c:if test="${not empty c.difficulty}">
                                            ·
                                            <c:choose>
                                                <c:when test="${c.difficulty eq 'EASY'}">${DIFF_EASY}</c:when>
                                                <c:when test="${c.difficulty eq 'HARD'}">${DIFF_HARD}</c:when>
                                                <c:otherwise>${DIFF_NORMAL}</c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </p>

                                    <%-- 순환형 여부와 이어지는 지하철역.
                                         지금 자료에는 지하철역이 하나도 없다.
                                         그래서 값이 있을 때만 그린다. 없는데 자리를 잡아 두면
                                         "· " 같은 구분점만 덩그러니 남는다. --%>
                                    <c:if test="${not empty c.loopType or not empty c.subwayInfo}">
                                        <p class="trail-subinfo">
                                            <c:if test="${not empty c.loopType}">
                                                ${c.loopType eq 'LOOP' ? '순환형' : '편도'}
                                            </c:if>
                                            <c:if test="${not empty c.loopType and not empty c.subwayInfo}"> · </c:if>
                                            <c:if test="${not empty c.subwayInfo}">${c.subwayInfo}</c:if>
                                        </p>
                                    </c:if>
                                </div>

                                <span class="distance-badge">
                                    <c:choose>
                                        <c:when test="${c.distanceFromMe lt 1}">
                                            <fmt:formatNumber value="${c.distanceFromMe * 1000}"
                                                              maxFractionDigits="0"/>m
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${c.distanceFromMe}"
                                                              maxFractionDigits="1"/>km
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </a>
                        </c:forEach>
                    </div>

                    <c:if test="${fn:length(courses) > 5}">
                        <button type="button" class="btn-more" data-more>
                            코스 ${fn:length(courses) - 5}개 더보기
                        </button>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </c:when>

        <%-- ========================= 시설에서 ========================= --%>
        <c:otherwise>

            <%-- 종목 단추는 표에서 가져온다. 화면에 이름을 박아 두면 종목이 늘어도 화면이 모른다. --%>
            <div class="sub-tag-group">
                <c:forEach var="s" items="${sports}">
                    <a href="?tab=facility&sportId=${s.sportId}${pos}"
                       class="sub-tag-btn ${sportId eq s.sportId ? 'is-active' : ''}">${s.name}</a>
                </c:forEach>
            </div>

            <c:choose>
                <c:when test="${empty facilities}">
                    <div class="empty-box">
                        가까운 곳을 찾지 못했어요.<br>
                        다른 종목을 골라 보세요.
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="facility-list">
                        <c:forEach var="f" items="${facilities}" varStatus="st">
                            <a href="${pageContext.request.contextPath}/workout/workoutDetail/${f.facilityId}?sportId=${sportId}&lat=${lat}&lng=${lng}"
                               class="facility-card ${st.first ? 'is-highlight' : ''} ${st.index >= 5 ? 'is-folded' : ''}">
                                <div class="card-body">
                                    <h3 class="facility-name">${f.name}</h3>
                                    <%-- 도로명주소가 없는 시설이 여섯 곳 있다. 그때는 지번주소를 쓴다. --%>
                                    <p class="facility-address">
                                        ${not empty f.roadAddr ? f.roadAddr : f.lotAddr}
                                    </p>
                                </div>

                                <span class="distance-badge">
                                    <c:choose>
                                        <c:when test="${f.distanceKm lt 1}">
                                            <fmt:formatNumber value="${f.distanceKm * 1000}"
                                                              maxFractionDigits="0"/>m
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${f.distanceKm}"
                                                              maxFractionDigits="1"/>km
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </a>
                        </c:forEach>
                    </div>

                    <c:if test="${fn:length(facilities) > 5}">
                        <button type="button" class="btn-more" data-more>
                            ${fn:length(facilities) - 5}곳 더보기
                        </button>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>

</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<%-- 현위치를 받아 좌표를 달고 같은 주소를 다시 연다. 거리 표시가 여기에 달려 있다. --%>
<script src="${pageContext.request.contextPath}/js/geo.js"></script>
<script src="${pageContext.request.contextPath}/js/workout.js"></script>
</body>
</html>
