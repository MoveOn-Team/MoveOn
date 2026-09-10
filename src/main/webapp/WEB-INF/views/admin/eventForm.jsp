<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MOVE:ON 관리자 · <c:out value="${isNew ? '행사 추가' : '행사 수정'}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body class="admin-page" data-context-path="${pageContext.request.contextPath}">

<header class="admin-top">
    <h1><c:out value="${isNew ? '행사 추가' : '행사 수정'}"/></h1>
    <div class="admin-who">
        <a href="${pageContext.request.contextPath}/admin/eventAdmin">목록으로</a>
    </div>
</header>

<main class="admin-body">
    <form method="post" action="${pageContext.request.contextPath}/admin/eventForm" class="panel">

        <input type="hidden" name="eventId" value="${event.eventId}">
        <input type="hidden" name="source" value="${empty event.source ? 'MANUAL' : event.source}">

        <%-- 검색 단계에서 지역을 잘못 봐서 다른 지역 대회가 통과하는 일이 있다 --%>
        <c:if test="${event.outsideArea}">
            <p class="form-warn">
                <b>서울·경기 대회가 아닌 것 같습니다.</b>
                주소를 보니 <b><c:out value="${event.sigungu}"/></b> 입니다.
                이 앱은 서울·경기만 다루니 맞는지 확인하고, 아니면 저장하지 마세요.
            </p>
        </c:if>

        <%-- 이미 열린 대회를 넣으려 했을 때 --%>
        <c:if test="${param.past eq '1'}">
            <p class="form-warn">
                <b>이미 열린 대회라 저장하지 않았습니다.</b>
                행사 시작일이 오늘보다 앞이면 넣을 수 없습니다.
                지난 회차 정보를 읽어 온 것일 수 있으니 사이트에서 올해 일정을 확인해 주세요.
            </p>
        </c:if>

        <c:if test="${not empty event.homepageUrl}">
            <p class="form-hint">
                <c:choose>
                    <c:when test="${linkKind eq 'OFFICIAL'}">공식 사이트를 찾았습니다 →</c:when>
                    <c:when test="${linkKind eq 'APPLY'}">
                        <b>전용 홈페이지가 없는 대회입니다.</b> 접수·안내 페이지를 찾았습니다 →
                    </c:when>
                    <c:when test="${linkKind eq 'INFO'}">
                        <b>공식 사이트도 접수처도 못 찾았습니다.</b> 대회 정보 페이지를 대신 넣었습니다 →
                    </c:when>
                    <c:otherwise>사이트를 찾았습니다 →</c:otherwise>
                </c:choose>
                <a href="${event.homepageUrl}" target="_blank" rel="noopener">${event.homepageUrl}</a>
                <br>
                <c:choose>
                    <c:when test="${autoFilled}">
                        <b>이 사이트를 읽어 아래 값을 미리 채웠습니다.</b>
                        기계가 읽은 것이라 틀릴 수 있으니 <b>반드시 사이트를 열어 확인</b>해 주세요.
                        특히 <b>지난 회차 정보를 가져오는 경우</b>가 있어 날짜를 꼭 봐 주세요.
                    </c:when>
                    <c:otherwise>
                        이 사이트에서는 값을 자동으로 못 뽑았습니다.
                        열어 보고 접수기간·참가비·장소를 직접 채워 주세요.
                    </c:otherwise>
                </c:choose>
            </p>
        </c:if>

        <%-- 모음 사이트가 잡히는 일이 있다. 대회 이름과 주소가 안 맞으면 여기서 걸러야 한다. --%>
        <c:if test="${isNew and not empty event.homepageUrl}">
            <p class="form-warn">
                <c:choose>
                    <c:when test="${linkKind eq 'INFO'}">
                        정보 페이지라 접수는 못 합니다.
                        원본에서 <b>실제 접수처를 찾아 주소를 바꿔 주세요.</b>
                        브랜드가 여는 행사는 인스타그램 공지에만 있는 경우도 있습니다.
                    </c:when>
                    <c:otherwise>
                        주소가 이 대회와 맞는지 먼저 봐 주세요.
                        여러 대회를 모아 둔 사이트가 잡히면 엉뚱한 대회로 연결됩니다.
                    </c:otherwise>
                </c:choose>
            </p>
        </c:if>

        <div class="form-grid">

            <label class="full">
                <span>대회명 *</span>
                <input type="text" name="title" value="${event.title}" required>
            </label>

            <label>
                <span>종목</span>
                <input type="text" name="eventType" value="${event.eventType}"
                       placeholder="마라톤 / 걷기 / 자전거">
            </label>

            <label>
                <span>거리 종목</span>
                <input type="text" name="distances" value="${event.distances}"
                       placeholder="5km,10km,하프">
            </label>

            <label>
                <span>행사 시작일</span>
                <input type="date" name="startDate" value="${event.startDate}">
            </label>

            <label>
                <span>행사 종료일</span>
                <input type="date" name="endDate" value="${event.endDate}">
            </label>

            <label>
                <span>접수 시작일</span>
                <input type="date" name="applyStart" value="${event.applyStart}">
            </label>

            <label>
                <span>접수 마감일</span>
                <input type="date" name="applyEnd" value="${event.applyEnd}">
            </label>

            <%-- 좌표가 비면 사용자 목록에서 통째로 빠진다. 카카오에 물어 채운다 --%>
            <label class="full">
                <span>장소 *</span>
                <span class="with-btn">
                    <input type="text" id="placeName" name="placeName"
                           value="${event.placeName}" required
                           placeholder="여의도 한강공원 물빛광장">
                    <button type="button" id="btnFindPlace" class="btn-line">좌표 찾기</button>
                </span>
            </label>

            <label>
                <span>자치구</span>
                <input type="text" id="sigungu" name="sigungu" value="${event.sigungu}" readonly>
            </label>

            <%-- 좌표 0 이면 빈 칸으로. "0.0" 을 두면 값이 있는 셈이라 required 가 통과시킨다 --%>
            <label>
                <span>위도</span>
                <input type="text" id="lat" name="lat" readonly
                       value="${event.lat eq 0 ? '' : event.lat}"
                       placeholder="좌표 찾기를 눌러 주세요">
            </label>

            <label>
                <span>경도</span>
                <input type="text" id="lng" name="lng" readonly
                       value="${event.lng eq 0 ? '' : event.lng}"
                       placeholder="좌표 찾기를 눌러 주세요">
            </label>

            <label class="full">
                <span>참가비</span>
                <input type="text" name="feeText" value="${event.feeText}"
                       placeholder="하프 80,000원 / 10km 70,000원">
            </label>

            <label>
                <span>참가 대상</span>
                <input type="text" name="target" value="${event.target}"
                       placeholder="비우면 '제한 없음' 으로 보입니다">
            </label>

            <label>
                <span>문의처</span>
                <input type="text" name="contact" value="${event.contact}">
            </label>

            <label class="full">
                <span>공식 홈페이지</span>
                <input type="url" name="homepageUrl" value="${event.homepageUrl}">
            </label>

            <label class="full">
                <span>확인한 곳 (source_url)</span>
                <input type="url" name="sourceUrl" value="${event.sourceUrl}"
                       placeholder="값을 어디서 보고 넣었는지. 대회 공식 홈페이지를 적습니다">
            </label>
        </div>

        <div class="form-foot">
            <p class="form-note">
                저장하면 <b>검수 대기</b> 상태가 됩니다.
                목록에서 <b>공개</b>를 눌러야 사용자 화면에 나갑니다.
                <br>
                <b>행사일과 좌표는 지금 비워 둬도 됩니다.</b>
                아직 공지가 안 올라온 대회는 담아 두었다가 나중에 채우세요.
                다만 그 둘이 없으면 <b>공개할 수 없습니다.</b>
            </p>
            <button type="submit" class="btn-primary">저장</button>
        </div>
    </form>
</main>

<script src="${pageContext.request.contextPath}/js/admin.js"></script>
</body>
</html>
