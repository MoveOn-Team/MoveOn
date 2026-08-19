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

        <!-- eventList.jsp / eventDetail.jsp 하단에 작성했던 <nav class="bottom-nav">...</nav> 대신 이 2줄 적용 -->
        <c:set var="active" value="event" />
        <jsp:include page="/WEB-INF/views/common/tabbar.jsp" />
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>