<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>${detail.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.2">
</head>
<body class="auth-page">
<main class="auth-shell workout-shell">
    <!-- 상단 뒤로가기 버튼 -->
    <div class="detail-header">
        <button type="button" class="back-btn" onclick="history.back();">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#111" stroke-width="2">
                <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
        </button>
    </div>

    <!-- 제목 및 위치 정보 -->
    <div class="detail-title-area">
        <h1 class="detail-title">${detail.name}</h1>
        <p class="detail-subtext">${detail.category} · ${detail.address} · ${detail.distance}</p>
    </div>

    <!-- 요금/운영시간 요약 카드 -->
    <div class="summary-card-grid">
        <div class="summary-card">
            <span class="label">이용요금</span>
            <strong class="value">${detail.price}</strong>
        </div>
        <div class="summary-card">
            <span class="label">평일</span>
            <strong class="value">${detail.weekdayTime}</strong>
        </div>
        <div class="summary-card">
            <span class="label">주말</span>
            <strong class="value">${detail.weekendTime}</strong>
        </div>
    </div>

    <!-- 상세 안내 테이블 -->
    <div class="info-table-box">
        <div class="info-row">
            <span class="info-label">휴관일</span>
            <span class="info-value">${detail.closedDay}</span>
        </div>
        <div class="info-row">
            <span class="info-label">요금 기준</span>
            <span class="info-value">${detail.priceStandard}</span>
        </div>
        <div class="info-row">
            <span class="info-label">수용 인원</span>
            <span class="info-value">${detail.capacity}</span>
        </div>
        <div class="info-row">
            <span class="info-label">문의</span>
            <span class="info-value">${detail.phone}</span>
        </div>
    </div>

    <!-- 안내 경고 박스 -->
    <div class="notice-alert-box">
        <p class="notice-title">요금·운영시간은 상시 변경될 수 있습니다</p>
        <p class="notice-desc">방문 전 안내 페이지에서 꼭 확인해 주세요</p>
    </div>

    <!-- 지도 영역 (placeholder) -->
    <div class="map-placeholder">
        <div class="map-pin"></div>
    </div>
    <p class="map-sub-info">${detail.subwayInfo}</p>

    <!-- 하단 버튼 2개 -->
    <div class="detail-action-btns">
        <button class="btn-outline">길찾기</button>
        <button class="btn-primary">안내페이지로 이동</button>
    </div>

    <!-- 하단 탭바 공통 인클루드 -->
</main>

<c:set var="active" value="workout"/>
<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>
</body>
</html>