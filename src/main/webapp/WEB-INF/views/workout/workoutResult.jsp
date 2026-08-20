<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>오늘의 운동 완료</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.1">
</head>
<body>
<div class="app-shell">
    <div class="result-date">8월 5일 (수)</div>

    <!-- 오늘의 운동 완료 그린 메인 배너 -->
    <div class="result-summary-banner">
        <span class="banner-sub">오늘의 운동 완료</span>
        <h2 class="banner-title">20분 · 96kcal</h2>
    </div>

    <!-- 3열 요약 카드 -->
    <div class="result-stats-grid">
        <div class="stat-card">
            <span class="stat-label">완료동작</span>
            <span class="stat-value">6/6</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">총세트</span>
            <span class="stat-value">16세트</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">연속출석</span>
            <span class="stat-value">4일째</span>
        </div>
    </div>

    <!-- 완료한 루틴 목록 -->
    <div class="result-routine-list">
        <div class="routine-card"><span>목 돌리기</span><span class="count-badge">10회 x 2세트</span></div>
        <div class="routine-card"><span>어깨 스트레칭</span><span class="count-badge">30초 x 2세트</span></div>
        <div class="routine-card"><span>윗몸일으키기</span><span class="count-badge">10회 x 3세트</span></div>
        <div class="routine-card"><span>스쿼트</span><span class="count-badge">15회 x 3세트</span></div>
        <div class="routine-card"><span>플랭크</span><span class="count-badge">30초 x 2세트</span></div>
        <div class="routine-card"><span>햄스트링 스트레칭</span><span class="count-badge">30초 x 2세트</span></div>
    </div>

    <!-- 하단 전환 버튼 -->
    <div class="result-action-group">
        <a href="${pageContext.request.contextPath}/recommend/recommendList" class="btn-sub-action text-center">홈으로</a>
        <a href="${pageContext.request.contextPath}/mypage/workoutReport" class="btn-main-action text-center">내 리포트 보기</a>
    </div>

    <jsp:include page="/WEB-INF/views/common/tabbar.jsp" />
</div>
</body>
</html>