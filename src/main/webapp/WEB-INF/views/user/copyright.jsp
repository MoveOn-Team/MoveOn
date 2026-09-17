<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
        <p>MOVE:ON 서비스 구축에 사용된 공공데이터, 외부 API, 오픈소스 라이브러리 및 디자인 자산 정보입니다.</p>
    </div>

    <!-- 카드 컨테이너 목록 -->
    <div class="copyright-container">

        <%-- 시설·홈트·추천 점수가 전부 공공데이터에서 왔으므로 맨 위에 둔다.

             이용조건은 배포처에 적힌 것만 옮긴다. KSPO 두 건은 문화 빅데이터
             플랫폼에도 공공데이터포털에도 유형이 안 붙어 있어 비워 두었다.
             그 플랫폼 저작권정책이 '공공누리 표시가 부착된 저작물인지 확인한
             뒤 이용하라' 고 하므로, 없는 것을 제1유형이라 단정할 수 없다.
             출처 표시는 어느 유형이든 공통 의무라 그것만 남긴다. --%>
        <div class="copyright-section">
            <h2 class="section-category">공공데이터 (Public Data)</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">전국공공체육시설 데이터</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> 국민체육진흥공단 (KSPO)</li>
                    <li><span class="info-label">용도:</span> 시설 이름 · 주소 · 좌표 · 전화 · 홈페이지</li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">국민 연령별 추천운동 정보</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> 국민체육진흥공단 (KSPO)</li>
                    <li><span class="info-label">용도:</span> 연령대 · BMI · 성별에 따른 홈트레이닝 계획</li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">2025 국민생활체육조사 결과보고서</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">발행:</span> 문화체육관광부 (2025. 12.)</li>
                    <li><span class="info-label">용도:</span> 연령대 · 성별 종목 참여율 (추천 점수에 반영)</li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">서울시 체육시설 공공서비스 예약정보</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> 서울특별시</li>
                    <li><span class="info-label">용도:</span> 강습 · 대관 신청 창구 연결</li>
                    <li><span class="info-label">이용조건:</span> 공공누리 제1유형 (출처 표시)</li>
                </ul>
            </div>

            <%-- 네 건 모두 제4유형이다. 상업적 이용과 변경이 금지돼 있으니
                 이 서비스를 영리로 돌릴 때는 서울시에 따로 물어야 한다. --%>
            <div class="copyright-card">
                <h3 class="card-item-title">서울두드림길 · 둘레길 · 문화길 위치정보</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> 서울특별시 (정원도시국 자연생태과)</li>
                    <li><span class="info-label">용도:</span> 걷기 · 등산 코스 위치와 경로</li>
                    <li><span class="info-label">이용조건:</span> 공공누리 제4유형
                        (출처 표시 + 상업적 이용 금지 + 변경 금지)</li>
                </ul>
            </div>
        </div>

        <!-- 섹션 2: 디자인 자산 -->
        <div class="copyright-section">
            <h2 class="section-category">디자인 자산 (Graphics)</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">운동 동작 일러스트 (PNG / JPG)</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> Freepik (www.freepik.com)</li>
                    <li><span class="info-label">용도:</span> 홈트레이닝 동작 가이드 이미지 (41종)</li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">날씨 아이콘 (SVG)</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> amCharts (www.amcharts.com)</li>
                    <li><span class="info-label">용도:</span> 추천 탭 날씨 표시</li>
                    <li><span class="info-label">라이선스:</span>
                        <a class="license-link" href="https://creativecommons.org/licenses/by/4.0/"
                           target="_blank" rel="noopener noreferrer">CC BY 4.0</a> (출처 표시)</li>
                </ul>
            </div>
        </div>

        <!-- 섹션 3: 위치 및 지도 서비스 -->
        <div class="copyright-section">
            <h2 class="section-category">위치 및 지도 서비스</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">Kakao Maps JavaScript API</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> Kakao Corp.</li>
                    <li><span class="info-label">용도:</span> 코스 · 시설 지도 표시, 카카오맵 길찾기 연결</li>
                </ul>
            </div>

            <%-- API 가 아니라 nmap:// 주소로 앱을 띄우는 것뿐이다. 키도 쓰지 않는다. --%>
            <div class="copyright-card">
                <h3 class="card-item-title">네이버 지도</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> NAVER Corp.</li>
                    <li><span class="info-label">용도:</span> 코스 길안내를 네이버지도 앱으로 넘김</li>
                </ul>
            </div>
        </div>

        <!-- 섹션 4: 그 밖의 API -->
        <div class="copyright-section">
            <h2 class="section-category">그 밖의 API</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">OpenWeather API</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> OpenWeather Ltd.</li>
                    <li><span class="info-label">용도:</span> 추천 탭 현재 기온 · 날씨 상태</li>
                    <li><span class="info-label">라이선스:</span>
                        <a class="license-link" href="https://opendatacommons.org/licenses/odbl/1-0/"
                           target="_blank" rel="noopener noreferrer">ODbL 1.0</a></li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">Google Gemini API</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> Google LLC</li>
                    <li><span class="info-label">용도:</span> 운동 리포트의 AI 코치 글, 행사 정보 정리</li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">NAVER 검색 API</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">제공:</span> NAVER Corp.</li>
                    <li><span class="info-label">용도:</span> 지역 스포츠 행사 검색</li>
                </ul>
            </div>
        </div>

        <%-- 위치는 받아서 쓰기만 하고 남기지 않는다.
             좌표는 주소창과 브라우저 sessionStorage 에만 있고 표에 넣지 않는다. --%>
        <div class="copyright-section">
            <h2 class="section-category">위치 정보</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">현재 위치 이용 안내</h3>
                <ul class="card-info-list">
                    <li><span class="info-label">용도:</span> 가까운 체육시설 · 코스를 거리순으로 정렬</li>
                    <li><span class="info-label">보관:</span> 저장하지 않습니다. 브라우저를 닫으면 사라집니다</li>
                    <li><span class="info-label">거부 시:</span> 서울시청을 기준점으로 계산합니다</li>
                </ul>
            </div>
        </div>

        <%-- 라이선스는 각 라이브러리의 pom 에 적힌 것을 그대로 옮겼다.
             전에 'jQuery & JSTL : MIT / Apache 2.0' 으로 묶여 있었는데
             JSTL 은 EPL 2.0 이고, LGPL 인 MariaDB 드라이버는 아예 빠져 있었다.

             Spring Boot 는 실행 jar 안에 의존 라이브러리를 전부 넣는다.
             그 jar 를 넘기는 순간 배포라, 라이선스 전문을 같이 줘야 한다.
             전문을 통째로 싣는 대신 원문 주소를 건다. --%>
        <div class="copyright-section">
            <h2 class="section-category">오픈소스 라이브러리 (Open Source)</h2>

            <div class="copyright-card">
                <h3 class="card-item-title">Apache License 2.0</h3>
                <ul class="card-info-list">
                    <li>Spring Boot · Spring Framework</li>
                    <li>MyBatis</li>
                    <li>Apache Tomcat (Jasper)</li>
                    <li><a class="license-link" href="https://www.apache.org/licenses/LICENSE-2.0"
                           target="_blank" rel="noopener noreferrer">전문 보기</a></li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">MIT License</h3>
                <ul class="card-info-list">
                    <li>jQuery</li>
                    <li>Project Lombok</li>
                    <li><a class="license-link" href="https://opensource.org/license/mit"
                           target="_blank" rel="noopener noreferrer">전문 보기</a></li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">Eclipse Public License 2.0</h3>
                <ul class="card-info-list">
                    <li>Jakarta Standard Tag Library (JSTL)</li>
                    <li><a class="license-link" href="https://www.eclipse.org/legal/epl-2.0/"
                           target="_blank" rel="noopener noreferrer">전문 보기</a></li>
                </ul>
            </div>

            <div class="copyright-card">
                <h3 class="card-item-title">LGPL 2.1 or later</h3>
                <ul class="card-info-list">
                    <li>MariaDB Connector/J</li>
                    <li><a class="license-link" href="https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html"
                           target="_blank" rel="noopener noreferrer">전문 보기</a></li>
                </ul>
            </div>
        </div>

    </div>
</main>

</body>
</html>
