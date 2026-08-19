<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>운동 리포트 - MOVE:ON</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app-container">
  <main class="app-shell report-shell">

    <!-- 상단 헤더 (뒤로가기) -->
    <header class="page-header nav-header">
      <a href="javascript:history.back()" class="btn-back">‹</a>
      <h1 class="page-title">운동 리포트</h1>
      <div style="width: 24px;"></div>
    </header>

    <!-- 이번주 요약 하이라이트 카톡 -->
    <section class="report-banner-card">
      <span class="banner-sub">이번주 (8/3 ~ 8/9)</span>
      <h2 class="banner-title">${weeklyCount}회 · ${weeklyMinutes}분</h2>
      <p class="banner-desc">지난주보다 1회 늘었어요</p>
    </section>

    <!-- 3종 실적 통계 -->
    <section class="body-stats-grid">
      <div class="stat-card center">
        <span class="stat-label">소모 칼로리</span>
        <span class="stat-value">${totalBurnedKcal}<strong>kcal</strong></span>
      </div>
      <div class="stat-card center">
        <span class="stat-label">연속 출석</span>
        <span class="stat-value">${streakDays}<strong>일</strong></span>
      </div>
      <div class="stat-card center">
        <span class="stat-label">누적 기록</span>
        <span class="stat-value">${totalRecords}<strong>회</strong></span>
      </div>
    </section>

    <!-- 최근 8주 운동 횟수 차트 -->
    <section class="card chart-card">
      <h3 class="card-title">최근 8주 운동 횟수</h3>
      <div class="bar-chart-container">
        <div class="bar-group"><div class="bar" style="height: 30%;"></div><span>6/15</span></div>
        <div class="bar-group"><div class="bar" style="height: 50%;"></div><span>6/22</span></div>
        <div class="bar-group"><div class="bar" style="height: 20%;"></div><span>6/29</span></div>
        <div class="bar-group"><div class="bar" style="height: 70%;"></div><span>7/6</span></div>
        <div class="bar-group"><div class="bar" style="height: 55%;"></div><span>7/13</span></div>
        <div class="bar-group"><div class="bar" style="height: 90%;"></div><span>7/20</span></div>
        <div class="bar-group"><div class="bar" style="height: 60%;"></div><span>7/27</span></div>
        <div class="bar-group active"><div class="bar" style="height: 80%;"></div><span>이번</span></div>
      </div>
    </section>

    <!-- 많이 한 종목 순위 -->
    <section class="card rank-card">
      <h3 class="card-title">많이 한 종목</h3>
      <ul class="rank-list">
        <li><span class="rank-name">헬스</span><span class="rank-value">16회 · 42%</span></li>
        <li><span class="rank-name">걷기</span><span class="rank-value">11회 · 29%</span></li>
        <li><span class="rank-name">홈트</span><span class="rank-value">8회 · 21%</span></li>
        <li><span class="rank-name">수영</span><span class="rank-value">3회 · 8%</span></li>
      </ul>
    </section>

    <!-- 기록 통계 상세 -->
    <section class="card detail-stats-card">
      <div class="detail-row">
        <span>가장 오래한 운동</span>
        <strong>등산 · 150분</strong>
      </div>
      <div class="detail-row">
        <span>최장 연속 출석</span>
        <strong>12일</strong>
      </div>
      <div class="detail-row">
        <span>첫 기록</span>
        <strong>2026년 5월 3일</strong>
      </div>
    </section>

  </main>

  <!-- 공통 하단 탭바 -->
  <c:set var="active" value="myPage" />
  <jsp:include page="/WEB-INF/views/common/tabbar.jsp" />
</div>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>