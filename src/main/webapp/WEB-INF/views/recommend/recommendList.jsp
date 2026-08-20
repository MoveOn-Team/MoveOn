<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON 맞춤 운동 추천</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/recommend.css?v=1.1">
</head>
<body class="auth-page">
<main class="auth-shell recommend-shell">

    <div class="page-head">
        <h1>맞춤 운동 추천</h1>
        <a class="chip-button" href="${pageContext.request.contextPath}/user/onboarding-page">다시 진단</a>
    </div>

    <%-- ---------- 성향 요약 카드 ----------
         성향 코드를 우리말로 바꾸는 일은 RecommendService.getProfile 이 끝내서 넘겨준다.
         화면에서는 받은 문자열을 그대로 찍기만 한다. --%>
    <c:if test="${not empty profile}">
        <section class="profile-card">
            <h2>${profile.titleTop}<br>${profile.titleBottom}</h2>

            <%-- 이름표는 <b> 로 감싸 흐리게 둔다.
                 가운뎃점을 쓰면 알약마다 6px 씩 더 먹어 네 개가 한 줄에 안 들어갔다. --%>
            <div class="profile-tags">
                <span><b>동반자</b>${profile.companionLabel}</span>
                <span><b>승부욕</b>${profile.competitionLabel}</span>
                <span><b>장소</b>${profile.placeLabel}</span>
                <span><b>강도</b>${profile.intensityLabel}</span>
            </div>

            <p class="profile-foot">
                만 ${profile.age}세
                <c:if test="${not empty profile.bmi}">
                    · BMI <fmt:formatNumber value="${profile.bmi}" maxFractionDigits="1"/> 반영
                </c:if>
                <%-- 위치 권한을 안 주면 동네를 못 받는다. 그때는 어느 좌표로 계산했는지만 밝힌다. --%>
                <c:choose>
                    <c:when test="${not empty profile.regionName}"> · ${profile.regionName} 기준</c:when>
                    <c:when test="${usingGps}"> · 현위치 기준</c:when>
                    <c:otherwise> · 서울시청 기준</c:otherwise>
                </c:choose>
            </p>
        </section>
    </c:if>

    <h2 class="section-title">지속 적합도 TOP 3</h2>

    <c:choose>
        <c:when test="${empty top3}">
            <div class="empty-box">
                추천할 종목을 찾지 못했어요.<br>
                성향 조사를 마쳤는지 확인해 주세요.
            </div>
        </c:when>

        <c:otherwise>
            <ul class="sport-list">
                <c:forEach var="sport" items="${top3}" varStatus="loop">
                    <li>
                        <%-- 현위치를 상세 화면까지 이어줘야 거리 표시가 달라지지 않는다 --%>
                        <a class="sport-card ${loop.first ? 'is-top' : ''}"
                           href="${pageContext.request.contextPath}/recommend/sportDetail/${sport.sportId}?lat=${lat}&lng=${lng}">
                            <span class="rank-badge">${loop.count}위</span>
                            <span class="sport-name">${sport.name}</span>
                            <span class="sport-score">
                                <b>${sport.totalScore}%</b>
                                <small>지속 적합도</small>
                            </span>
                        </a>
                    </li>
                </c:forEach>
            </ul>
        </c:otherwise>
    </c:choose>

</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<script src="${pageContext.request.contextPath}/js/recommend.js"></script>
</body>
</html>
