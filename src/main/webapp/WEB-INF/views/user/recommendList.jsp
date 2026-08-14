<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>맞춤 운동 추천 - MoveOn</title>
    <!-- 기존 auth.css 연결 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<div class="auth-page recommend-page">
    <div class="auth-shell">

        <!-- 1. 상단 헤더 (페이지 타이틀 & 성향 재진단 버튼) -->
        <div class="recommend-header">
            <h1>맞춤 운동 추천</h1>
            <a href="${pageContext.request.contextPath}/user/onboarding" class="btn-re-diagnose">성향 재진단</a>
        </div>

        <!-- 2. 온보딩 유저 성향 요약 카드 (네이비 카드) -->
        <div class="user-type-card">
            <h2 class="card-title">나에게 딱 맞는<br>지속 가능한 운동은?</h2>

            <div class="tag-group">
                <span class="tag">동반자 · 혼자</span>
                <span class="tag">승부욕 · 낮음</span>
                <span class="tag">장소 · 실내</span>
                <span class="tag">강도 · 중</span>
            </div>

            <p class="user-meta">
                신체 및 위치 정보(BMI 등)가 맞춤 분석에 반영되었습니다.
            </p>
        </div>

        <!-- 3. 지속 적합도 TOP 3 추천 목록 -->
        <div class="recommend-section">
            <h2 class="recommend-section-title">지속 적합도 TOP 3</h2>

            <div class="top3-list">
                <c:forEach var="sport" items="${top3}" varStatus="status">
                    <!-- 1위 카드는 rank-1 클래스로 시각적 강조 적용 -->
                    <div class="rank-card ${status.count == 1 ? 'rank-1' : ''}"
                         onclick="location.href='${pageContext.request.contextPath}/recommend/${sport.sportId}'">

                        <div class="rank-left">
                            <span class="rank-badge">${status.count}위</span>
                            <span class="sport-name"><c:out value="${sport.name}"/></span>
                        </div>

                        <div class="score-box">
                            <span class="score-value"><c:out value="${sport.totalScore}"/>%</span>
                            <span class="score-label">지속 적합도</span>
                        </div>
                    </div>
                </c:forEach>

                <!-- 추천 목록이 비어있을 경우 예외 처리 -->
                <c:if test="${empty top3}">
                    <div style="text-align: center; padding: 40px 0; color: #9a9ea5; font-size: 14px;">
                        추천할 종목 데이터가 없습니다.
                    </div>
                </c:if>
            </div>
        </div>

        <!-- 4. 하단 4개 탭 네비게이션 -->
        <nav class="bottom-nav">
            <a href="${pageContext.request.contextPath}/recommend" class="nav-item active">
                <span class="nav-icon">🎯</span>
                <span>맞춤 추천</span>
            </a>
            <a href="#" class="nav-item">
                <span class="nav-icon">⚡</span>
                <span>즉시 운동</span>
            </a>
            <a href="#" class="nav-item">
                <span class="nav-icon">🎪</span>
                <span>행사</span>
            </a>
            <a href="#" class="nav-item">
                <span class="nav-icon">👤</span>
                <span>내 정보</span>
            </a>
        </nav>

    </div>
</div>

<!-- 기존 auth.js 연결 -->
<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>