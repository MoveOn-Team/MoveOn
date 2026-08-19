<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>${event.title} - 상세보기</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
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
                    <button type="button" id="btnLocation" class="btn-location" data-map-url="${event.mapUrl}">길찾기</button>
                <button type="button" id="btnExternal" class="btn-external" data-target-url="${event.externalUrl}">사이트로 이동</button>
            </div>
        </div>

        <!-- 하단 탭바 -->
        <nav class="bottom-nav">
            <a href="${pageContext.request.contextPath}/recommend" class="nav-item">
                <svg viewBox="0 0 24 24"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/></svg>
                <span>추천</span>
            </a>
            <a href="${pageContext.request.contextPath}/instant" class="nav-item">
                <svg viewBox="0 0 24 24"><path d="M13.5 5.5c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zM9.8 8.9L7 23h2.1l1.8-8 2.1 2v6h2v-7.5l-2.1-2 .6-3C14.8 12 16.8 13 19 13v-2c-1.9 0-3.5-1-4.3-2.4l-1-1.6c-.4-.6-1-1-1.7-1-.3 0-.5.1-.8.1L6 8.3V13h2V9.6l1.8-.7z"/></svg>
                <span>즉시운동</span>
            </a>
            <a href="${pageContext.request.contextPath}/event" class="nav-item active">
                <svg viewBox="0 0 24 24"><path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2z"/></svg>
                <span>행사</span>
            </a>
            <a href="${pageContext.request.contextPath}/mypage" class="nav-item">
                <svg viewBox="0 0 24 24"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                <span>내정보</span>
            </a>
        </nav>
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>