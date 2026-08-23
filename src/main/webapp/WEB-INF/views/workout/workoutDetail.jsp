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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.2">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">

<%-- 이 화면은 아직 손보는 중이다.
     목업에는 이용요금·평일·주말·휴관일 칸이 있는데, facilities 표에 그런 칸이 없다.
     요금과 시간은 시설이 아니라 강좌(programs)마다 다르게 붙어 있어서다.
     지금은 표에 실제로 있는 값만 보여준다. 강좌 목록 꾸미기는 다음 차례. --%>

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
        <div class="info-row">
            <span class="info-label">여는 강좌</span>
            <span class="info-value">
                <c:choose>
                    <c:when test="${empty programs}">등록된 강좌가 없어요</c:when>
                    <c:otherwise>${fn:length(programs)}개</c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>

    <div class="notice-alert-box">
        <p class="notice-title">요금·운영시간은 상시 변경될 수 있습니다</p>
        <p class="notice-desc">방문 전 안내 페이지에서 꼭 확인해 주세요</p>
    </div>

    <div class="map-placeholder">
        <div class="map-pin"></div>
    </div>
    <p class="map-sub-info">${facility.guName} · 지도는 준비 중이에요</p>

    <div class="detail-action-btns">
        <button type="button" id="btnFacilityMap" class="btn-outline"
                data-map-url="https://map.kakao.com/link/map/${facility.name},${facility.lat},${facility.lng}">
            길찾기
        </button>
        <button type="button" id="btnFacilitySite" class="btn-primary"
                data-target-url="${not empty facility.homepageUrl ? facility.homepageUrl : facility.districtUrl}">
            안내페이지로 이동
        </button>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<%-- 현위치를 받아 좌표를 달고 같은 주소를 다시 연다. 거리 표시가 여기에 달려 있다. --%>
<script src="${pageContext.request.contextPath}/js/geo.js"></script>
<script src="${pageContext.request.contextPath}/js/workout.js"></script>
</body>
</html>
