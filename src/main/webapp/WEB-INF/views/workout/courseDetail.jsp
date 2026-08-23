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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.2">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">

<main class="auth-shell workout-shell">

    <%-- 목록으로 돌아갈 때 현위치를 이어줘야 거리 표시가 그대로 유지된다.
         history.back() 을 쓰면 주소로 바로 들어온 경우에 갈 곳이 없다. --%>
    <div class="detail-header">
        <a class="back-button"
           href="${pageContext.request.contextPath}/workout/workoutList?tab=outdoor&type=${course.courseType}&lat=${lat}&lng=${lng}"
           aria-label="뒤로 가기">&#8249;</a>
    </div>

    <div class="detail-title-area">
        <h1 class="detail-title">${course.name}</h1>
        <p class="detail-subtext">
            ${course.courseType eq 'HIKE' ? '산길' : '평지'}
            <c:if test="${not empty course.guName}"> · ${course.guName}</c:if>
            <%-- 위치를 못 받았으면 '현위치' 라고 하면 안 된다.
                 서울시청에서 잰 값을 현위치라고 하면 거리가 통째로 거짓말이 된다. --%>
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

    <%-- 코스를 고를 때 가장 먼저 보는 세 가지다.
         '얼마나 걷나 / 얼마나 걸리나 / 힘든가' --%>
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

    <%-- 아래 표는 값이 있는 줄만 그린다.
         지금 자료에는 지하철역·특징이 한 건도 없어서, 자리를 잡아 두면
         빈 줄만 늘어선 표가 된다. --%>
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

    <%-- 상자를 두르지 않는다. 행사 탭 상세의 같은 문구와 모양을 맞춘다.
         상자로 감싸면 '읽어야 하는 경고' 처럼 보이는데,
         실제로는 '자료가 바뀔 수 있다' 는 꼬리말에 가깝다. --%>
    <p class="data-notice">
        코스 상태는 날씨·공사로 달라질 수 있습니다<br>
        비 온 뒤나 겨울에는 미끄러운 구간이 있을 수 있어요
    </p>

    <%-- 코스 모양.

         좌표를 카카오 지도에 얹는다. 선은 점이 넉넉할 때만 긋는다.

         원본 자료는 코스마다 좌표 수가 크게 다르다.
             청계천 길            68개
             서서울호수공원 산책길   23개
         점이 서너 개뿐인 코스를 이으면 공원을 가로지르는 삼각형이 된다.
         지도 위에 있으니 '진짜 저 길' 로 읽혀서, 없는 길을 그리는 셈이 된다.
         그럴 때는 선을 긋지 않고 지나는 자리만 찍는다. --%>
    <c:choose>
        <c:when test="${not empty kakaoMapKey and fn:length(points) >= 2}">
            <div class="course-map">
                <%-- 좌표를 자바스크립트로 넘긴다.
                     [[위도,경도],[위도,경도], …] 모양이라 그대로 읽어 쓰면 된다.
                     line 이 false 면 화면이 선을 긋지 않고 점만 찍는다. --%>
                <div id="courseMap" class="kakao-map"
                     data-loop="${course.loopType eq 'LOOP'}"
                     data-line="${fn:length(points) >= 5}"
                     data-points='[<c:forEach var="pt" items="${points}" varStatus="st"><c:if test="${not st.first}">,</c:if>[${pt.lat},${pt.lng}]</c:forEach>]'></div>

                <%-- 선을 안 그은 코스는 왜 안 그었는지 밝힌다.
                     아무 말 없이 점만 있으면 화면이 덜 만들어진 것처럼 보인다. --%>
                <c:if test="${fn:length(points) < 5}">
                    <p class="map-sub-info">
                        이 코스는 원본에 지점이 ${fn:length(points)}곳만 있어
                        지나는 자리만 표시했어요
                    </p>
                </c:if>

                <%-- 지나는 지점 이름. 같은 이름이 잇달아 나오면 한 번만 적는다. --%>
                <c:set var="prev" value=""/>
                <c:set var="hasName" value="false"/>
                <c:forEach var="pt" items="${points}">
                    <c:if test="${not empty pt.pointName}"><c:set var="hasName" value="true"/></c:if>
                </c:forEach>

                <c:if test="${hasName}">
                    <p class="course-legend">
                        <c:forEach var="pt" items="${points}">
                            <c:if test="${not empty pt.pointName and pt.pointName ne prev}">
                                <span>${pt.pointName}</span>
                                <c:set var="prev" value="${pt.pointName}"/>
                            </c:if>
                        </c:forEach>
                    </p>
                </c:if>
            </div>

            <%-- 지도 라이브러리.
                 https 를 그대로 적는다. '//' 로 시작하면 페이지가 http 일 때
                 http://dapi.kakao.com 을 부르는데 카카오는 https 만 받아 준다. --%>
            <script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapKey}"></script>
        </c:when>

        <c:otherwise>
            <p class="map-sub-info">이 코스는 위치 자료가 아직 없어요</p>
        </c:otherwise>
    </c:choose>

    <%-- 버튼은 하나만 둔다.

         '지도에서 보기' 를 함께 두었는데, 바로 위에 이미 지도가 있어 겹쳤다.
         화면 안 지도로 코스 모양을 보고, 갈 마음이 서면 시작점까지 안내받는다.
         그 둘이면 충분하다.

         길 안내는 코스 '시작점' 을 가리킨다.
         코스는 선이라 중간 지점이 더 가까울 수 있지만, 실제로 가야 하는 곳은 시작점이다.
         입구가 공원 안쪽이나 골목에 있는 코스가 많아 이 버튼이 필요하다.

         link/to 는 카카오맵의 길찾기 화면을 연다.
         출발지는 카카오맵이 알아서 현위치로 잡는다.
         휴대폰에서는 카카오맵 앱이 있으면 앱으로 넘어간다. --%>
    <div class="detail-action-btns">
        <button type="button" id="btnCourseRoute" class="btn-primary"
                data-map-url="https://map.kakao.com/link/to/${fn:replace(course.name, ',', ' ')},${course.startLat},${course.startLng}">
            시작점까지 길 안내
        </button>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<%-- 현위치를 받아 좌표를 달고 같은 주소를 다시 연다. 거리 표시가 여기에 달려 있다. --%>
<script src="${pageContext.request.contextPath}/js/geo.js"></script>
<script src="${pageContext.request.contextPath}/js/workout.js"></script>
</body>
</html>
