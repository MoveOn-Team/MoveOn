<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>${event.title} - 상세보기</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="auth-page">
    <div class="app-shell">
        <div class="app-content">
            <!-- 뒤로가기 버튼 -->
            <a href="javascript:history.back()" class="back-button" aria-label="뒤로가기">&lt;</a>

            <!-- 상세 상단 카드 -->
            <div class="detail-header-card">
                <span class="detail-badge">${event.statusText}</span>
                <h1 class="detail-title">${event.title}</h1>
                <p class="detail-sub">현위치 40km 이내 · 서울·경기</p>
            </div>

            <!-- 상세 정보 테이블 -->
            <div class="info-table-card">
                <div class="info-row">
                    <span class="info-label">행사일시</span>
                    <span class="info-val highlight-text">${event.eventDateTimeStr}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">접수기간</span>
                    <span class="info-val highlight-text">${event.applyPeriodStr}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">장소</span>
                    <span class="info-val">${event.locationDetail}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">종목</span>
                    <span class="info-val">${event.categoryStr}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">대상</span>
                    <span class="info-val">${event.targetAudience}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">참가비</span>
                    <span class="info-val"><fmt:formatNumber value="${event.price}" type="currency" currencySymbol=""/>원</span>
                </div>
                <div class="info-row">
                    <span class="info-label">문의</span>
                    <span class="info-val">${event.contactNumber}</span>
                </div>
            </div>

            <!-- 유의사항 안내 박스 -->
            <div class="notice-box">
                일정·참가비는 주최 측 사정으로 변경될 수 있습니다<br>
                접수 전 원본 사이트에서 반드시 확인해주세요
            </div>

            <!-- 하단 액션 버튼 -->
            <div class="detail-btn-group">
                    <button type="button" id="btnLocation" class=" btn-location" data-map-url="${event.mapUrl}">길찾기</button>
                <button type="button" id="btnExternal" class="btn-external" data-target-url="${event.externalUrl}">사이트로 이동</button>
            </div>
        </div>


    </div>
    <jsp:include page="/WEB-INF/views/common/tabbar.jsp" />
</div>
<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>