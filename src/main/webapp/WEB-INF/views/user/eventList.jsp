<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>지역 스포츠 행사</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="auth-page">
    <div class="app-shell">
        <div class="app-content">
            <!-- 상단 헤더 -->
            <div class="event-header">
                <h1>지역 스포츠 행사</h1>
                <button type="button" id="btnRefresh" class="refresh-badge">8/5 갱신</button>
            </div>
            <p class="event-subtext">현위치 40km 이내 · 서울·경기</p>

            <!-- 정렬 탭 -->
            <div class="tab-group">
                <button type="button" class="tab-btn active" data-filter="near">가까운 순</button>
                <button type="button" class="tab-btn" data-filter="close">마감 임박순</button>
            </div>

            <!-- 행사 목록 -->
            <div class="event-card-list">
                <c:forEach var="event" items="${eventList}">
                    <div class="event-card ${event.isHighlight ? 'highlight' : ''} ${event.isExpired ? 'expired' : ''}"
                         data-event-id="${event.id}">

                        <!-- 좌측 날짜 영역 -->
                        <div class="event-date-box">
                            <span class="month"><fmt:formatDate value="${event.eventDate}" pattern="M월"/></span>
                            <span class="day"><fmt:formatDate value="${event.eventDate}" pattern="d"/></span>
                            <span class="weekday"><fmt:formatDate value="${event.eventDate}" pattern="E"/></span>
                        </div>

                        <!-- 우측 정보 영역 -->
                        <div class="event-info">
                            <h2 class="event-title">${event.title}</h2>
                            <p class="event-location">${event.location} · 현위치에서 ${event.distance}km</p>

                            <div class="event-tags">
                                <c:forEach var="tag" items="${event.tags}">
                                    <span class="tag-spec">${tag}</span>
                                </c:forEach>
                                <c:if test="${not empty event.dDayStatus}">
                                    <span class="tag-status">${event.dDayStatus}</span>
                                </c:if>
                            </div>

                            <span class="event-price">참가비 <fmt:formatNumber value="${event.price}" type="currency" currencySymbol=""/>원${event.isPriceVariable ? '~' : ''}</span>
                        </div>
                    </div>
                </c:forEach>
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