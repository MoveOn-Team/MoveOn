<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>운동 진행 중</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.2">
</head>
<body class="auth-page">
<main class="auth-shell workout-shell">
    <!-- 상단 헤더 & 진행 바 -->
    <div class="play-header">
        <a href="javascript:history.back()" class="btn-back-arrow">＜</a>
        <span class="play-status-text" id="statusText">동작 3/6 · 남은 14분</span>
        <button type="button" class="btn-pause">일시정지</button>
    </div>

    <!-- 6칸 진행 세그먼트 바 -->
    <div class="progress-bar-group">
        <div class="progress-step is-active"></div>
        <div class="progress-step is-active"></div>
        <div class="progress-step is-active"></div>
        <div class="progress-step"></div>
        <div class="progress-step"></div>
        <div class="progress-step"></div>
    </div>

    <!-- 미디어 비디오/이미지 영역 -->
    <div class="media-box">
        <span class="set-tag-badge">2세트 / 3세트</span>
        <div class="media-placeholder">스쿼트 동작 이미지</div>
    </div>

    <!-- 동작 타이틀 & 정보 -->
    <div class="exercise-info-section">
        <div class="info-left">
            <h2 class="exercise-title" id="exerciseTitle">스쿼트</h2>
            <p class="exercise-desc" id="exerciseDesc">무릎이 발끝을 넘지 않게,<br>천천히 앉았다가 일어나기</p>
            <div class="set-check-group">
                <span class="set-check-item is-done">✓</span>
                <span class="set-check-item is-current">2</span>
                <span class="set-check-item">3</span>
            </div>
        </div>
        <div class="info-right">
            <span class="set-label">이번 세트</span>
            <span class="set-value">15회</span>
            <span class="rest-label">휴식 30초 자동</span>
        </div>
    </div>

    <!-- 다음 동작 안내 카고 -->
    <div class="next-exercise-card">
        <div class="next-text-group">
            <span class="next-title">다음동작</span>
            <span class="next-name">플랭크 · 30초 x 3세트</span>
        </div>
        <span class="next-arrow">＞</span>
    </div>

    <!-- 하단 세트 진행 버튼 -->
    <div class="play-action-group">
        <button type="button" class="btn-skip" id="btnSkip">건너뛰기</button>
        <button type="button" class="btn-complete" id="btnComplete">세트 완료</button>
    </div>

</main>

<c:set var="active" value="workout"/>
<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        let currentStep = 3; // 현재 3번째 세그먼트 채워짐 (총 6단계)
        const maxStep = 6;

        const steps = document.querySelectorAll(".progress-step");
        const btnComplete = document.getElementById("btnComplete");
        const btnSkip = document.getElementById("btnSkip");
        const statusText = document.getElementById("statusText");

        function updateProgress() {
            steps.forEach((step, idx) => {
                if (idx < currentStep) {
                    step.classList.add("is-active");
                } else {
                    step.classList.remove("is-active");
                }
            });

            statusText.innerText = "동작 " + currentStep + "/6 · 남은 " + Math.max(1, (6 - currentStep) * 3) + "분";

            // 진행바가 6칸 모두 완료되었을 때 결과 페이지 이동
            if (currentStep >= maxStep) {
                setTimeout(() => {
                    location.href = "${pageContext.request.contextPath}/workout/workoutResult";
                }, 300);
            }
        }

        btnComplete.addEventListener("click", function () {
            if (currentStep < maxStep) {
                currentStep++;
                updateProgress();
            }
        });

        btnSkip.addEventListener("click", function () {
            if (currentStep < maxStep) {
                currentStep++;
                updateProgress();
            }
        });
    });
</script>
</body>
</html>