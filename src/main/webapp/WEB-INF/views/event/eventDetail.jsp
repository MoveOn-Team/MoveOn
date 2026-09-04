<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON ${event.title}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/event.css">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">

<c:set var="DOW" value="월,화,수,목,금,토,일"/>

<main class="auth-shell event-shell">

    <%-- 목록으로 돌아갈 때 현위치를 이어줘야 거리 표시가 그대로 유지됨.
         history.back() 을 쓰면 주소로 바로 들어온 경우에 갈 곳이 없음. --%>
    <a class="back-button"
       href="${pageContext.request.contextPath}/event/eventList?lat=${lat}&lng=${lng}"
       aria-label="뒤로 가기">&#8249;</a>

    <div class="detail-header-card">
        <span class="detail-badge">
            <c:choose>
                <c:when test="${event.applyState eq 'CLOSED'}">접수마감</c:when>
                    <c:when test="${event.applyState eq 'BEFORE'}">${event.applyStart.monthValue}/${event.applyStart.dayOfMonth} 접수 시작</c:when>
                    <c:when test="${event.applyState eq 'OPEN' and event.dday eq null}">접수중 · 선착순</c:when>
                    <c:when test="${event.applyState eq 'OPEN' and event.dday eq 0}">오늘 마감</c:when>
                    <c:when test="${event.applyState eq 'OPEN'}">접수중 · D-${event.dday}</c:when>
                    <c:otherwise>접수 정보 확인 필요</c:otherwise>
            </c:choose>
        </span>
        <h1 class="detail-title">${event.title}</h1>
        <p class="detail-sub">
            ${event.sigungu}
            · 현위치에서 <fmt:formatNumber value="${event.distanceKm}" maxFractionDigits="1"/>km
        </p>
    </div>

    <div class="info-table-card">

        <div class="info-row">
            <span class="info-label">행사일시</span>
            <span class="info-val highlight-text">
                ${event.startDate.year}.${event.startDate.monthValue}.${event.startDate.dayOfMonth}
                (${fn:split(DOW, ',')[event.startDate.dayOfWeek.value - 1]})
                <%-- 며칠에 걸쳐 열리는 행사면 끝나는 날도 붙임 --%>
                <c:if test="${event.endDate ne null and event.endDate ne event.startDate}">
                    ~ ${event.endDate.monthValue}.${event.endDate.dayOfMonth}
                </c:if>
            </span>
        </div>

        <%-- 접수 기간은 홈페이지에 안 적힌 대회가 실제로 있음.
             그때는 빈 칸을 두지 말고 확인이 필요하다고 알려줌. --%>
        <div class="info-row">
            <span class="info-label">접수기간</span>
            <span class="info-val highlight-text">
                <c:choose>
                    <c:when test="${event.applyStart ne null and event.applyEnd ne null}">
                        ${event.applyStart.monthValue}.${event.applyStart.dayOfMonth}
                        ~ ${event.applyEnd.monthValue}.${event.applyEnd.dayOfMonth}
                    </c:when>
                    <c:when test="${event.applyEnd ne null}">
                        ~ ${event.applyEnd.monthValue}.${event.applyEnd.dayOfMonth} 까지
                    </c:when>
                    <c:when test="${event.applyStart ne null}">
                        ${event.applyStart.monthValue}.${event.applyStart.dayOfMonth} 부터 · 선착순 마감
                    </c:when>
                    <c:otherwise>사이트에서 확인</c:otherwise>
                </c:choose>
            </span>
        </div>

        <div class="info-row">
            <span class="info-label">장소</span>
            <span class="info-val">${event.placeName}</span>
        </div>

        <div class="info-row">
            <span class="info-label">종목</span>
            <span class="info-val">
                <%-- 자료에는 "하프,10km" 처럼 붙어 있다.
                     쉼표 뒤를 한 칸 띄워야 종목이 몇 개인지 눈에 들어온다. --%>
                <c:choose>
                    <c:when test="${not empty event.distances}">
                        ${fn:replace(event.distances, ',', ', ')}
                    </c:when>
                    <c:otherwise>${event.eventType}</c:otherwise>
                </c:choose>
            </span>
        </div>

        <div class="info-row">
            <span class="info-label">대상</span>
            <span class="info-val">
                <c:choose>
                    <c:when test="${not empty event.target}">${event.target}</c:when>
                    <c:otherwise>제한 없음</c:otherwise>
                </c:choose>
            </span>
        </div>

        <div class="info-row">
            <span class="info-label">참가비</span>
            <span class="info-val">
                <c:choose>
                    <c:when test="${not empty event.feeText}">${event.feeText}</c:when>
                    <c:otherwise>사이트에서 확인</c:otherwise>
                </c:choose>
            </span>
        </div>

        <div class="info-row">
            <span class="info-label">문의</span>
            <span class="info-val">
                <%-- 연락처가 여러 개인 대회가 많다.
                       070-7727-1751, 카카오톡 아이디: mbn서울마라톤
                       전화 070-7725-6258 / 메일 run.ytn@gmail.com
                     한 줄로 이어 붙이면 어디까지가 전화번호인지 알 수 없다.
                     쉼표와 빗금이 둘 다 쓰이므로 fn:split 에 두 글자를 함께 넘긴다.
                     (fn:split 은 구분자를 '글자 모음' 으로 받는다) --%>
                <c:choose>
                    <c:when test="${not empty event.contact}">
                        <c:forEach var="one" items="${fn:split(event.contact, ',/')}">
                            <span class="contact-line">${fn:trim(one)}</span>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>&mdash;</c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>

    <p class="data-notice">
        일정·참가비는 주최 측 사정으로 변경될 수 있습니다<br>
        접수 전 원본 사이트에서 반드시 확인해주세요
    </p>

    <%-- 길찾기는 좌표로 연다. 관리자가 넣을 때 좌표를 확인해 두므로 좌표 링크가 정확하다.
         link/map 은 그 자리를 지도에 띄우기만 해서 단추 이름과 어긋난다.
         link/to 는 도착지를 정해 주고 출발지는 카카오맵이 현위치로 잡는다.
         행사명에 쉼표가 들어가면 좌표가 밀리므로 미리 뗀다. --%>
    <div class="detail-btn-group">
        <button type="button" id="btnLocation" class="btn-location"
                data-map-url="https://map.kakao.com/link/to/${fn:replace(event.placeName, ',', ' ')},${event.lat},${event.lng}">
            길찾기
        </button>
        <button type="button" id="btnExternal" class="btn-external"
                data-target-url="${event.homepageUrl}">
            사이트로 이동
        </button>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp"/>

<%-- 현위치를 받아 좌표를 달고 같은 주소를 다시 연다. 거리 표시가 여기에 달려 있다. --%>
<script src="${pageContext.request.contextPath}/js/geo.js"></script>
<script src="${pageContext.request.contextPath}/js/event.js"></script>
</body>
</html>
