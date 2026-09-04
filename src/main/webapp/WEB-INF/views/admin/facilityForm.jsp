<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MOVE:ON 관리자 · ${facility.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body class="admin-page" data-context-path="${pageContext.request.contextPath}">

<header class="admin-top">
    <h1>${facility.name}</h1>
    <div class="admin-who">
        <a href="${pageContext.request.contextPath}/admin/facilityAdmin">시설 목록</a>
        ${adminName}
        <a href="${pageContext.request.contextPath}/admin/logout">로그아웃</a>
    </div>
</header>

<main class="admin-body">

    <c:if test="${param.saved eq '1'}">
        <p class="form-note"><b>저장했습니다.</b> 회원 화면에 바로 반영됩니다.</p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/admin/facilityForm">
        <input type="hidden" name="facilityId" value="${facility.facilityId}">

        <%-- ==================== 시설 정보 ====================
             고칠 수 없는 값들이다. 어디인지 알아야 사이트를 찾을 수 있어 함께 보여준다. --%>
        <section class="panel">
            <h2>어디인가</h2>
            <div class="form-grid">
                <label>자치구</label>
                <div>${facility.guName}</div>

                <label>주소</label>
                <div>${not empty facility.roadAddr ? facility.roadAddr : facility.lotAddr}</div>

                <label>전화</label>
                <div>
                    <c:choose>
                        <c:when test="${not empty facility.phone}">
                            <a href="tel:${facility.phone}">${facility.phone}</a>
                        </c:when>
                        <c:otherwise>&mdash;</c:otherwise>
                    </c:choose>
                </div>

                <label>지도</label>
                <div>
                    <a class="ext" target="_blank" rel="noopener"
                       href="https://map.kakao.com/link/map/${facility.name},${facility.lat},${facility.lng}">
                        카카오맵에서 보기 ↗
                    </a>
                </div>

                <%-- 자치구 창구는 이 시설이 자기 주소를 못 가졌을 때 회원이 떨어지는 곳이다.
                     여기서 그 시설을 찾을 수 있으면 그 주소를 아래에 넣어 주면 된다. --%>
                <label>자치구 창구</label>
                <div>
                    <c:if test="${not empty facility.districtUrl}">
                        <a class="ext" href="${facility.districtUrl}" target="_blank" rel="noopener">수강신청 ↗</a>
                    </c:if>
                    <c:if test="${not empty facility.districtRentalUrl}">
                        <a class="ext" href="${facility.districtRentalUrl}" target="_blank" rel="noopener">대관 ↗</a>
                    </c:if>
                    <c:if test="${empty facility.districtUrl and empty facility.districtRentalUrl}">&mdash;</c:if>
                    <p class="form-hint">
                        ${facility.guName} 전체가 쓰는 주소입니다. 여기서는 못 고칩니다.
                        열어서 이 시설이 있는지 보고, 그 시설의 주소를 아래에 넣어 주세요.
                    </p>
                </div>
            </div>
        </section>

        <%-- ==================== 주소 ==================== --%>
        <section class="panel">
            <h2>회원을 어디로 보낼까</h2>

            <%-- 주소 칸이 세 군데라 헷갈리기 쉽다.
                 셋은 서로 다른 종류가 아니라 '좁은 것부터 넓은 것' 순서다.
                 화면은 위에서부터 찾다가 값이 없으면 아래로 내려간다. --%>
            <p class="panel-sub">
                주소 자리가 셋인데 <b>고르는 게 아니라 순서</b>입니다.
                좁은 것부터 찾다가 없으면 넓은 것으로 내려갑니다.
            </p>

            <table class="admin-table order-table">
                <thead>
                <tr><th>순서</th><th>어디에 적나</th><th>무엇의 주소인가</th></tr>
                </thead>
                <tbody>
                <tr><td>1</td><td>아래 <b>종목별 대관 주소</b></td><td>이 시설의 그 코트 하나</td></tr>
                <tr><td>2</td><td>여기 <b>수강신청 · 대관</b></td><td>이 시설 전체</td></tr>
                <tr><td>3</td><td>위 <b>자치구 창구</b> (못 고침)</td><td>그 구의 모든 시설</td></tr>
                </tbody>
            </table>

            <p class="form-hint">
                자치구 창구는 그 구가 통째로 쓰는 주소라 여기서 못 고칩니다.
                자치구 사이트를 열었을 때 이 시설이 안 보이면, 그곳 주소를 찾아
                아래 두 칸에 넣어 주세요. 그래야 회원이 큰 사이트에서 헤매지 않습니다.
            </p>

            <div class="form-grid">
                <label for="homepageUrl">수강신청 · 안내</label>
                <div>
                    <input type="url" id="homepageUrl" name="homepageUrl" value="${facility.homepageUrl}"
                           placeholder="https://... 강좌를 신청하거나 안내하는 페이지">
                    <p class="form-hint">
                        <b>강좌가 있는 시설</b>이 여기로 갑니다.
                        방문 접수만 받는 곳이면 프로그램 안내 페이지를 넣어 주세요.
                        단추 이름이 '안내 페이지로 이동' 으로 바뀝니다.
                    </p>
                </div>

                <label for="rentalUrl">대관</label>
                <div>
                    <input type="url" id="rentalUrl" name="rentalUrl" value="${facility.rentalUrl}"
                           placeholder="https://... 이 시설 전체의 대관 신청 페이지">
                    <p class="form-hint">
                        <b>강좌 없이 장소만 내주는 시설</b>이 여기로 갑니다.
                        시설 전체가 한 페이지에서 대관을 받을 때만 쓰고,
                        코트마다 페이지가 다르면 아래 종목별 칸에 넣어 주세요.
                    </p>
                </div>
            </div>
        </section>

        <%-- ==================== 종목 ==================== --%>
        <section class="panel">
            <h2>무슨 종목을 하는가</h2>
            <p class="panel-sub">
                체크한 종목의 목록에만 이 시설이 뜹니다.
                <b>이름이 테니스장인데 테니스가 안 붙어 있던 곳이 실제로 많았습니다.</b>
                예약주소는 그 종목만의 대관 화면이 따로 있을 때 넣습니다.
            </p>

            <div class="table-wrap">
                <table class="admin-table">
                    <thead>
                    <tr>
                        <th>함</th>
                        <th>종목</th>
                        <th>우리 자료</th>
                        <th>이 종목만의 대관 주소</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="s" items="${sports}">
                        <tr>
                            <td>
                                <input type="checkbox" name="sportIds" value="${s.sportId}"
                                       id="sp${s.sportId}" ${s.linked == 1 ? 'checked' : ''}>
                            </td>
                            <td><label for="sp${s.sportId}">${s.sportName}</label></td>

                            <%-- 강좌·이용권이 몇 건인지 보여준다.
                                 체크를 풀지 말지 판단하는 근거다.
                                 자료가 0인데 체크돼 있으면 이름이나 관리대장에서 온 것이다. --%>
                            <td>
                                <c:choose>
                                    <c:when test="${s.courseCnt > 0 and s.otherCnt > 0}">
                                        강좌 ${s.courseCnt} · 이용권 ${s.otherCnt}
                                    </c:when>
                                    <c:when test="${s.courseCnt > 0}">강좌 ${s.courseCnt}</c:when>
                                    <c:when test="${s.otherCnt > 0}">이용권 ${s.otherCnt}</c:when>
                                    <c:otherwise>&mdash;</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <input type="url" name="reserveUrl_${s.sportId}" value="${s.reserveUrl}"
                                       placeholder="https://...">
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>

        <div class="form-foot">
            <a class="btn-line" href="${pageContext.request.contextPath}/admin/facilityAdmin">목록</a>
            <button type="submit" class="btn-primary">저장</button>
        </div>
    </form>

</main>
</body>
</html>
