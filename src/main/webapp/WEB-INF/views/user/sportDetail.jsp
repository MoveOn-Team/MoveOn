<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${sport.name}"/> 상세 - MoveOn</title>
    <!-- 기존 auth.css 연결 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<div class="auth-page recommend-page">
    <div class="auth-shell">

        <!-- 1. 상단 뒤로가기 & 헤더 -->
        <div class="detail-header">
            <a href="${pageContext.request.contextPath}/recommend" class="back-button" aria-label="뒤로가기">‹</a>
            <span class="detail-header-title">종목 상세 정보</span>
            <div style="width: 26px;"></div> <!-- 중앙 정렬 맞춤용 여백 -->
        </div>

        <!-- 2. 종목 기본 정보 -->
        <div class="sport-detail-intro">
            <h1 class="sport-detail-title"><c:out value="${sport.name}"/></h1>
            <p class="sport-detail-sub">
                <c:out value="${sport.description != null ? sport.description : '나에게 적합한 운동인지 확인해보세요.'}"/>
            </p>
        </div>

        <!-- 3. 핵심 지표 3종 카드 Grid (지속 적합도 / 강도 MET / 최소 인원) -->
        <div class="metric-grid">
            <div class="metric-card">
                <div class="metric-label">지속 적합도</div>
                <div class="metric-value" style="color: #2b529a;"><c:out value="${sport.totalScore}"/>%</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">운동 강도</div>
                <div class="metric-value">MET <c:out value="${sport.metValue}"/></div>
            </div>
            <div class="metric-card">
                <div class="metric-label">권장 인원</div>
                <div class="metric-value">
                    <c:choose>
                        <c:when test="${sport.minPeople == 1}">혼자</c:when>
                        <c:when test="${sport.minPeople == 2}">상대 필요</c:when>
                        <c:otherwise>단체</c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- 4. 가까운 공공체육시설 TOP 3 -->
        <div class="recommend-section">
            <h2 class="recommend-section-title">가까운 공공체육시설</h2>

            <div class="facility-list">
                <c:forEach var="facility" items="${facilities}">
                    <div class="facility-card ${facility.facilityId == pickId ? 'active' : ''}"
                         data-facility-id="${facility.facilityId}"
                         data-sport-id="${sport.sportId}">
                        <div>
                            <div class="facility-name"><c:out value="${facility.name}"/></div>
                            <div class="facility-addr">
                                <c:out value="${not empty facility.roadAddr ? facility.roadAddr : facility.lotAddr}"/>
                            </div>
                        </div>
                        <span class="distance-badge">
                            <fmt:formatNumber value="${facility.distanceKm}" pattern="0.0"/>km
                        </span>
                    </div>
                </c:forEach>

                <c:if test="${empty facilities}">
                    <div style="text-align: center; padding: 20px 0; color: #9a9ea5; font-size: 13px;">
                        주변에 이용 가능한 공공체육시설이 없습니다.
                    </div>
                </c:if>
            </div>
        </div>

        <!-- 5. 선택된 시설의 운영 강좌 영역 (AJAX 비동기 교체 대상) -->
        <div class="recommend-section" style="margin-top: 20px;">
            <h2 class="recommend-section-title">운영 강좌 정보</h2>

            <div id="programListArea">
                <c:forEach var="program" items="${programs}">
                    <div class="program-card">
                        <div>
                            <div class="program-name"><c:out value="${program.name}"/></div>
                            <div class="program-info">
                                <c:if test="${not empty program.target}">[<c:out value="${program.target}"/>] </c:if>
                                <c:if test="${not empty program.dayOfWeek}"><c:out value="${program.dayOfWeek}"/> </c:if>
                                <c:if test="${not empty program.startTime and not empty program.endTime}">
                                    <c:out value="${program.startTime}"/>~<c:out value="${program.endTime}"/>
                                </c:if>
                            </div>
                        </div>
                        <div class="program-price">
                            <c:choose>
                                <c:when test="${not empty program.fee and program.fee > 0}">
                                    <fmt:formatNumber value="${program.fee}" pattern="#,###"/>원
                                </c:when>
                                <c:otherwise>무료/문의</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty programs}">
                    <div style="text-align: center; padding: 24px 0; color: #9a9ea5; font-size: 13px; background: #f8f9fa; border-radius: 14px;">
                        현재 조회된 등록 강좌가 없습니다.
                    </div>
                </c:if>
            </div>
        </div>

        <!-- 6. 하단 액션 버튼 그룹 (길찾기 및 외부 링크 / AJAX 비동기 교체 대상) -->
        <div id="detailActionGroup" class="detail-action-group">
            <c:set var="pickFacility" value="${null}" />
            <c:forEach var="f" items="${facilities}">
                <c:if test="${f.facilityId == pickId}">
                    <c:set var="pickFacility" value="${f}" />
                </c:if>
            </c:forEach>

            <!-- 카카오맵 지도 길찾기 연동 -->
            <button type="button" class="btn-route"
                    onclick="window.open('https://map.kakao.com/link/search/' + encodeURIComponent('${not empty pickFacility ? pickFacility.name : sport.name}'), '_blank')">
                <!-- 길찾기 -->
            </button>

            <!-- 컨트롤러에서 정한 예약/안내 링크 버튼 -->
            <c:if test="${not empty linkUrl}">
                <a href="${linkUrl}" target="_blank" rel="noopener noreferrer" class="btn-link">
                    <c:out value="${linkLabel}"/>
                </a>
            </c:if>
        </div>

    </div>
</div>

<!-- 기존 auth.js 연결 -->
<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>