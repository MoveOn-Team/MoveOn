<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>즉시 운동하기</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.7">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">

<c:set var="DIFF_EASY" value="쉬움"/>
<c:set var="DIFF_NORMAL" value="보통"/>
<c:set var="DIFF_HARD" value="어려움"/>

<main class="auth-shell workout-shell">
    <header class="workout-header">
        <h1>즉시 운동하기</h1>
        <p class="workout-subtext">
            지금 갈 수 있는 곳만 골라드려요 ·
            <c:choose>
                <c:when test="${usingGps}">현재 위치 기준</c:when>
                <c:otherwise>서울시청 기준</c:otherwise>
            </c:choose>
        </p>
    </header>


    <nav class="main-tab-group">
        <a href="?tab=facility"
           class="main-tab-btn ${currentTab == 'facility' || empty currentTab ? 'is-active' : ''}">시설에서</a>
        <a href="?tab=outdoor"
           class="main-tab-btn ${currentTab == 'outdoor' ? 'is-active' : ''}">야외에서</a>
        <a href="?tab=home"
           class="main-tab-btn ${currentTab == 'home' ? 'is-active' : ''}">집에서</a>
    </nav>

    <c:choose>
        <c:when test="${currentTab == 'home'}">
            <section class="home-picker" id="homeWorkoutPicker">
                <span class="section-label">강도</span>
                <div class="option-grid" data-home-options="intensity">
                    <button type="button" class="option-btn" data-value="LIGHT">가볍게</button>
                    <button type="button" class="option-btn is-active" data-value="MODERATE">적당히</button>
                    <button type="button" class="option-btn" data-value="HARD">숨차게</button>
                </div>

                <span class="section-label">소요시간</span>
                <div class="option-grid" data-home-options="targetMin">
                    <button type="button" class="option-btn" data-value="10">10분</button>
                    <button type="button" class="option-btn is-active" data-value="20">20분</button>
                    <button type="button" class="option-btn" data-value="30">30분</button>
                </div>

                <button type="button" class="btn-make-plan" id="btnMakeHomePlan">운동 계획 만들기</button>
            </section>

            <section id="workoutPlanResult" class="home-plan-preview is-hidden" aria-live="polite"></section>
        </c:when>

        <c:when test="${currentTab == 'outdoor'}">
            <div class="sub-tag-group">
                <a href="?tab=outdoor&type=WALK"
                   class="sub-tag-btn ${courseType == 'WALK' ? 'is-active' : ''}">평지</a>
                <a href="?tab=outdoor&type=HIKE"
                   class="sub-tag-btn ${courseType == 'HIKE' ? 'is-active' : ''}">산길</a>
            </div>

            <c:choose>
                <c:when test="${empty courses}">
                    <div class="empty-box">
                        가까운 코스를 찾지 못했어요.<br>
                        잠시 후 다시 시도해 주세요.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="facility-list">
                        <c:forEach var="c" items="${courses}" varStatus="st">
                            <a href="${pageContext.request.contextPath}/workout/courseDetail/${c.courseId}"
                               class="facility-card ${st.first ? 'is-highlight' : ''} ${st.index >= 5 ? 'is-folded' : ''}">
                                <div class="card-body">
                                    <h3 class="facility-name">${c.name}</h3>
                                    <p class="trail-info">
                                        <c:if test="${c.distanceKm ne null}">
                                            <fmt:formatNumber value="${c.distanceKm}" maxFractionDigits="1"/>km
                                        </c:if>
                                        <c:if test="${c.durationMin ne null}">
                                            · 약 ${c.durationMin}분
                                        </c:if>
                                        <c:if test="${not empty c.difficulty}">
                                            ·
                                            <c:choose>
                                                <c:when test="${c.difficulty eq 'EASY'}">${DIFF_EASY}</c:when>
                                                <c:when test="${c.difficulty eq 'HARD'}">${DIFF_HARD}</c:when>
                                                <c:otherwise>${DIFF_NORMAL}</c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </p>
                                    <c:if test="${not empty c.loopType or not empty c.subwayInfo}">
                                        <p class="trail-subinfo">
                                            <c:if test="${not empty c.loopType}">
                                                <c:choose>
                                                    <c:when test="${c.loopType eq 'LOOP'}">순환형</c:when>
                                                    <c:otherwise>편도</c:otherwise>
                                                </c:choose>
                                            </c:if>
                                            <c:if test="${not empty c.loopType and not empty c.subwayInfo}"> · </c:if>
                                            <c:if test="${not empty c.subwayInfo}">${c.subwayInfo}</c:if>
                                        </p>
                                    </c:if>
                                </div>

                                <span class="distance-badge">
                                    <c:choose>
                                        <c:when test="${c.distanceFromMe lt 1}">
                                            <fmt:formatNumber value="${c.distanceFromMe * 1000}" maxFractionDigits="0"/>m
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${c.distanceFromMe}" maxFractionDigits="1"/>km
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </a>
                        </c:forEach>
                    </div>

                    <c:if test="${fn:length(courses) > 5}">
                        <button type="button" class="btn-more" data-more>
                            코스 ${fn:length(courses) - 5}개 더보기
                        </button>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </c:when>

        <c:otherwise>
            <div class="sub-tag-scroll" id="sportTagScroll">
                <div class="sub-tag-group">
                    <c:forEach var="s" items="${sports}">
                        <a href="?tab=facility&sportId=${s.sportId}"
                           class="sub-tag-btn ${sportId eq s.sportId ? 'is-active' : ''}">${s.name}</a>
                    </c:forEach>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty facilities}">
                    <div class="empty-box">
                        가까운 곳을 찾지 못했어요.<br>
                        다른 종목을 골라 보세요.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="facility-list">
                        <c:forEach var="f" items="${facilities}" varStatus="st">
                            <a href="${pageContext.request.contextPath}/workout/workoutDetail/${f.facilityId}?sportId=${sportId}"
                               class="facility-card ${st.first ? 'is-highlight' : ''}">
                                <div class="card-body">
                                    <h3 class="facility-name">${f.name}</h3>
                                    <p class="facility-address">${not empty f.roadAddr ? f.roadAddr : f.lotAddr}</p>
                                </div>

                                <span class="distance-badge">   
                                    <c:choose>
                                        <c:when test="${f.distanceKm lt 1}">
                                            <fmt:formatNumber value="${f.distanceKm * 1000}" maxFractionDigits="0"/>m
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${f.distanceKm}" maxFractionDigits="1"/>km
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </a>
                        </c:forEach>
                    </div>

                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<script src="${pageContext.request.contextPath}/js/geo.js"></script>
<script src="${pageContext.request.contextPath}/js/workout.js?v=1.7"></script>
<script>
    // 종목 단추 줄. 넘치는 쪽을 클래스로 알려 css 가 그쪽 끝을 흐린다
    (function () {
        var wrap = document.getElementById("sportTagScroll");
        if (!wrap) {
            return;
        }
        var group = wrap.querySelector(".sub-tag-group");

        function update() {
            var max = group.scrollWidth - group.clientWidth;
            wrap.classList.toggle("fade-left", group.scrollLeft > 1);
            wrap.classList.toggle("fade-right", group.scrollLeft < max - 1);
        }

        // 고른 종목이 화면 밖이면 가운데로 끌어온다. 안 보이면 무엇을 골랐는지 모른다
        var active = group.querySelector(".is-active");
        if (active) {
            var left = active.offsetLeft - (group.clientWidth - active.offsetWidth) / 2;
            group.scrollLeft = Math.max(0, left - group.offsetLeft);
        }

        group.addEventListener("scroll", update, {passive: true});
        window.addEventListener("resize", update);
        update();
    })();
</script>
</body>
</html>
