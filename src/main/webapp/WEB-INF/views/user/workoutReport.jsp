<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>운동 리포트 - MOVE:ON</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
  <style>
    /* 피그마 목업 스타일 라운드 더보기 버튼 */
    .btn-more-sports {
      width: 100%;
      padding: 12px 0;
      background-color: #f8f9fa;
      border: 1px solid #e9ecef;
      border-radius: 12px;
      color: #495057;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: background-color 0.2s ease;
    }
    .btn-more-sports:active {
      background-color: #e9ecef;
    }
  </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app-container">
  <main class="app-shell report-shell">

    <!-- 상단 헤더 -->
    <header class="page-header nav-header">
      <a href="javascript:history.back()" class="btn-back">‹</a>
      <h1 class="page-title">운동 리포트</h1>
      <div style="width: 24px;"></div>
    </header>

    <!-- 이번주 요약 하이라이트 카드 -->
    <section class="report-banner-card">
      <span class="banner-sub">이번주 (${report.weekRangeText})</span>
      <h2 class="banner-title">${report.thisWeekCount}회 · ${report.thisWeekDurationMin}분</h2>
      <p class="banner-desc">
        <c:choose>
          <c:when test="${report.weekDiffCount > 0}">
            지난주보다 ${report.weekDiffCount}회 늘었어요
          </c:when>
          <c:when test="${report.weekDiffCount < 0}">
            지난주보다 ${Math.abs(report.weekDiffCount)}회 줄었어요
          </c:when>
          <c:otherwise>
            지난주와 동일하게 운동했어요
          </c:otherwise>
        </c:choose>
      </p>
    </section>

    <!-- 3종 실적 통계 -->
    <section class="body-stats-grid">
      <div class="stat-card center">
        <span class="stat-label">소모 칼로리</span>
        <span class="stat-value">${report.totalCalories}<strong>kcal</strong></span>
      </div>
      <div class="stat-card center">
        <span class="stat-label">연속 출석</span>
        <span class="stat-value">${report.currentStreak}<strong>일</strong></span>
      </div>
      <div class="stat-card center">
        <span class="stat-label">누적 기록</span>
        <span class="stat-value">${report.totalWorkoutCount}<strong>회</strong></span>
      </div>
    </section>

    <!-- 최근 8주 운동 횟수 차트 -->
    <section class="card chart-card">
      <h3 class="card-title">최근 8주 운동 횟수</h3>
      <div class="bar-chart-container">
        <div class="bar-group"><div class="bar" style="height: 0%;"></div><span>6/15</span></div>
        <div class="bar-group"><div class="bar" style="height: 0%;"></div><span>6/22</span></div>
        <div class="bar-group"><div class="bar" style="height: 0%;"></div><span>6/29</span></div>
        <div class="bar-group"><div class="bar" style="height: 0%;"></div><span>7/6</span></div>
        <div class="bar-group"><div class="bar" style="height: 0%;"></div><span>7/13</span></div>
        <div class="bar-group"><div class="bar" style="height: 0%;"></div><span>7/20</span></div>
        <div class="bar-group"><div class="bar" style="height: 0%;"></div><span>7/27</span></div>
        <div class="bar-group active"><div class="bar" style="height: 80%;"></div><span>이번</span></div>
      </div>
    </section>

    <!-- 많이 한 종목 카드 -->
    <section class="card rank-card">
      <h3 class="card-title">많이 한 종목</h3>
      <ul class="rank-list" id="sportsRankList">
        <c:forEach var="sport" items="${report.topSports}" varStatus="status">
          <li class="sport-item" style="${status.index >= 4 ? 'display: none;' : ''}">
            <span class="rank-name">${sport.sportName}</span>
            <span class="rank-value">${sport.count}회 · ${sport.percentage}%</span>
          </li>
        </c:forEach>
        <c:if test="${empty report.topSports}">
          <li style="justify-content: center; color: #aaa;">기록된 운동이 없습니다.</li>
        </c:if>
      </ul>

      <!-- 4개 초과 시 N개 계산 후 출력 (최대 3개 단위) -->
      <c:if test="${report.topSports != null && report.topSports.size() > 4}">
        <c:set var="totalSize" value="${report.topSports.size()}" />
        <c:set var="remainCount" value="${totalSize - 4}" />
        <c:set var="nextShow" value="${remainCount > 3 ? 3 : remainCount}" />

        <div style="margin-top: 12px;">
          <button type="button"
                  class="btn-more-sports"
                  id="btnMoreSports"
                  data-total="${totalSize}"
                  data-step="3"
                  onclick="showMoreSports()">
              ${nextShow}개 더보기
          </button>
        </div>
      </c:if>
    </section>

    <!-- 기록 통계 상세 -->
    <section class="card detail-stats-card">
      <div class="detail-row">
        <span>가장 오래한 운동</span>
        <strong>
          <c:choose>
            <c:when test="${report.maxDurationMin > 0}">
              ${report.maxDurationSportName} · ${report.maxDurationMin}분
            </c:when>
            <c:otherwise>-</c:otherwise>
          </c:choose>
        </strong>
      </div>
      <div class="detail-row">
        <span>최장 연속 출석</span>
        <strong>${report.maxStreakDays}일</strong>
      </div>
      <div class="detail-row">
        <span>첫 기록</span>
        <strong>${report.firstRecordDate}</strong>
      </div>
    </section>

  </main>

  <!-- 공통 하단 탭바 -->
  <jsp:include page="/WEB-INF/views/common/tabbar.jsp" />
</div>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
<script>
  let visibleCount = 4; // 기본 4개 노출

  function showMoreSports() {
    const btn = document.getElementById('btnMoreSports');
    const items = document.querySelectorAll('#sportsRankList .sport-item');
    const totalCount = parseInt(btn.getAttribute('data-total'), 10);
    const step = parseInt(btn.getAttribute('data-step'), 10);

    // 1. 이미 다 펼쳐져 있으면 다시 4개로 접기
    if (visibleCount >= totalCount) {
      visibleCount = 4;
      items.forEach((item, idx) => {
        item.style.display = idx < 4 ? 'flex' : 'none';
      });
      const remaining = totalCount - 4;
      const nextShow = Math.min(remaining, step);
      btn.textContent = `${nextShow}개 더보기`;
      return;
    }

    // 2. 3개씩 더 보여주기
    visibleCount += step;
    items.forEach((item, idx) => {
      if (idx < visibleCount) {
        item.style.display = 'flex';
      }
    });

    // 3. 남은 개수 계산하여 버튼 문구 변경
    const remaining = totalCount - visibleCount;
    if (remaining > 0) {
      const nextShow = Math.min(remaining, step);
      btn.textContent = `${nextShow}개 더보기`;
    } else {
      btn.textContent = '접기 ∧';
    }
  }
</script>
</body>
</html>