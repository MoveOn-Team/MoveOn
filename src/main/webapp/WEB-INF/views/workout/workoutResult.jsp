<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>오늘의 운동 완료</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.4">
</head>
<body class="auth-page">
<main class="auth-shell workout-shell">
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
        <a href="${pageContext.request.contextPath}/recommend/recommendList" class="btn-sub-action">홈으로</a>
        <%-- 리포트는 내정보 탭 화면이라 /user 아래에 있다. /mypage 는 매핑이 없어 404 였다. --%>
        <a href="${pageContext.request.contextPath}/user/workoutReport" class="btn-main-action">내 리포트 보기</a>
    </div>

</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>
</body>
</html>