<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON 지역 스포츠 행사</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/event.css">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}"
      data-lat="${lat}" data-lng="${lng}">

<%-- 날짜의 요일을 우리말로 바꿀 때 쓴다.
     LocalDate.getDayOfWeek().getValue() 가 월요일 1 ~ 일요일 7 이라 -1 해서 꺼낸다. --%>
<c:set var="DOW" value="월,화,수,목,금,토,일"/>

<main class="auth-shell event-shell">

    <div class="event-header">
        <h1>지역 스포츠 행사</h1>
        <%-- 행사마다 넣은 날이 달라서 그중 가장 최근 날을 대표로 보여준다.
             한 건도 없으면 표시할 날짜가 없으므로 배지를 만들지 않는다. --%>
        <c:if test="${not empty updatedAt}">
            <button type="button" id="btnRefresh" class="refresh-badge">
                ${updatedAt.monthValue}/${updatedAt.dayOfMonth} 갱신
            </button>
        </c:if>
    </div>

    <p class="event-subtext">
        <c:choose>
            <c:when test="${usingGps}">현위치 기준</c:when>
            <c:otherwise>서울시청 기준</c:otherwise>
        </c:choose>
        · 서울·경기
    </p>

    <%-- 정렬 토글.
         누르면 event.js 가 같은 주소에 sort 만 바꿔 다시 요청한다.
         화면에서 목록을 다시 줄 세우지 않는 이유는, 거리와 마감일 계산이 모두 SQL 에 있어서다. --%>
    <div class="tab-group">
        <button type="button" class="tab-btn ${sort ne 'deadline' ? 'active' : ''}"
                data-filter="near">가까운 순</button>
        <button type="button" class="tab-btn ${sort eq 'deadline' ? 'active' : ''}"
                data-filter="deadline">마감 임박순</button>
    </div>

    <c:choose>
        <c:when test="${empty events}">
            <%-- 화면이 통째로 비면 고장으로 보인다. 초기에는 자료가 적어 이 화면을 자주 보게 된다. --%>
            <div class="empty-box">
                지금 참가할 수 있는 행사가 없어요.<br>
                접수 중인 대회가 올라오면 여기에 보여드릴게요.
            </div>
        </c:when>

        <c:otherwise>
            <div class="event-card-list">
                <c:forEach var="event" items="${events}">

                    <%-- 접수가 끝난 대회는 흐리게 보여준다.
                         목록에서 아예 빼지 않는 이유는, 행사 자체는 아직 남아 있어
                         "이런 대회가 있구나" 를 알리는 값어치는 있어서다.
                         대신 SQL 이 맨 아래로 내려 준다.

                         테두리로 강조하던 것은 없앴다.
                         왜 테두리가 쳐졌는지 화면에서 알 수가 없었다.
                         아래 배지가 "오늘 마감", "접수 D-3" 으로 이미 말해 준다. --%>
                    <div class="event-card ${event.applyState eq 'CLOSED' ? 'expired' : ''}"
                         data-event-id="${event.eventId}">

                        <div class="event-date-box">
                            <span class="month">${event.startDate.monthValue}월</span>
                            <span class="day">${event.startDate.dayOfMonth}</span>
                            <span class="weekday">${fn:split(DOW, ',')[event.startDate.dayOfWeek.value - 1]}</span>
                        </div>

                        <div class="event-info">
                            <h2 class="event-title">${event.title}</h2>

                            <p class="event-location">
                                ${event.placeName}
                                · 현위치에서
                                <c:choose>
                                    <c:when test="${event.distanceKm lt 1}">
                                        <fmt:formatNumber value="${event.distanceKm * 1000}"
                                                          maxFractionDigits="0"/>m
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatNumber value="${event.distanceKm}"
                                                          maxFractionDigits="1"/>km
                                    </c:otherwise>
                                </c:choose>
                            </p>

                            <div class="event-tags">
                                <%-- distances 는 "5km,10km,하프" 처럼 한 칸에 쉼표로 들어 있다 --%>
                                <c:if test="${not empty event.distances}">
                                    <c:forEach var="d" items="${fn:split(event.distances, ',')}">
                                        <span class="tag-spec">${fn:trim(d)}</span>
                                    </c:forEach>
                                </c:if>

                                <%-- 마라톤은 "선착순 마감" 이 많아 접수 마감일이 없는 대회가 흔하다.
                                     마감일만 보면 그런 대회를 "정보 없음" 으로 밀어내게 되므로
                                     접수 시작일까지 함께 본 applyState 로 갈라 보여준다. --%>
                                <c:choose>
                                    <c:when test="${event.applyState eq 'CLOSED'}">
                                        <span class="tag-status is-off">접수마감</span>
                                    </c:when>
                                    <c:when test="${event.applyState eq 'BEFORE'}">
                                        <span class="tag-status">${event.applyStart.monthValue}/${event.applyStart.dayOfMonth} 접수 시작</span>
                                    </c:when>
                                    <c:when test="${event.applyState eq 'OPEN' and event.dday eq null}">
                                        <span class="tag-status">접수중 · 선착순</span>
                                    </c:when>
                                    <c:when test="${event.applyState eq 'OPEN' and event.dday eq 0}">
                                        <span class="tag-status">오늘 마감</span>
                                    </c:when>
                                    <c:when test="${event.applyState eq 'OPEN'}">
                                        <span class="tag-status">접수 D-${event.dday}</span>
                                    </c:when>
                                    <c:otherwise><%-- 접수 정보를 모르면 배지를 만들지 않는다 --%></c:otherwise>
                                </c:choose>
                            </div>

                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<c:set var="active" value="event"/>
<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<script src="${pageContext.request.contextPath}/js/event.js"></script>
</body>
</html>
