<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>오픈소스 및 저작권 정보</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/workout.css?v=1.4">
</head>
<body class="auth-page">
<main class="auth-shell copyright-shell">
    <!-- 헤더 영역 -->
    <div class="copyright-header">
        <a href="${pageContext.request.contextPath}/user/myPage" class="back-button" aria-label="뒤로 가기">&#8249;</a>
        <h1 class="page-title">오픈소스 및 저작권 정보</h1>
    </div>

    <!-- 안내 문구 -->
    <div class="copyright-intro">
        <p>MOVE:ON 서비스 구축에 사용된 외부 API, 오픈소스 라이브러리 및 디자인 자산 정보입니다.</p>
    </div>

    <!-- 카드 컨테이너 목록 -->
    <div class="copyright-container">

        <!-- 섹션 1: 디자인 자산 -->
        <div class="copyright-section">
            <h2 class="section-category">디자인 자산 (Graphics)</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">운동 동작 일러스트 (PNG / JPG)</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">출처:</span> Freepik (https://www.freepik.com)</li>
                    <li><span class="info-label">용도:</span> 홈트레이닝 동작 가이드 이미지 (41종)</li>
                    <li><span class="info-label">비고:</span> 자유롭게 수정 가능한 텍스트 필드입니다.</li>
                </ul>
            </div>
        </div>

        <!-- 섹션 2: 위치 및 지도 서비스 -->
        <div class="copyright-section">
            <h2 class="section-category">위치 및 지도 서비스 (APIs)</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">Kakao Maps API</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> Kakao Corp.</li>
                    <li><span class="info-label">용도:</span> 러닝 코스 지도 렌더링 및 위치 표시</li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">Naver Map API</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> NAVER Corp.</li>
                    <li><span class="info-label">용도:</span> 길안내 외부 지도앱 연동</li>
                </ul>
            </div>
        </div>

        <!-- 섹션 3: 오픈소스 라이브러리 -->
        <div class="copyright-section">
            <h2 class="section-category">오픈소스 라이브러리 (Open Source)</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">Spring Framework</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">라이선스:</span> Apache License 2.0</li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">jQuery & JSTL</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">라이선스:</span> MIT / Apache License 2.0</li>
                </ul>
            </div>
        </div>

    </div>
</main>

</body>
</html>