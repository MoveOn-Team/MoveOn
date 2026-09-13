<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="active" value="myPage" scope="request" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>내 정보 - MOVE:ON</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <style>
        /* 로그아웃 버튼 포인트 컬러 (붉은색 계열) */
        .menu-list-card .logout-item span {
            color: #000000 !important;
            font-weight: 600;
        }

        .btn-more-today {
            width: 100%;
            padding: 12px 0;
            background-color: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 12px;
            color: #495057;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            margin-bottom: 12px;
            transition: background-color 0.2s ease;
        }
        .btn-more-today:active {
            background-color: #e9ecef;
        }

        .tab-item.active {
            color: #6c5ce7; /* 또는 메인 브랜드 컬러 */
        }
        .tab-item.active svg,
        .tab-item.active i {
            fill: #6c5ce7;
            stroke: #6c5ce7;
        }

    </style>
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}" data-user-weight="${user.weightKg != null and user.weightKg > 0 ? user.weightKg : 65}">
<main class="auth-shell mypage-shell">

        <header class="page-header">
            <h1 class="page-title">내 정보</h1>
        </header>

        <!-- 프로필 기본 정보 카드 -->
        <section class="card profile-card">
            <div class="profile-main">
                <div class="profile-info">
                    <h2 class="user-name">${user.name}</h2>
                    <p class="user-sub">${user.email}</p>
                </div>
                <a href="${pageContext.request.contextPath}/user/profileEdit" class="btn-edit-profile">수정</a>
            </div>
        </section>

        <!-- 신체 정보 (키, 몸무게, BMI) -->
        <section class="body-stats-grid">
            <div class="stat-card">
                <span class="stat-label">키</span>
                <span class="stat-value">${user.heightCm}<strong>cm</strong></span>
            </div>
            <div class="stat-card">
                <span class="stat-label">몸무게</span>
                <span class="stat-value">${user.weightKg}<strong>kg</strong></span>
            </div>
            <div class="stat-card bmi-card">
                <span class="stat-label">BMI</span>
                <span class="stat-value">${user.calculatedBmi != null ? user.calculatedBmi : user.bmi}</span>
            </div>
        </section>

    <!-- 오늘의 운동 완료 -->
    <section class="card workout-today-card">
        <div class="card-header-row">
            <h3 class="card-title">오늘의 운동 완료</h3>
            <span class="card-date" id="todayCardDate"></span>
        </div>

        <!-- id="todayWorkoutList" 속성 추가 -->
        <div class="workout-list" id="todayWorkoutList">
            <!-- 등록된 운동이 있는 경우 -->
            <c:forEach var="workout" items="${todayWorkoutList}" varStatus="status">
                <div class="workout-item completed" style="${status.index >= 3 ? 'display: none;' : ''}">
                    <div class="check-icon">✓</div>
                    <div class="workout-info">
                        <strong>${workout.sportName} ${workout.durationMin}분</strong>
                        <p>
                            <c:choose>
                                <c:when test="${not empty workout.memo}">
                                    ${workout.memo} · ${workout.caloriesBurned} kcal
                                </c:when>
                                <c:otherwise>
                                    직접 기록함 · ${workout.caloriesBurned} kcal
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:forEach>

            <!-- 등록된 운동이 없는 경우 -->
            <c:if test="${empty todayWorkoutList}">
                <div class="workout-item planned">
                    <div class="check-icon empty"></div>
                    <div class="workout-info">
                        <strong>오늘의 운동을 기록해보세요!</strong>
                        <p>아래 버튼을 눌러 운동을 추가할 수 있습니다.</p>
                    </div>
                </div>
            </c:if>
        </div> <!-- workout-list 닫는 태그 -->

        <!-- 더보기 버튼 -->
        <c:if test="${todayWorkoutList != null && todayWorkoutList.size() > 4}">
            <div style="margin-top: 12px;">
                <button type="button"
                        class="btn-more-today"
                        id="btnMoreTodayWorkouts"
                        onclick="showMoreTodayWorkouts()">더보기</button>
            </div>
        </c:if>

        <button type="button" class="btn-add-workout" id="btnRecordWorkout" onclick="openWorkoutModal()">다른 운동 기록하기 <span>›</span></button>
    </section>

        <!-- 연속 출석 -->
        <section class="card streak-card">
            <div class="card-header-row">
                <h3 class="card-title">연속 출석</h3>
                <span class="streak-count">${streakDays != null ? streakDays : 0}일째</span>
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
                <strong id="modalCalorieText">0 kcal</strong>
            </div>

            <div class="input-card">
                <input type="text" id="modalWorkoutDate" readonly />
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
    let visibleTodayCount = 3;

    function showMoreTodayWorkouts() {
        const btn = document.getElementById('btnMoreTodayWorkouts');
        const items = document.querySelectorAll('#todayWorkoutList .workout-item.completed');
        const totalCount = items.length;
        const step = 3;

        if (!btn || totalCount <= 4) return;

        if (visibleTodayCount >= totalCount) {
            visibleTodayCount = 3;
            items.forEach((item, idx) => {
                item.style.display = idx < 3 ? 'flex' : 'none';
            });
            btn.textContent = '더보기';
            return;
        }

        visibleTodayCount += step;
        items.forEach((item, idx) => {
            if (idx < visibleTodayCount) {
                item.style.display = 'flex';
            }
        });

        if (visibleTodayCount >= totalCount) {
            btn.textContent = '접기 ∧';
        } else {
            btn.textContent = '더보기';
        }
    }

    // 운동 MET 정의
    const SPORT_MET = {
        '헬스': 5.0,
        '배드민턴': 5.5,
        '수영': 7.0,
        '걷기': 3.5,
        '테니스': 6.0,
        '농구': 6.5,
        '골프': 3.5
    };

    // 강도 가중치
    const INTENSITY_FACTOR = {
        '가볍게': 0.8,
        '적당히': 1.0,
        '숨차게': 1.25
    };

    // 실시간 칼로리 계산
    function calculateModalCalories() {
        // 기본 체중은 위 data-user-weight 한 곳에서만 정한다
        var userWeight = parseFloat(document.body.dataset.userWeight) || 0;

        var sportName = document.querySelector(".sport-chips .chip.active") ? document.querySelector(".sport-chips .chip.active").innerText.trim() : "헬스";
        var timeText = document.querySelector(".time-chips .chip.active") ? document.querySelector(".time-chips .chip.active").innerText.trim() : "60분";
        var intensity = document.querySelector(".intensity-chips .chip.active") ? document.querySelector(".intensity-chips .chip.active").innerText.trim() : "적당히";

        var durationMin = parseInt(timeText.replace(/[^0-9]/g, "")) || 60;
        var met = SPORT_MET[sportName] || 5.0;
        var factor = INTENSITY_FACTOR[intensity] || 1.0;

        var calculatedKcal = Math.round((met * 3.5 * userWeight / 200) * durationMin * factor);

        var calElement = document.getElementById("modalCalorieText");
        if (calElement) {
            calElement.innerText = calculatedKcal + " kcal";
        }
    }

    // 오늘 날짜 포맷팅 (2026년 8월 27일 (목))
    function setTodayDate() {
        var today = new Date();
        var year = today.getFullYear();
        var month = today.getMonth() + 1;
        var date = today.getDate();
        var dayNames = ['일', '월', '화', '수', '목', '금', '토'];
        var dayOfWeek = dayNames[today.getDay()];

        var dateStr = year + "년 " + month + "월 " + date + "일 (" + dayOfWeek + ")";
        var dateInput = document.getElementById("modalWorkoutDate");

        if (dateInput) {
            dateInput.value = dateStr;
        }
    }

    // 모달 열기/닫기
    function openWorkoutModal() {
        var modal = document.getElementById("workoutModal");
        if (modal) {
            modal.classList.add("show");
            setTodayDate();
            calculateModalCalories();
        }
    }

    function closeWorkoutModal() {
        var modal = document.getElementById("workoutModal");
        if (modal) modal.classList.remove("show");
    }

    document.addEventListener("DOMContentLoaded", function () {
        // 1. 하단 탭바 활성화 (추가된 코드)
        const currentPath = window.location.pathname;
        const navItems = document.querySelectorAll('.tab-bar .tab-item');

        navItems.forEach(item => {
            const href = item.getAttribute('href');
            if (href && currentPath.includes(href)) {
                item.classList.add('active');
            }
        });

        // 메인 카드 헤더 날짜 세팅
        var todayCardDate = document.getElementById("todayCardDate");
        if (todayCardDate) {
            todayCardDate.innerText = (new Date().getMonth() + 1) + "월 " + new Date().getDate() + "일";
        }

        // 칩 선택 이벤트
        var chips = document.querySelectorAll(".chip-group .chip");
        chips.forEach(function (chip) {
            chip.addEventListener("click", function () {
                var parent = this.parentElement;
                parent.querySelectorAll(".chip").forEach(function (c) { c.classList.remove("active"); });
                this.classList.add("active");
                calculateModalCalories();
            });
        });

        // [기록하기] 버튼 이벤트
        var submitBtn = document.querySelector(".btn-modal-submit");
        if (submitBtn) {
            submitBtn.onclick = function () {
                var sportName = document.querySelector(".sport-chips .chip.active") ? document.querySelector(".sport-chips .chip.active").innerText.trim() : "헬스";
                var timeText = document.querySelector(".time-chips .chip.active") ? document.querySelector(".time-chips .chip.active").innerText.trim() : "60분";
                var intensity = document.querySelector(".intensity-chips .chip.active") ? document.querySelector(".intensity-chips .chip.active").innerText.trim() : "적당히";
                var durationMin = parseInt(timeText.replace(/[^0-9]/g, "")) || 60;

                var calText = document.getElementById("modalCalorieText") ? document.getElementById("modalCalorieText").innerText : "0";
                var caloriesBurned = parseInt(calText.replace(/[^0-9]/g, "")) || 0;
                var memo = document.querySelector("#workoutModal textarea") ? document.querySelector("#workoutModal textarea").value : "";

                var now = new Date();
                var offset = now.getTimezoneOffset() * 60000;
                var todayIso = new Date(now.getTime() - offset).toISOString().substring(0, 10);

                var requestData = {
                    sportName: sportName,
                    durationMin: durationMin,
                    intensity: intensity,
                    caloriesBurned: caloriesBurned,
                    workoutDate: todayIso,
                    memo: memo
                };

                fetch("${pageContext.request.contextPath}/user/recordWorkout", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(requestData)
                })
                    .then(function (response) { return response.json(); })
                    .then(function (data) {
                        alert(data.msg || "운동 기록이 저장되었습니다.");
                        closeWorkoutModal();
                        location.reload();
                    })
                    .catch(function (error) {
                        console.error("Error:", error);
                        alert("운동 기록 저장 중 오류가 발생했습니다.");
                    });
            };
        }
    });
</script>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>