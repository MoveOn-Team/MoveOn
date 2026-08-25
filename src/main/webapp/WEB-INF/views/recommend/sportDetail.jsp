<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>운동 리포트</title>
    <!-- 공통 스타일시트 연동 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">

    <!-- 운동 리포트 전용 UI 보완 스타일 -->
    <style>
        .report-card-primary {
            background: #6c5ce7;
            color: #ffffff;
            border: none;
            border-radius: 16px;
            padding: 20px;
            margin-bottom: 16px;
        }
        .report-card-primary .week-range { font-size: 13px; opacity: 0.85; display: block; margin-bottom: 8px; }
        .report-card-primary .main-stat { font-size: 24px; font-weight: 800; margin-bottom: 8px; color: #fff; }
        .report-card-primary .diff-text { font-size: 13px; opacity: 0.9; }

        .stat-grid-3 {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
            margin-bottom: 16px;
        }
        .stat-grid-item {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 12px;
            padding: 12px 8px;
            text-align: center;
        }
        .stat-grid-item .label { font-size: 11px; color: #6c757d; display: block; margin-bottom: 4px; }
        .stat-grid-item .value { font-size: 14px; font-weight: 700; color: #212529; }

        .report-section {
            background: #ffffff;
            border: 1px solid #e9ecef;
            border-radius: 16px;
            padding: 20px;
            margin-bottom: 16px;
        }
        .report-section h3 { font-size: 16px; font-weight: 700; margin-bottom: 14px; color: #212529; }

        .sport-list-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            font-size: 14px;
        }
        .sport-list-item .sport-name { font-weight: 600; color: #333; }
        .sport-list-item .sport-stat { color: #6c757d; font-size: 13px; }

        .record-item-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 0;
            border-bottom: 1px solid #f1f3f5;
            font-size: 14px;
        }
        .record-item-row:last-child { border-bottom: none; }
        .record-item-row span { color: #6c757d; }
        .record-item-row strong { font-weight: 700; color: #212529; }
        .empty-text { text-align: center; color: #adb5bd; font-size: 13px; padding: 8px 0; }
    </style>
</head>
<body>

<div class="container" style="padding-bottom: 80px;">

    <!-- 상단 헤더 -->
    <div class="header">
        <a href="${pageContext.request.contextPath}/user/myPage" class="back-btn">&lt;</a>
        <h1 class="title">운동 리포트</h1>
    </div>

    <!-- 이번 주 운동 요약 메인 카드 -->
    <div class="report-card-primary">
        <span class="week-range">이번주 (${report.weekRangeText})</span>
        <h2 class="main-stat">${report.thisWeekCount}회 · ${report.thisWeekDurationMin}분</h2>
        <p class="diff-text">
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
    </div>

    <!-- 핵심 지표 3종 요약 Grid -->
    <div class="stat-grid-3">
        <div class="stat-grid-item">
            <span class="label">소모 칼로리</span>
            <strong class="value">${report.totalCalories} kcal</strong>
        </div>
        <div class="stat-grid-item">
            <span class="label">연속 출석</span>
            <strong class="value">${report.currentStreak} 일</strong>
        </div>
        <div class="stat-grid-item">
            <span class="label">누적 기록</span>
            <strong class="value">${report.totalWorkoutCount} 회</strong>
        </div>
    </div>

    <!-- 많이 한 종목 Top 4 -->
    <div class="report-section">
        <h3>많이 한 종목</h3>
        <div class="top-sports-list">
            <c:forEach var="sport" items="${report.topSports}">
                <div class="sport-list-item">
                    <span class="sport-name">${sport.sportName}</span>
                    <span class="sport-stat">${sport.count}회 · ${sport.percentage}%</span>
                </div>
            </c:forEach>

            <c:if test="${empty report.topSports}">
                <div class="empty-text">기록된 운동이 없습니다.</div>
            </c:if>
        </div>
    </div>

    <!-- 기록 갱신 영역 -->
    <div class="report-section">
        <div class="record-item-row">
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
        <div class="record-item-row">
            <span>최장 연속 출석</span>
            <strong>${report.maxStreakDays}일</strong>
        </div>
        <div class="record-item-row">
            <span>첫 기록</span>
            <strong>${report.firstRecordDate}</strong>
        </div>
    </div>

</div>

<!-- 공통 탭바 모듈 포함 -->
<jsp:include page="/WEB-INF/views/common/tabbar.jsp" />

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>