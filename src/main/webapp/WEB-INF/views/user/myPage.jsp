<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
                    <%-- 고치는 단추는 두지 않는다. 지우고 다시 넣으면 결과가 같다.
                         번호는 서버가 세션의 회원 번호와 함께 확인한다. --%>
                    <button type="button" class="btn-del-log"
                            data-log-id="${workout.logId}"
                            aria-label="${workout.sportName} 기록 지우기">✕</button>
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
                <span class="streak-count">${streak.current}일째</span>
            </div>
            <%-- 월~일 일곱 칸. 서비스가 이번 주 기록을 보고 채운 값이다.
                 전에는 월·화·수·목이 켜진 채로 박혀 있어서 새 계정도 그렇게 보였다. --%>
            <c:set var="dayNames" value="월,화,수,목,금,토,일" />
            <div class="week-days">
                <c:forEach var="done" items="${streak.week}" varStatus="st">
                    <span class="day-chip ${done ? 'active' : ''}">${fn:split(dayNames, ',')[st.index]}</span>
                </c:forEach>
            </div>
        </section>

        <!-- 하단 메뉴 리스트 -->
        <section class="card menu-list-card">
            <a href="${pageContext.request.contextPath}/user/workoutReport" class="menu-item">
                <span>운동 리포트 보기</span>
                <span class="arrow">›</span>
            </a>
            <a href="${pageContext.request.contextPath}/user/copyright" class="menu-item">
                <span>오픈소스 및 저작권 정보</span>
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
                <%-- 종목과 MET 값을 표에서 가져온다.
                     화면에 적어 두면 종목이 늘어도 안 따라오고, MET 도 표와 어긋난다.
                     실제로 수영이 화면 7.0 · 표 6.0 으로 갈려 있었다. --%>
                <div class="chip-group sport-chips">
                    <c:forEach var="s" items="${sports}" varStatus="st">
                        <button type="button" class="chip ${st.first ? 'active' : ''}"
                                data-met="${s.metValue}">${s.name}</button>
                    </c:forEach>
                </div>
            </div>

            <%-- 값을 data 속성에 싣는다. 글자를 읽어 쓰면 문구를 다듬는 순간
                 조용히 엉뚱한 값이 저장된다. 즉시운동 탭도 이 방식이다. --%>
            <div class="form-group">
                <label>얼마나 했나요?</label>
                <div class="chip-group time-chips">
                    <button type="button" class="chip" data-min="30">30분</button>
                    <button type="button" class="chip active" data-min="60">60분</button>
                    <button type="button" class="chip" data-min="90">90분</button>
                    <button type="button" class="chip" data-min="custom">직접입력</button>
                </div>
                <div class="custom-min-box" id="customMinBox" hidden>
                    <input type="number" id="customMin" min="1" max="600"
                           inputmode="numeric" placeholder="분">
                    <span>분</span>
                </div>
            </div>

            <div class="form-group">
                <label>얼마나 힘들었나요?</label>
                <div class="chip-group intensity-chips">
                    <button type="button" class="chip" data-value="LIGHT">가볍게</button>
                    <button type="button" class="chip active" data-value="MODERATE">적당히</button>
                    <button type="button" class="chip" data-value="HARD">숨차게</button>
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

    // MET 은 종목 칩의 data-met 에 실려 온다. sports 표의 met_value 다.

    // 강도 가중치. 열쇠는 화면 글자가 아니라 칩의 data-value 다
    const INTENSITY_FACTOR = { LIGHT: 0.8, MODERATE: 1.0, HARD: 1.25 };

    /** 고른 강도. 못 고르면 중간 */
    function readIntensity() {
        var chip = document.querySelector(".intensity-chips .chip.active");
        return chip ? chip.dataset.value : "MODERATE";
    }

    /**
     * 고른 시간(분). '직접입력' 을 골랐는데 안 적었으면 0 을 준다.
     * 전에는 '직접입력' 글자에서 숫자를 뽑다가 빈 값이 나와 말없이 60분이 됐다.
     */
    function readDuration() {
        var chip = document.querySelector(".time-chips .chip.active");
        if (!chip) {
            return 60;
        }
        if (chip.dataset.min === "custom") {
            var box = document.getElementById("customMin");
            var n = box ? parseInt(box.value, 10) : NaN;
            return n > 0 ? n : 0;
        }
        return parseInt(chip.dataset.min, 10);
    }

    // 실시간 칼로리 계산
    function calculateModalCalories() {
        // 기본 체중은 위 data-user-weight 한 곳에서만 정한다
        var userWeight = parseFloat(document.body.dataset.userWeight) || 0;

        var sportChip = document.querySelector(".sport-chips .chip.active");
        var intensity = readIntensity();

        var durationMin = readDuration();
        var met = sportChip ? parseFloat(sportChip.dataset.met) : 0;
        if (!(met > 0)) {
            met = 5.0;   // 표에 값이 없는 종목. 중강도로 본다
        }
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
                toggleCustomMin();
                calculateModalCalories();
            });
        });

        // '직접입력' 을 골랐을 때만 입력칸을 보여준다
        function toggleCustomMin() {
            var box = document.getElementById("customMinBox");
            if (!box) {
                return;
            }
            var chip = document.querySelector(".time-chips .chip.active");
            var on = chip && chip.dataset.min === "custom";
            box.hidden = !on;
            if (on) {
                document.getElementById("customMin").focus();
            }
        }

        var customMin = document.getElementById("customMin");
        if (customMin) {
            customMin.addEventListener("input", calculateModalCalories);
        }

        // 기록 지우기.
        // 목록에 위임한다. 항목마다 걸면 '더보기' 로 나중에 드러난 줄은 안 걸린다.
        var todayList = document.getElementById("todayWorkoutList");
        if (todayList) {
            todayList.addEventListener("click", function (e) {
                var btn = e.target.closest(".btn-del-log");
                if (!btn) {
                    return;
                }
                if (!confirm("이 기록을 지울까요?")) {
                    return;
                }
                btn.disabled = true;   // 두 번 눌러 두 번 부르는 것을 막는다

                fetch("${pageContext.request.contextPath}/user/deleteWorkout", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ logId: parseInt(btn.dataset.logId, 10) })
                })
                    .then(function (res) { return res.json(); })
                    .then(function (data) {
                        alert(data.msg);
                        // 지우면 연속 출석·리포트까지 달라지므로 화면을 다시 받는다
                        location.reload();
                    })
                    .catch(function () {
                        btn.disabled = false;
                        alert("서버 오류가 발생했습니다.");
                    });
            });
        }

        // [기록하기] 버튼 이벤트
        var submitBtn = document.querySelector(".btn-modal-submit");
        if (submitBtn) {
            submitBtn.onclick = function () {
                var sportChip = document.querySelector(".sport-chips .chip.active");
                var durationMin = readDuration();

                // '직접입력' 을 골라 놓고 안 적은 경우. 말없이 60분으로 넘기지 않는다
                if (!(durationMin > 0)) {
                    alert("운동 시간을 입력해 주세요.");
                    var box = document.getElementById("customMin");
                    if (box) {
                        box.focus();
                    }
                    return;
                }
                if (!sportChip) {
                    alert("운동 종목을 골라 주세요.");
                    return;
                }

                var calText = document.getElementById("modalCalorieText") ? document.getElementById("modalCalorieText").innerText : "0";
                var caloriesBurned = parseInt(calText.replace(/[^0-9]/g, "")) || 0;
                var memo = document.querySelector("#workoutModal textarea") ? document.querySelector("#workoutModal textarea").value : "";

                // 날짜는 보내지 않는다. 서버가 자기 시계로 오늘을 정한다.
                // 셋(브라우저·서버·DB)이 각자 오늘을 알면 하나만 틀어져도 기록이 샌다.
                var requestData = {
                    sportName: sportChip.innerText.trim(),
                    durationMin: durationMin,
                    intensity: readIntensity(),
                    caloriesBurned: caloriesBurned,
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