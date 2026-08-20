<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>내 정보 - MOVE:ON</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">
<main class="auth-shell mypage-shell">

        <header class="page-header">
            <h1 class="page-title">내 정보</h1>
        </header>

        <!-- 프로필 기본 정보 카드 -->
        <section class="card profile-card">
            <div class="profile-main">
                <div class="profile-info">
                    <h2 class="user-name">${userInfo.userName}</h2>
                    <p class="user-sub">${userInfo.email} · ${userInfo.region}</p>
                </div>
                <button type="button" class="btn-edit-profile" id="btnEditProfile">수정</button>
            </div>
        </section>

        <!-- 신체 정보 (키, 몸무게, BMI) -->
        <section class="body-stats-grid">
            <div class="stat-card">
                <span class="stat-label">키</span>
                <span class="stat-value">${userInfo.height}<strong>cm</strong></span>
            </div>
            <div class="stat-card">
                <span class="stat-label">몸무게</span>
                <span class="stat-value">${userInfo.weight}<strong>kg</strong></span>
            </div>
            <div class="stat-card bmi-card">
                <span class="stat-label">BMI</span>
                <span class="stat-value">${userInfo.bmi}</span>
            </div>
        </section>

        <!-- 오늘의 운동 완료 -->
        <section class="card workout-today-card">
            <div class="card-header-row">
                <h3 class="card-title">오늘의 운동 완료</h3>
                <span class="card-date">8월 5일 (수)</span>
            </div>
            <div class="workout-list">
                <div class="workout-item completed">
                    <div class="check-icon">✓</div>
                    <div class="workout-info">
                        <strong>배드민턴 60분</strong>
                        <p>강서구 올림픽체육센터 · 16:00</p>
                    </div>
                </div>
                <div class="workout-item planned">
                    <div class="check-icon empty"></div>
                    <div class="workout-info">
                        <strong>홈트 20분</strong>
                        <p>계획만 생성됨 · 수행하면 자동 기록</p>
                    </div>
                </div>
            </div>
            <button type="button" class="btn-add-workout" id="btnRecordWorkout" onclick="openWorkoutModal()">다른 운동 기록하기  <span>›</span></button>
        </section>

        <!-- 연속 출석 -->
        <section class="card streak-card">
            <div class="card-header-row">
                <h3 class="card-title">연속 출석</h3>
                <span class="streak-count">${userInfo.streakDays}일째</span>
            </div>
            <div class="week-days">
                <span class="day-chip active">월</span>
                <span class="day-chip active">화</span>
                <span class="day-chip active">수</span>
                <span class="day-chip active">목</span>
                <span class="day-chip">금</span>
                <span class="day-chip">토</span>
                <span class="day-chip">일</span>
            </div>
        </section>

        <!-- 하단 메뉴 리스트 -->
        <section class="card menu-list-card">
            <a href="${pageContext.request.contextPath}/user/workoutReport" class="menu-item">
                <span>운동 리포트 보기</span>
                <span class="arrow">›</span>
            </a>
            <a href="#" class="menu-item">
                <span>알림 · 위치 권한 설정</span>
                <span class="arrow">›</span>
            </a>
            <a href="${pageContext.request.contextPath}/user/login" class="menu-item logout-item">
                <span>로그아웃</span>
            </a>
        </section>

</main>

<!-- 운동 기록 모달 -->
    <div class="modal-overlay" id="workoutModal">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="btn-close-modal" onclick="closeWorkoutModal()">✕</button>
                <h2>운동 기록</h2>
                <div style="width: 18px;"></div>
            </div>

            <div class="form-group">
                <label>무슨 운동을 했나요?</label>
                <div class="chip-group sport-chips">
                    <button type="button" class="chip active">헬스</button>
                    <button type="button" class="chip">배드민턴</button>
                    <button type="button" class="chip">수영</button>
                    <button type="button" class="chip">걷기</button>
                    <button type="button" class="chip">테니스</button>
                    <button type="button" class="chip">농구</button>
                    <button type="button" class="chip">골프</button>
                </div>
            </div>

            <div class="form-group">
                <label>얼마나 했나요?</label>
                <div class="chip-group time-chips">
                    <button type="button" class="chip">30분</button>
                    <button type="button" class="chip active">60분</button>
                    <button type="button" class="chip">90분</button>
                    <button type="button" class="chip">직접입력</button>
                </div>
            </div>

            <div class="form-group">
                <label>얼마나 힘들었나요?</label>
                <div class="chip-group intensity-chips">
                    <button type="button" class="chip">가볍게</button>
                    <button type="button" class="chip active">적당히</button>
                    <button type="button" class="chip">숨차게</button>
                </div>
            </div>

            <div class="calorie-box">
                <span>예상 소모 칼로리</span>
                <strong>341 kcal</strong>
            </div>

            <div class="input-card">
                <input type="text" value="2026년 8월 5일 (수)" readonly />
            </div>

            <div class="input-card">
                <textarea rows="2" placeholder="메모 (선택)"></textarea>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="closeWorkoutModal()">취소</button>
                <button type="button" class="btn-modal-submit" onclick="closeWorkoutModal()">기록하기</button>
            </div>
        </div>
    </div>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp" />

<!-- 모달 제어 -->
<script>
    function openWorkoutModal() {
        var modal = document.getElementById("workoutModal");
        if (modal) {
            modal.classList.add("show");
        } else {
            console.error("workoutModal 요소를 찾을 수 없습니다.");
        }
    }

    function closeWorkoutModal() {
        var modal = document.getElementById("workoutModal");
        if (modal) {
            modal.classList.remove("show");
        }
    }

    // 모달 내부 칩 버튼 클릭 이벤트
    document.addEventListener("DOMContentLoaded", function () {
        var chips = document.querySelectorAll(".chip-group .chip");
        chips.forEach(function (chip) {
            chip.addEventListener("click", function () {
                var parent = this.parentElement;
                var groupChips = parent.querySelectorAll(".chip");
                groupChips.forEach(c => c.classList.remove("active"));
                this.classList.add("active");
            });
        });
    });
</script>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>