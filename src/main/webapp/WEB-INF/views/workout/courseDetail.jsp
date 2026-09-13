<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON ${course.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.4">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">

<main class="auth-shell workout-shell">

    <%-- 이 화면은 두 탭이 함께 쓴다. 추천 탭의 걷기·등산에서도 여기로 온다.
         돌아갈 곳을 하나로 박으면 추천을 보던 사람이 즉시운동 목록에 떨어진다.
         history.back() 은 주소로 바로 들어온 경우에 갈 곳이 없다. --%>
    <div class="detail-header">
        <c:choose>
            <c:when test="${fromRecommend}">
                <a class="back-button"
                   href="${pageContext.request.contextPath}/recommend/sportDetail/${sportId}?lat=${lat}&lng=${lng}"
                   aria-label="뒤로 가기">&#8249;</a>
            </c:when>
            <c:otherwise>
                <a class="back-button"
                   href="${pageContext.request.contextPath}/workout/workoutList?tab=outdoor&type=${course.courseType}&lat=${lat}&lng=${lng}"
                   aria-label="뒤로 가기">&#8249;</a>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="detail-title-area">
        <h1 class="detail-title">${course.name}</h1>
        <p class="detail-subtext">
            ${course.courseType eq 'HIKE' ? '산길' : '평지'}
            <c:if test="${not empty course.guName}"> · ${course.guName}</c:if>
            <%-- 서울시청에서 잰 값을 '현위치' 라고 하면 거리가 거짓말이 된다 --%>
            · ${usingGps ? '현위치' : '서울시청'}에서
            <c:choose>
                <c:when test="${course.distanceFromMe lt 1}">
                    <fmt:formatNumber value="${course.distanceFromMe * 1000}" maxFractionDigits="0"/>m
                </c:when>
                <c:otherwise>
                    <fmt:formatNumber value="${course.distanceFromMe}" maxFractionDigits="1"/>km
                </c:otherwise>
            </c:choose>
        </p>
    </div>

    <div class="summary-card-grid">
        <div class="summary-card">
            <span class="label">코스 길이</span>
            <strong class="value">
                <c:choose>
                    <c:when test="${course.distanceKm ne null}">
                        <fmt:formatNumber value="${course.distanceKm}" maxFractionDigits="1"/>km
                    </c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                </c:choose>
            </strong>
        </div>
        <div class="summary-card">
            <span class="label">소요시간</span>
            <strong class="value">
                <c:choose>
                    <c:when test="${course.durationMin ne null}">약 ${course.durationMin}분</c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                </c:choose>
            </strong>
        </div>
        <div class="summary-card">
            <span class="label">난이도</span>
            <strong class="value">
                <c:choose>
                    <c:when test="${course.difficulty eq 'EASY'}">쉬움</c:when>
                    <c:when test="${course.difficulty eq 'HARD'}">어려움</c:when>
                    <c:when test="${course.difficulty eq 'NORMAL'}">보통</c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                </c:choose>
            </strong>
        </div>
    </div>

    <%-- 값이 있는 줄만 그린다. 지하철역·특징은 자료에 한 건도 없어
         자리를 잡아 두면 빈 줄만 늘어선 표가 된다 --%>
    <c:if test="${not empty course.loopType or not empty course.subwayInfo
                  or not empty course.features or not empty course.guName}">
        <div class="info-table-box">
            <c:if test="${not empty course.loopType}">
                <div class="info-row">
                    <span class="info-label">코스 형태</span>
                    <span class="info-value">
                        ${course.loopType eq 'LOOP' ? '순환형 · 출발점으로 돌아옴' : '편도 · 도착점이 다름'}
                    </span>
                </div>
            </c:if>
            <c:if test="${not empty course.guName}">
                <div class="info-row">
                    <span class="info-label">위치</span>
                    <span class="info-value">${course.guName}</span>
                </div>
            </c:if>
            <c:if test="${not empty course.subwayInfo}">
                <div class="info-row">
                    <span class="info-label">가는 길</span>
                    <span class="info-value">${course.subwayInfo}</span>
                </div>
            </c:if>
            <c:if test="${not empty course.features}">
                <div class="info-row">
                    <span class="info-label">특징</span>
                    <span class="info-value">${course.features}</span>
                </div>
            </c:if>
        </div>
    </c:if>

    <%-- 상자를 두르면 경고처럼 보인다. 행사 탭 꼬리말과 모양을 맞춘다 --%>
    <p class="data-notice">
        코스 상태는 날씨·공사로 달라질 수 있습니다<br>
        비 온 뒤나 겨울에는 미끄러운 구간이 있을 수 있어요
    </p>

    <%-- 선은 점이 넉넉할 때만 긋는다. 원본은 코스마다 좌표 수가 68개에서
         스물몇까지 다른데, 서너 개뿐인 코스를 이으면 공원을 가로지르는
         삼각형이 된다. 지도 위라 '진짜 저 길' 로 읽혀 없는 길을 그리는 셈이다 --%>
    <c:choose>
        <c:when test="${not empty kakaoMapKey and fn:length(points) >= 2}">
            <div class="course-map">
                <%-- [[위도,경도], …] 모양으로 넘긴다. line 이 false 면 점만 찍는다 --%>
                <div id="courseMap" class="kakao-map"
                     data-loop="${course.loopType eq 'LOOP'}"
                     data-line="${fn:length(points) >= 5}"
                     data-points='[<c:forEach var="pt" items="${points}" varStatus="st"><c:if test="${not st.first}">,</c:if>[${pt.lat},${pt.lng}]</c:forEach>]'></div>

                <%-- 아무 말 없이 점만 있으면 화면이 덜 만들어진 것처럼 보인다 --%>
                <c:if test="${fn:length(points) < 5}">
                    <p class="map-sub-info">
                        이 코스는 원본에 지점이 ${fn:length(points)}곳만 있어
                        지나는 자리만 표시했어요
                    </p>
                </c:if>

            </div>

            <%-- '//' 로 시작하면 http 페이지에서 http 로 부르는데 카카오는 https 만 받는다 --%>
            <script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapKey}"></script>
        </c:when>

        <c:otherwise>
            <p class="map-sub-info">이 코스는 위치 자료가 아직 없어요</p>
        </c:otherwise>
    </c:choose>

    <%-- 시작점 : 코스 입구까지. 입구가 공원 안쪽이나 골목인 코스가 많다.
         따라가기 : 코스를 훑는다. 경유지가 5개뿐이라 지점을 추려 넣는다.
         둘 다 네이버 앱을 먼저 부르고 없으면 카카오로 간다. --%>
    <div class="detail-action-btns">
        <button type="button" id="btnCourseRoute" class="btn-primary"
                data-naver-url="${startNaverUrl}"
                data-map-url="https://map.kakao.com/link/to/${fn:replace(course.name, ',', ' ')},${course.startLat},${course.startLng}">
            시작점까지 길 안내
        </button>
        <c:if test="${not empty routeUrl}">
            <button type="button" id="btnCourseFollow" class="btn-outline"
                    data-naver-url="${naverUrl}" data-map-url="${routeUrl}">
                코스 따라가기
            </button>
        </c:if>
    </div>
    <c:if test="${not empty routeUrl}">
        <p class="map-sub-info">
            네이버지도 앱이 있으면 음성 안내로, 없으면 카카오맵으로 열려요.<br>
            코스의 주요 지점만 이어 지도앱이 다시 계산한 경로라 실제 산책로와 다를 수 있어요.
        </p>
    </c:if>
</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<%-- 현위치를 받아 좌표를 달고 같은 주소를 다시 연다. 거리 표시가 여기에 달려 있다. --%>
<script src="${pageContext.request.contextPath}/js/geo.js"></script>
<script src="${pageContext.request.contextPath}/js/workout.js"></script>
</body>
</html>
