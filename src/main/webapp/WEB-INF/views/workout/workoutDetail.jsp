<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON ${facility.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.4">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">

<%-- 목업에는 이용요금·평일·주말·휴관일 칸이 있는데 채울 자료가 없다.
     facilities.hours_manual 은 칸만 있고 1,378곳 모두 비어 있다.
     요금은 programs 에 있지만 강좌마다 붙은 값이라 '이 시설의 요금' 이 아니다.
     지금은 표에 실제로 있는 값만 보여준다. --%>

<main class="auth-shell workout-shell">

    <div class="detail-header">
        <a class="back-button"
           href="${pageContext.request.contextPath}/workout/workoutList?tab=facility&sportId=${sportId}&lat=${lat}&lng=${lng}"
           aria-label="뒤로 가기">&#8249;</a>
    </div>

    <div class="detail-title-area">
        <h1 class="detail-title">${facility.name}</h1>
        <p class="detail-subtext">
            ${facility.guName}
            · ${usingGps ? '현위치' : '서울시청'}에서
            <c:choose>
                <c:when test="${facility.distanceKm lt 1}">
                    <fmt:formatNumber value="${facility.distanceKm * 1000}" maxFractionDigits="0"/>m
                </c:when>
                <c:otherwise>
                    <fmt:formatNumber value="${facility.distanceKm}" maxFractionDigits="1"/>km
                </c:otherwise>
            </c:choose>
        </p>
    </div>

    <div class="info-table-box">
        <div class="info-row">
            <span class="info-label">주소</span>
            <span class="info-value">
                ${not empty facility.roadAddr ? facility.roadAddr : facility.lotAddr}
            </span>
        </div>
        <c:if test="${not empty facility.phone}">
            <div class="info-row">
                <span class="info-label">문의</span>
                <span class="info-value">${facility.phone}</span>
            </div>
        </c:if>
        <c:if test="${facility.capacity ne null}">
            <div class="info-row">
                <span class="info-label">수용 인원</span>
                <span class="info-value">${facility.capacity}명</span>
            </div>
        </c:if>
    </div>

    <%-- 상자로 두르지 않는다.
         경고 상자는 회원이 무언가 해야 할 때 쓰는 것인데, 이건 그냥 단서다.
         정보 칸 오른쪽 끝에 맞춰 두면 위의 값들에 붙은 말로 읽힌다.

         강좌 수도 적지 않는다. 원본이 2025년 9월 자료라 개수마저 지금과 어긋난다.
         '여는 강좌 26개' 라고 적어 놓고 실제로는 다른 강좌가 열려 있으면
         숫자만 정확해 보여서 오히려 잘못 믿게 된다. --%>
    <p class="notice-inline">요금·운영시간은 방문 전 확인해 주세요</p>

    <%-- 시설 위치. 좌표는 모든 시설에 다 있어서 코스와 달리 빠지는 곳이 없다.
         키가 없거나 도메인 등록이 안 됐으면 kakao 가 아예 없으므로 자리표시만 남는다. --%>
    <c:choose>
        <c:when test="${not empty kakaoMapKey}">
            <div id="facilityMap" class="kakao-map"
                 data-lat="${facility.lat}" data-lng="${facility.lng}"
                 data-name="${fn:escapeXml(facility.name)}"></div>
            <%-- 지도 라이브러리.
                 https 를 그대로 적는다. '//' 로 시작하면 페이지가 http 일 때
                 http://dapi.kakao.com 을 부르는데 카카오는 https 만 받아 준다. --%>
            <script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapKey}"></script>
        </c:when>
        <c:otherwise>
            <div class="map-placeholder"><div class="map-pin"></div></div>
        </c:otherwise>
    </c:choose>
    <p class="map-sub-info">
        ${facility.guName}<c:if test="${not empty facility.roadAddr}"> · ${facility.roadAddr}</c:if>
    </p>

    <div class="detail-action-btns">
        <%-- link/map 은 그 자리를 지도에 띄우기만 한다. 단추 이름이 '길찾기' 이므로
             link/to 로 바꿔 도착지를 정해 주고, 출발지는 카카오맵이 현위치로 잡게 한다. --%>
        <button type="button" id="btnFacilityMap" class="btn-outline"
                data-map-url="https://map.kakao.com/link/to/${facility.name},${facility.lat},${facility.lng}">
            길찾기
        </button>
        <%-- 즉시운동은 오늘 가서 쓰는 화면이라 대관·이용 창구로 보낸다.
             수강신청은 추천 탭이 맡는다. --%>
        <c:if test="${not empty useUrl}">
            <button type="button" id="btnFacilitySite" class="btn-primary"
                    data-target-url="${useUrl}">
                이용 안내 보기
            </button>
        </c:if>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<%-- 현위치를 받아 좌표를 달고 같은 주소를 다시 연다. 거리 표시가 여기에 달려 있다. --%>
<script src="${pageContext.request.contextPath}/js/geo.js"></script>
<script src="${pageContext.request.contextPath}/js/workout.js"></script>
</body>
</html>
