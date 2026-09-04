<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MOVE:ON 관리자 · 시설</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body class="admin-page" data-context-path="${pageContext.request.contextPath}">

<header class="admin-top">
    <h1>공공체육시설</h1>
    <div class="admin-who">
        <a href="${pageContext.request.contextPath}/admin/eventAdmin">행사</a>
        ${adminName}
        <a href="${pageContext.request.contextPath}/admin/logout">로그아웃</a>
    </div>
</header>

<main class="admin-body">

    <section class="panel">
        <h2>시설 찾기</h2>
        <p class="panel-sub">
            회원을 어디로 보낼지는 <b>수강신청 주소</b>와 <b>대관 주소</b>가 정하고,
            어느 종목 목록에 뜰지는 <b>종목</b>이 정합니다.
            공공데이터에는 주소가 거의 안 들어 있어 사람이 채워야 합니다.
        </p>

        <form class="search-row" method="get"
              action="${pageContext.request.contextPath}/admin/facilityAdmin">
            <select name="gu">
                <option value="">자치구 전체</option>
                <c:forEach var="g" items="${guList}">
                    <option value="${g}" ${gu eq g ? 'selected' : ''}>${g}</option>
                </c:forEach>
            </select>
            <input type="text" name="keyword" value="${keyword}" placeholder="시설명 일부">
            <select name="filter">
                <option value="" ${filter eq '' ? 'selected' : ''}>전체</option>
                <option value="noUrl" ${filter eq 'noUrl' ? 'selected' : ''}>주소가 없는 곳</option>
                <option value="hasUrl" ${filter eq 'hasUrl' ? 'selected' : ''}>주소가 있는 곳</option>
                <option value="noSport" ${filter eq 'noSport' ? 'selected' : ''}>종목이 없는 곳</option>
            </select>
            <button type="submit" class="btn-line">찾기</button>
        </form>
    </section>

    <section class="panel">
        <div class="panel-head">
            <h2>시설 ${facilities.size()}곳</h2>
        </div>

        <%-- 한 번에 300곳까지만 준다. 전부 훑는 화면이 아니라 손볼 곳을 찾는 화면이다. --%>
        <c:if test="${facilities.size() >= 300}">
            <p class="form-hint">300곳까지만 보여줍니다. 자치구나 시설명으로 좁혀 주세요.</p>
        </c:if>

        <c:choose>
            <c:when test="${empty facilities}">
                <p class="empty">조건에 맞는 시설이 없습니다.</p>
            </c:when>

            <c:otherwise>
                <div class="table-wrap">
                    <table class="admin-table">
                        <thead>
                        <tr>
                            <th>구</th>
                            <th>시설명</th>
                            <th>종목</th>
                            <th>수강신청</th>
                            <th>대관</th>
                            <th>예약주소</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="f" items="${facilities}">
                            <tr>
                                <td>${f.guName}</td>
                                <td class="cell-title">
                                    <a href="${pageContext.request.contextPath}/admin/facilityForm?facilityId=${f.facilityId}">
                                        ${f.name}
                                    </a>
                                </td>
                                <%-- courseCount 에는 붙은 종목 수, myCourseCount 에는
                                     예약주소가 든 종목 수를 담아 왔다. 이 화면 전용 값이다. --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${f.courseCount == 0}"><span class="warn">없음</span></c:when>
                                        <c:otherwise>${f.courseCount}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty f.homepageUrl}"><span class="warn">없음</span></c:when>
                                        <c:otherwise>
                                            <a class="ext" href="${f.homepageUrl}" target="_blank" rel="noopener">열기 ↗</a>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty f.rentalUrl}">&mdash;</c:when>
                                        <c:otherwise>
                                            <a class="ext" href="${f.rentalUrl}" target="_blank" rel="noopener">열기 ↗</a>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${f.myCourseCount == 0 ? '—' : f.myCourseCount}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

</main>
</body>
</html>
