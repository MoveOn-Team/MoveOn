<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MOVE:ON 관리자 · 행사</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body class="admin-page" data-context-path="${pageContext.request.contextPath}">

<header class="admin-top">
    <h1>지역 스포츠 행사</h1>
    <div class="admin-who">
        <a href="${pageContext.request.contextPath}/admin/facilityAdmin">시설</a>
        ${adminName}
        <a href="${pageContext.request.contextPath}/admin/logout">로그아웃</a>
    </div>
</header>

<main class="admin-body">

    <%-- 공개를 눌렀는데 값이 모자라 안 된 경우 --%>
    <c:if test="${not empty param.fail}">
        <p class="form-warn">
            <c:choose>
                <c:when test="${param.fail eq 'date'}">
                    <b>행사일이 없어 공개하지 못했습니다.</b> 대회를 열어 행사 시작일을 채워 주세요.
                </c:when>
                <c:otherwise>
                    <b>좌표가 없어 공개하지 못했습니다.</b> 대회를 열어 장소를 적고 <b>좌표 찾기</b>를 눌러 주세요.
                    좌표가 없으면 거리 계산이 안 되어 사용자 화면에 아예 안 나옵니다.
                </c:otherwise>
            </c:choose>
        </p>
    </c:if>

    <c:if test="${param.deleted eq '1'}">
        <p class="form-hint">행사를 지웠습니다.</p>
    </c:if>


    <%-- 검색 결과는 DB 에 저장하지 않는다. 긁어 쌓으면 작년 후기와 광고만 수백 건 남는다 --%>
    <section class="panel">
        <h2>대회 찾기</h2>
        <p class="panel-sub">
            네이버 웹문서·블로그·뉴스에서 서울·경기 대회를 모아 옵니다.
            화면을 열면 알아서 찾습니다. <b>아직 등록하지 않은 대회가 위에 있습니다.</b>
        </p>

        <div class="search-row">
            <input type="text" id="searchKeyword"
                   placeholder="특정 대회를 찾을 때만 적으세요. 비우면 서울·경기 대회를 훑습니다">
            <button type="button" id="btnSearch" class="btn-line">다시 찾기</button>
        </div>

        <div id="searchResult" class="search-result"></div>
    </section>

    <%-- ==================== 등록된 행사 ==================== --%>
    <section class="panel">
        <div class="panel-head">
            <h2>등록된 행사 <span id="eventCount">${events.size()}</span>건</h2>
            <a class="btn-line" href="${pageContext.request.contextPath}/admin/eventForm">직접 추가</a>
        </div>

        <div class="filter-row">
            <c:forEach var="f" items="${['', 'PENDING', 'PUBLISHED', 'REJECTED']}">
                <a class="chip ${status eq f ? 'is-on' : ''}"
                   href="${pageContext.request.contextPath}/admin/eventAdmin<c:if test='${not empty f}'>?status=${f}</c:if>">
                    <c:choose>
                        <c:when test="${f eq ''}">전체</c:when>
                        <c:when test="${f eq 'PENDING'}">검수 대기</c:when>
                        <c:when test="${f eq 'PUBLISHED'}">공개중</c:when>
                        <c:otherwise>반려</c:otherwise>
                    </c:choose>
                </a>
            </c:forEach>
        </div>

        <c:choose>
            <c:when test="${empty events}">
                <p class="empty">아직 등록된 행사가 없습니다. 위에서 대회를 찾아 보세요.</p>
            </c:when>

            <c:otherwise>
                <div class="table-wrap">
                    <table class="admin-table">
                        <thead>
                        <tr>
                            <th>상태</th>
                            <th>대회명</th>
                            <th>행사일</th>
                            <th>접수마감</th>
                            <th>장소</th>
                            <th>좌표</th>
                            <th>처리</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="e" items="${events}">
                            <tr>
                                <td>
                                    <span class="badge ${e.status}">
                                        <c:choose>
                                            <c:when test="${e.status eq 'PENDING'}">대기</c:when>
                                            <c:when test="${e.status eq 'PUBLISHED'}">공개</c:when>
                                            <c:otherwise>반려</c:otherwise>
                                        </c:choose>
                                    </span>
                                </td>
                                <%-- 관리자 화면도 똑같이 이스케이프. 주소는 http 일 때만 건다 --%>
                                <td class="cell-title">
                                    <a href="${pageContext.request.contextPath}/admin/eventForm?eventId=${e.eventId}">
                                        ${fn:escapeXml(e.title)}
                                    </a>
                                    <c:if test="${fn:startsWith(e.homepageUrl, 'http')}">
                                        <a class="ext" href="${fn:escapeXml(e.homepageUrl)}" target="_blank"
                                           rel="noopener" title="공식 사이트 열기">↗</a>
                                    </c:if>
                                </td>
                                <td>${e.startDate}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty e.applyEnd}"><span class="warn">없음</span></c:when>
                                        <c:otherwise>${e.applyEnd}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${fn:escapeXml(e.placeName)}</td>
                                <td>
                                    <%-- 좌표가 없으면 사용자 목록에서 통째로 빠진다.
                                         공개 조건이 lat·lng 를 둘 다 보므로 여기서도 둘 다 본다 --%>
                                    <c:choose>
                                        <c:when test="${e.lat eq 0 or e.lng eq 0}">
                                            <span class="warn">없음</span>
                                        </c:when>
                                        <c:otherwise>있음</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="cell-act">
                                    <c:if test="${e.status ne 'PUBLISHED'}">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/admin/changeStatus">
                                            <input type="hidden" name="eventId" value="${e.eventId}">
                                            <input type="hidden" name="status" value="PUBLISHED">
                                            <button type="submit" class="btn-ok">공개</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${e.status ne 'REJECTED'}">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/admin/changeStatus">
                                            <input type="hidden" name="eventId" value="${e.eventId}">
                                            <input type="hidden" name="status" value="REJECTED">
                                            <button type="submit" class="btn-no">반려</button>
                                        </form>
                                    </c:if>

                                    <%-- 반려가 아니라 아예 지우는 자리. admin.js 가 한 번 물어본다 --%>
                                    <form method="post" class="form-delete"
                                          action="${pageContext.request.contextPath}/admin/deleteEvent"
                                          data-title="${fn:escapeXml(e.title)}">
                                        <input type="hidden" name="eventId" value="${e.eventId}">
                                        <button type="submit" class="btn-del">삭제</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

</main>

<script src="${pageContext.request.contextPath}/js/admin.js"></script>
</body>
</html>
