<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>즉시 운동하기</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.2">
</head>
<body class="auth-page">
<main class="auth-shell workout-shell">
    <header class="workout-header">
        <h1>즉시 운동하기</h1>
        <p class="workout-subtext">지금 갈 수 있는 곳만 골라드려요 · 강서구 화곡동</p>
    </header>

    <!-- 메인 탭 -->
    <nav class="main-tab-group">
        <a href="?tab=facility" class="main-tab-btn ${currentTab == 'facility' || empty currentTab ? 'is-active' : ''}">시설에서</a>
        <a href="?tab=outdoor" class="main-tab-btn ${currentTab == 'outdoor' ? 'is-active' : ''}">야외에서</a>
        <a href="?tab=home" class="main-tab-btn ${currentTab == 'home' ? 'is-active' : ''}">집에서</a>
    </nav>

    <c:choose>
        <%-- [집에서] 탭 선택 시 --%>
        <c:when test="${currentTab == 'home'}">
            <div class="home-workout-container">
                <!-- 강도 선택 -->
                <div class="select-section">
                    <label class="section-label">강도</label>
                    <div class="option-grid">
                        <button class="option-btn">가볍게</button>
                        <button class="option-btn is-active">적당히</button>
                        <button class="option-btn">숨차게</button>
                    </div>
                </div>

                <!-- 소요시간 선택 -->
                <div class="select-section">
                    <label class="section-label">소요시간</label>
                    <div class="option-grid">
                        <button class="option-btn">10분</button>
                        <button class="option-btn is-active">20분</button>
                        <button class="option-btn">30분</button>
                    </div>
                </div>

                <button class="btn-make-plan">운동 계획 만들기</button>

                <!-- 준비운동 -->
                <div class="routine-group">
                    <span class="routine-title">준비운동 · 3분</span>
                    <c:forEach var="item" items="${prepList}">
                        <div class="routine-card">
                            <span>${item.name}</span>
                            <span class="count-badge">${item.count}</span>
                        </div>
                    </c:forEach>
                </div>

                <!-- 본운동 -->
                <div class="routine-group">
                    <span class="routine-title">본운동 · 14분</span>
                    <c:forEach var="item" items="${mainList}">
                        <div class="routine-card">
                            <span>${item.name}</span>
                            <span class="count-badge">${item.count}</span>
                        </div>
                    </c:forEach>
                </div>

                <!-- 마무리 -->
                <div class="routine-group">
                    <span class="routine-title">마무리 · 3분</span>
                    <c:forEach var="item" items="${coolList}">
                        <div class="routine-card">
                            <span>${item.name}</span>
                            <span class="count-badge">${item.count}</span>
                        </div>
                    </c:forEach>
                </div>

                <!-- 하단 액션 버튼 -->
                <div class="home-action-group">
                    <button type="button" class="btn-sub-action">다시 만들기</button>
                    <a href="${pageContext.request.contextPath}/workout/workoutPlay" class="btn-main-action">운동 시작하기</a>
                </div>
            </div>
        </c:when>

        <%-- [야외에서] / [시설에서] 탭 --%>
        <c:otherwise>
            <div class="sub-tag-group">
                <c:choose>
                    <c:when test="${currentTab == 'outdoor'}">
                        <button class="sub-tag-btn is-active">걷기</button>
                        <button class="sub-tag-btn">등산</button>
                    </c:when>
                    <c:otherwise>
                        <button class="sub-tag-btn is-active">수영</button>
                        <button class="sub-tag-btn">배드민턴</button>
                        <button class="sub-tag-btn">헬스</button>
                        <button class="sub-tag-btn">탁구</button>
                        <button class="sub-tag-btn">테니스</button>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="facility-list">
                <c:choose>
                    <c:when test="${currentTab == 'outdoor'}">
                        <c:forEach var="item" items="${outdoorList}">
                            <a href="${pageContext.request.contextPath}/workout/workoutDetail/${item.id}"
                               class="facility-card ${item.isSelected ? 'is-highlight' : ''}">
                                <div class="card-body">
                                    <h3 class="facility-name">${item.name}</h3>
                                    <p class="trail-info">${item.info1}</p>
                                    <p class="trail-subinfo">${item.info2}</p>
                                </div>
                                <span class="distance-badge">${item.distance}</span>
                            </a>
                        </c:forEach>
                        <button type="button" class="btn-map-view">지도로 코스 10개 보기</button>
                    </c:when>

                    <c:otherwise>
                        <c:forEach var="item" items="${facilityList}">
                            <a href="${pageContext.request.contextPath}/workout/workoutDetail/${item.id}"
                               class="facility-card ${item.isSelected ? 'is-highlight' : ''}">
                                <div class="card-body">
                                    <h3 class="facility-name">${item.name}</h3>
                                    <p class="facility-address">${item.address}</p>
                                </div>
                                <span class="distance-badge">${item.distance}</span>
                            </a>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<script src="${pageContext.request.contextPath}/js/workout.js"></script>
</body>
</html>