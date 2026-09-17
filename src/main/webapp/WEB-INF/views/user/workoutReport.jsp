<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="active" value="myPage" scope="request" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>운동 리포트 - MOVE:ON</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
  <style>
    /* 배너 간격.
       h2·p 의 브라우저 기본 여백이 flex gap 위에 얹혀서 줄 사이가 벌어져 있었다.
       여백은 여기서만 정한다. */
    .report-banner-card {
      gap: 0;
      padding: 18px 20px 20px;
    }
    .report-banner-card .banner-sub {
      font-size: 12.5px;
      opacity: 0.75;
    }
    .report-banner-card .banner-title {
      margin: 4px 0 0;
      font-size: 26px;
      line-height: 1.25;
    }
    .report-banner-card .banner-desc {
      margin: 4px 0 0;
      font-size: 12.5px;
      opacity: 0.85;
    }

    /* 코치 글 묶음. 선 하나로 '여기부터 다른 이야기' 를 알린다 */
    .banner-ai {
      margin-top: 16px;
      padding-top: 14px;
      border-top: 1px solid rgba(255, 255, 255, 0.22);
    }

    /* AI 가 쓴 글임을 밝힌다. 사람이 쓴 안내와 섞이면 안 된다 */
    .ai-tag {
      display: inline-block;
      font-size: 10.5px;
      font-weight: 700;
      letter-spacing: 0.04em;
      padding: 3px 8px;
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.92);
      color: #6C5CE7;
    }
    .ai-text {
      margin: 9px 0 0;
      font-size: 14px;
      line-height: 1.75;
      color: rgba(255, 255, 255, 0.96);
      word-break: keep-all;
    }

    /* 글을 받아오는 동안. 멈춰 있는 게 아니라는 표시만 하면 된다 */
    .ai-text.is-loading {
      opacity: 0.6;
      animation: aiPulse 1.4s ease-in-out infinite;
    }
    @keyframes aiPulse {
      0%, 100% { opacity: 0.45; }
      50%      { opacity: 0.8; }
    }
    @media (prefers-reduced-motion: reduce) {
      .ai-text.is-loading { animation: none; }
    }

    /* 막대 위 숫자. 막대 길이만 보고는 몇 번인지 알 수 없다 */
    .bar-count {
      font-size: 11px;
      font-weight: 600;
      color: #9CA3AF;
      min-height: 14px;
    }
    .bar-group.active .bar-count { color: #6C5CE7; }

    /* 막대가 바닥에서 올라온다.
       높이를 처음부터 넣어 두면 transition 이 걸리지 않으므로,
       실제 높이는 --h 에 담아 두고 차트가 보일 때 is-drawn 을 붙여 옮긴다.
       --d 는 막대마다 조금씩 늦추는 값. 왼쪽부터 차례로 올라온다. */
    .bar-chart-container .bar {
      height: 0;
      transition: height 0.55s cubic-bezier(0.22, 1, 0.36, 1) var(--d, 0ms);
    }
    .bar-chart-container .bar-count {
      opacity: 0;
      transition: opacity 0.3s ease var(--d, 0ms);
    }
    .bar-chart-container.is-drawn .bar { height: var(--h); }
    .bar-chart-container.is-drawn .bar-count { opacity: 1; }

    /* 움직임을 줄여 달라고 설정해 둔 사람에게는 그냥 그려 준다 */
    @media (prefers-reduced-motion: reduce) {
      .bar-chart-container .bar,
      .bar-chart-container .bar-count { transition: none; }
    }

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
          <c:when test="${report.weekDiffCount > 0}">지난주보다 ${report.weekDiffCount}회 늘었어요</c:when>
          <c:when test="${report.weekDiffCount < 0}">지난주보다 ${-report.weekDiffCount}회 줄었어요</c:when>
          <c:otherwise>지난주와 같은 횟수예요</c:otherwise>
        </c:choose>
      </p>

      <%-- 코치 글은 이 배너 안에 붙인다. 배너가 이미 이번 주 숫자를 말하고 있어서
           따로 카드를 두면 같은 이야기를 두 번 하게 된다.

           화면이 뜬 뒤에 따로 받아 온다. Gemini 가 2~3초 걸려서
           여기서 기다리면 리포트 전체가 늦게 뜬다.
           받지 못하면 이 칸만 사라지고 배너는 그대로 남는다. --%>
      <c:if test="${aiReady}">
        <div class="banner-ai" id="aiNoteBox">
          <span class="ai-tag">AI 코치</span>
          <p class="ai-text" id="aiNoteText">이번 주 기록을 읽고 있어요</p>
        </div>
      </c:if>
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

    <%-- 최근 6주 운동 횟수.
         막대 높이는 그 기간 최대 횟수를 100% 로 잡아 견준다.
         고정 기준(예: 7회)으로 하면 주 2회쯤 하는 사람은 막대가 늘 바닥에 붙는다. --%>
    <c:set var="maxWeek" value="1" />
    <c:forEach var="w" items="${report.weeklyStats}">
      <c:if test="${w.count > maxWeek}"><c:set var="maxWeek" value="${w.count}" /></c:if>
    </c:forEach>

    <section class="card chart-card">
      <h3 class="card-title">최근 ${fn:length(report.weeklyStats)}주 운동 횟수</h3>
      <div class="bar-chart-container" id="weekChart">
        <c:forEach var="w" items="${report.weeklyStats}" varStatus="st">
          <div class="bar-group ${st.last ? 'active' : ''}"
               style="--h: ${w.count == 0 ? 3 : (w.count * 100 / maxWeek)}%; --d: ${st.index * 70}ms;">
            <span class="bar-count">${w.count > 0 ? w.count : ''}</span>
            <div class="bar"></div>
            <span>${w.weekLabel}</span>
          </div>
        </c:forEach>
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

  // 막대 올리기.
  // 차트가 화면에 들어왔을 때 시작한다. 접힌 화면에서는 아래쪽에 있어서
  // 페이지가 뜨자마자 올리면 다 끝난 뒤에 스크롤이 닿는다.
  (function () {
    var chart = document.getElementById('weekChart');
    if (!chart) {
      return;
    }
    var draw = function () { chart.classList.add('is-drawn'); };

    if (!('IntersectionObserver' in window)) {
      draw();
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (e.isIntersecting) {
          draw();
          io.disconnect();
        }
      });
    }, { threshold: 0.35 });
    io.observe(chart);
  })();

  // 코치 글 받아오기. 못 받으면 칸을 통째로 없앤다.
  // 빈 칸이 남아 있는 것보다 아예 없는 편이 낫다.
  (function () {
    var box = document.getElementById('aiNoteBox');
    var line = document.getElementById('aiNoteText');
    if (!box || !line) {
      return;
    }
    line.classList.add('is-loading');

    fetch('${pageContext.request.contextPath}/user/api/reportNote')
      .then(function (res) { return res.json(); })
      .then(function (data) {
        if (data && data.msg) {
          line.classList.remove('is-loading');
          line.textContent = data.msg;
        } else {
          box.remove();
        }
      })
      .catch(function () { box.remove(); });
  })();
</script>
</body>
</html>