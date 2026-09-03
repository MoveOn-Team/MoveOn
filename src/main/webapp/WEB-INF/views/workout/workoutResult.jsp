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
<body class="auth-page" data-user-weight="${userWeight}">
<main class="auth-shell workout-shell">
    <div class="result-date">${homePlan.exerciseDate}</div>

    <div class="result-summary-banner">
        <span class="banner-sub">오늘의 운동 완료</span>
        <h2 class="banner-title">${homePlan.totalMin}분 · ${homePlan.totalKcal}kcal</h2>
    </div>

    <div class="result-stats-grid">
        <div class="stat-card">
            <span class="stat-label">완료동작</span>
            <span class="stat-value">${homePlan.completedExerciseCount}/${homePlan.exerciseCount}</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">완료세트</span>
            <span class="stat-value">${homePlan.completedSetCount}세트</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">건너뜀</span>
            <span class="stat-value">${homePlan.skippedSetCount}세트</span>
        </div>
    </div>

    <div class="result-routine-list">
        <c:forEach var="e" items="${homePlan.exercises}">
            <div class="routine-card">
                <span>${e.name}</span>
                <span class="count-badge">${e.volumeLabel}</span>
            </div>
        </c:forEach>
    </div>

    <div class="result-action-group">
        <a href="${pageContext.request.contextPath}/user/myPage" class="btn-sub-action">홈으로</a>
        <a href="${pageContext.request.contextPath}/user/workoutReport" class="btn-main-action">내 리포트 보기</a>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>
</body>
</html>
