<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>운동 진행 중</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.4">
</head>
<body class="auth-page"
      data-context-path="${pageContext.request.contextPath}"
      data-user-weight="${userWeight}">
<main class="auth-shell workout-shell workout-play-shell" id="homeWorkoutPlay">
    <div class="play-header">
        <a href="${pageContext.request.contextPath}/workout/workoutList?tab=home" class="back-button" aria-label="뒤로 가기">&#8249;</a>
        <span class="play-status-text" id="statusText">
            <c:choose>
                <c:when test="${not empty homePlan}">동작 1/6 · 남은 ${homePlan.totalMin}분</c:when>
                <c:otherwise>운동 준비 중...</c:otherwise>
            </c:choose>
        </span>
    </div>

    <div class="progress-bar-group" id="progressBarGroup" role="progressbar" aria-label="전체 운동 진행률"></div>

    <div class="media-box" id="exerciseMedia">
        <span class="set-tag-badge" id="setTagBadge">-세트 / -세트</span>
        <div class="media-placeholder" id="mediaPlaceholder">
            <span class="placeholder-text">동작 이미지</span>
        </div>
    </div>

    <div class="exercise-info-section">
        <div class="info-left">
            <h2 class="exercise-title" id="exerciseTitle">운동 준비</h2>
            <p class="exercise-desc" id="exerciseDesc">곧 운동을 시작합니다.</p>
            <div class="set-check-group" id="setCheckGroup"></div>
        </div>
        <div class="info-right">
            <span class="set-label">이번 세트</span>
            <span class="set-value" id="setValue">-</span>
        </div>
    </div>

    <div class="next-exercise-card" id="nextExerciseCard" role="button" tabindex="0">
        <div class="next-text-group">
            <span class="next-title">다음동작</span>
            <span class="next-name" id="nextExerciseName">-</span>
        </div>
        <span class="next-arrow" aria-hidden="true">›</span>
    </div>

    <div class="play-action-group">
        <button type="button" class="btn-skip" id="btnSkip">건너뛰기</button>
        <button type="button" class="btn-complete" id="btnComplete">세트 완료</button>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<script src="${pageContext.request.contextPath}/js/workout.js?v=1.4"></script>
</body>
</html>
