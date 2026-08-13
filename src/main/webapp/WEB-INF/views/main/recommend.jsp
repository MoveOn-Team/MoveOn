<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>맞춤 운동 추천 - MOVEON</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="/css/recommend.css">
</head>
<body>

<div class="mobile-container">
    <!-- ================= 1. 기본 추천 화면 ================= -->
    <div id="viewMainRecommend" class="content-body">
        <div class="top-header">
            <h1 class="page-title">맞춤 운동 추천</h1>
            <a href="/user/onboarding" class="btn-re-diagnose">다시 진단</a>
        </div>

        <!-- 사용자 성향 요약 카드 -->
        <div class="type-card">
            <div class="type-title">혼자서 꾸준히<br>실내 중강도 타입</div>
            <div class="type-tags">
                <span class="type-tag">동반자 · 혼자</span>
                <span class="type-tag">승부욕 · 낮음</span>
                <span class="type-tag">장소 · 실내</span>
                <span class="type-tag">강도 · 중</span>
            </div>
            <div class="type-divider"></div>
            <div class="type-subinfo">만 34세 · BMI 22.4 반영 · 강서구 화곡동 기준</div>
        </div>

        <div class="section-title">지속 적합도 TOP 3</div>

        <!-- TOP 3 운동 리스트 -->
        <div class="top-list">
            <!-- 1위 -->
            <div class="top-item rank-1" onclick="openDetail('1위', '헬스', '81%', 'MET 5.0', '혼자')">
                <div class="item-left">
                    <div class="rank-badge">1위</div>
                    <div class="item-name">헬스</div>
                </div>
                <div class="item-right">
                    <span class="match-rate">81%</span>
                    <span class="match-label">지속 적합도</span>
                </div>
            </div>

            <!-- 2위 -->
            <div class="top-item rank-2" onclick="openDetail('2위', '수영', '77%', 'MET 6.0', '혼자')">
                <div class="item-left">
                    <div class="rank-badge">2위</div>
                    <div class="item-name">수영</div>
                </div>
                <div class="item-right">
                    <span class="match-rate">77%</span>
                    <span class="match-label">지속 적합도</span>
                </div>
            </div>

            <!-- 3위 -->
            <div class="top-item rank-3" onclick="openDetail('3위', '골프', '73%', 'MET 4.5', '2인 이상')">
                <div class="item-left">
                    <div class="rank-badge">3위</div>
                    <div class="item-name">골프</div>
                </div>
                <div class="item-right">
                    <span class="match-rate">73%</span>
                    <span class="match-label">지속 적합도</span>
                </div>
            </div>
        </div>
    </div>

    <!-- ================= 2. 추천 운동 상세 화면 ================= -->
    <div id="viewDetailRecommend" class="content-body" style="display: none;">
        <div class="detail-header">
            <button type="button" class="btn-back" onclick="closeDetail()"><i class="fa-solid fa-chevron-left"></i></button>
            <div id="detailRankTitle" class="detail-rank-title">지속 적합도 1위</div>
            <div style="width: 20px;"></div><!-- 여백 맞춤용 -->
        </div>

        <h1 id="detailSportName" class="detail-main-title">헬스</h1>
        <p class="detail-sub-title">혼자 · 내 페이스 · 실내 · 중강도에 잘 맞아요</p>

        <!-- 핵심 지표 3개 -->
        <div class="metric-group">
            <div class="metric-card">
                <div class="metric-label">지속 적합도</div>
                <div id="metricRate" class="metric-value">81%</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">강도</div>
                <div id="metricIntensity" class="metric-value">MET 5.0</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">최소 인원</div>
                <div id="metricMember" class="metric-value">혼자</div>
            </div>
        </div>

        <!-- 가까운 공공체육시설 3곳 -->
        <div class="section-title" style="font-size: 16px;">가까운 공공체육시설 3곳</div>

        <div class="facility-card active">
            <div>
                <div class="facility-name">강서구민올림픽체육센터</div>
                <div class="facility-addr">강서구 등촌동 707-3</div>
            </div>
            <span class="distance-badge">2.2km</span>
        </div>

        <div class="facility-card">
            <div>
                <div class="facility-name">가양레포츠센터</div>
                <div class="facility-addr">강서구 양천로 61길 101</div>
            </div>
            <span class="distance-badge">3.0km</span>
        </div>

        <div class="facility-card">
            <div>
                <div class="facility-name">마곡레포츠센터</div>
                <div class="facility-addr">강서구 양천로 251</div>
            </div>
            <span class="distance-badge">3.6km</span>
        </div>

        <!-- 운영 강좌 리스트 -->
        <div class="section-title" style="font-size: 16px; margin-top: 24px;">강서구민올림픽체육센터 운영 강좌</div>

        <div class="course-card">
            <div>
                <div class="course-name">1일 입장</div>
                <div class="course-info">성인/청소년 · 월~금 · 06:00~21:50</div>
            </div>
            <div class="course-price">3,500원</div>
        </div>

        <div class="course-card">
            <div>
                <div class="course-name">헬스 + 수영</div>
                <div class="course-info">성인 · 월~토</div>
            </div>
            <div class="course-price">63,000원</div>
        </div>

        <!-- 하단 액션 버튼 -->
        <div class="action-btn-group">
            <button type="button" class="btn-outline">길찾기</button>
            <button type="button" class="btn-solid">예약페이지로 이동</button>
        </div>
    </div>

    <!-- 하단 공통 탭바 -->
    <nav class="bottom-nav">
        <a href="/user/recommend" class="nav-item active">
            <i class="fa-solid fa-bullseye"></i>
            <span>추천</span>
        </a>
        <a href="#" class="nav-item">
            <i class="fa-solid fa-person-running"></i>
            <span>즉시운동</span>
        </a>
        <a href="#" class="nav-item">
            <i class="fa-regular fa-calendar"></i>
            <span>행사</span>
        </a>
        <a href="#" class="nav-item">
            <i class="fa-regular fa-user"></i>
            <span>내정보</span>
        </a>
    </nav>
</div>

<script>
    // 상세 화면 열기
    function openDetail(rankText, name, matchRate, intensity, member) {
        document.getElementById('detailRankTitle').innerText = '지속 적합도 ' + rankText;
        document.getElementById('detailSportName').innerText = name;
        document.getElementById('metricRate').innerText = matchRate;
        document.getElementById('metricIntensity').innerText = intensity;
        document.getElementById('metricMember').innerText = member;

        document.getElementById('viewMainRecommend').style.display = 'none';
        document.getElementById('viewDetailRecommend').style.display = 'block';
        window.scrollTo(0, 0);
    }

    // 목록 화면으로 돌아가기
    function closeDetail() {
        document.getElementById('viewDetailRecommend').style.display = 'none';
        document.getElementById('viewMainRecommend').style.display = 'block';
        window.scrollTo(0, 0);
    }
</script>
</body>
</html>