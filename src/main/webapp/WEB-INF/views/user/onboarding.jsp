<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MoveOn - 성향 조사 및 신체정보</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">

<main class="auth-shell">
  <div class="onboarding-container">
    <!-- 상단 헤더 & 프로그래스 바 -->
    <div class="onboarding-header">
      <button type="button" class="back-button" id="btnBack" style="visibility: hidden;">&lt;</button>
      <div class="progress-bar-group">
        <div class="progress-step active" data-step="1"></div>
        <div class="progress-step" data-step="2"></div>
        <div class="progress-step" data-step="3"></div>
        <div class="progress-step" data-step="4"></div>
        <div class="progress-step" data-step="5"></div>
      </div>
    </div>

    <!-- Step 1: 신체 정보 -->
    <div class="step-content" id="step1">
      <div class="step-indicator">1 / 5 · 신체정보</div>
      <h2 class="step-title">기본 정보를 알려주세요</h2>
      <p class="step-description">나이·체형에 맞는 강도로 종목을 골라드려요</p>

      <div class="onboarding-body">
        <!-- 생년월일 -->
        <div class="line-field box-style">
          <label>생년월일</label>
          <div class="input-group">
            <input type="number" id="birthYear" placeholder="YYYY" min="1900" max="2026" maxlength="4"> <span class="unit">년</span>
            <input type="number" id="birthMonth" placeholder="MM" min="1" max="12" maxlength="2"> <span class="unit">월</span>
            <input type="number" id="birthDay" placeholder="DD" min="1" max="31" maxlength="2"> <span class="unit">일</span>
          </div>
        </div>

        <!-- 성별 선택 -->
        <div class="gender-select-group">
          <button type="button" class="gender-btn" data-gender="M">남성</button>
          <button type="button" class="gender-btn" data-gender="F">여성</button>
        </div>

        <!-- 키 -->
        <div class="line-field box-style">
          <label>키</label>
          <div class="input-group">
            <input type="number" id="height" placeholder="170" min="100" max="250">
            <span class="unit">cm</span>
          </div>
        </div>

        <!-- 몸무게 -->
        <div class="line-field box-style">
          <label>몸무게</label>
          <div class="input-group">
            <input type="number" id="weight" placeholder="65" min="30" max="200">
            <span class="unit">kg</span>
          </div>
        </div>

        <!-- BMI 계산 카드 -->
        <div class="bmi-card" id="bmiCard">
          <div class="bmi-header">
            <span class="bmi-title">산출된 BMI</span>
            <span class="bmi-status-badge" id="bmiStatusText">측정 대기</span>
          </div>
          <div class="bmi-value" id="bmiValueText">--.-</div>
          <div class="bmi-bar-wrapper">
            <div class="bmi-bar"></div>
            <div class="bmi-pointer" id="bmiPointer" style="left: 0%;"></div>
          </div>
          <div class="bmi-labels">
            <span>저체중</span>
            <span>정상</span>
            <span>과체중</span>
            <span>비만</span>
          </div>
        </div>

        <p class="bmi-notice">입력한 정보는 나중에 내 정보에서 수정할 수 있어요</p>
      </div>
    </div>

    <!-- Step 2: 동반자 (companion) -->
    <div class="step-content" id="step2" style="display: none;">
      <div class="step-indicator">2 / 5 · 동반자</div>
      <h2 class="step-title">운동은 주로<br>누구와 하고 싶으세요?</h2>
      <p class="step-description">답에 따라 추천 종목이 달라져요</p>

      <div class="onboarding-body option-group" data-name="companion">
        <label class="option-card">
          <input type="radio" name="companion" value="ALONE">
          <span class="radio-circle"></span>
          <span class="option-text">혼자가 편해요</span>
        </label>
        <label class="option-card">
          <input type="radio" name="companion" value="PAIR">
          <span class="radio-circle"></span>
          <span class="option-text">둘이서 같이 하고 싶어요</span>
        </label>
        <label class="option-card">
          <input type="radio" name="companion" value="GROUP">
          <span class="radio-circle"></span>
          <span class="option-text">여럿이 어울리는 게 좋아요</span>
        </label>
      </div>
    </div>

    <!-- Step 3: 승부욕 (competition) -->
    <div class="step-content" id="step3" style="display: none;">
      <div class="step-indicator">3 / 5 · 승부욕</div>
      <h2 class="step-title">경쟁하는 운동이<br>즐거우신가요?</h2>
      <p class="step-description">답에 따라 추천 종목이 달라져요</p>

      <div class="onboarding-body option-group" data-name="competition">
        <label class="option-card">
          <input type="radio" name="competition" value="OWN_PACE">
          <span class="radio-circle"></span>
          <span class="option-text">내 페이스대로 하고 싶어요</span>
        </label>
        <label class="option-card">
          <input type="radio" name="competition" value="ANY">
          <span class="radio-circle"></span>
          <span class="option-text">상관없어요</span>
        </label>
        <label class="option-card">
          <input type="radio" name="competition" value="WIN">
          <span class="radio-circle"></span>
          <span class="option-text">겨루는 게 제일 재밌어요</span>
        </label>
      </div>
    </div>

    <!-- Step 4: 장소 (place) -->
    <div class="step-content" id="step4" style="display: none;">
      <div class="step-indicator">4 / 5 · 장소</div>
      <h2 class="step-title">실내와 실외 중<br>어디가 더 좋으세요?</h2>
      <p class="step-description">답에 따라 추천 종목이 달라져요</p>

      <div class="onboarding-body option-group" data-name="place">
        <label class="option-card">
          <input type="radio" name="place" value="INDOOR">
          <span class="radio-circle"></span>
          <span class="option-text">날씨 상관없는 실내가 좋아요</span>
        </label>
        <label class="option-card">
          <input type="radio" name="place" value="ANY">
          <span class="radio-circle"></span>
          <span class="option-text">상관없어요</span>
        </label>
        <label class="option-card">
          <input type="radio" name="place" value="OUTDOOR">
          <span class="radio-circle"></span>
          <span class="option-text">바깥에서 하고 싶어요</span>
        </label>
      </div>
    </div>

    <!-- Step 5: 강도 (intensity) -->
    <div class="step-content" id="step5" style="display: none;">
      <div class="step-indicator">5 / 5 · 강도</div>
      <h2 class="step-title">어느 정도로<br>움직이고 싶으세요?</h2>
      <p class="step-description">답에 따라 추천 종목이 달라져요</p>

      <div class="onboarding-body option-group" data-name="intensity">
        <label class="option-card">
          <input type="radio" name="intensity" value="LIGHT">
          <span class="radio-circle"></span>
          <span class="option-text">가볍게 몸만 풀 정도</span>
        </label>
        <label class="option-card">
          <input type="radio" name="intensity" value="MODERATE">
          <span class="radio-circle"></span>
          <span class="option-text">적당히 땀이 날 정도</span>
        </label>
        <label class="option-card">
          <input type="radio" name="intensity" value="HARD">
          <span class="radio-circle"></span>
          <span class="option-text">숨이 찰 만큼 확실하게</span>
        </label>
      </div>
    </div>

    <!-- Step 6: 요약 및 최종 확인 -->
    <div class="step-content" id="step6" style="display: none;">
      <div class="step-indicator">입력 완료</div>
      <h2 class="step-title">이제 나에게 맞는<br>운동을 찾아볼게요</h2>
      <p class="step-description">성향과 BMI로 지속률 TOP3를 계산해요</p>

      <div class="onboarding-body summary-list">
        <div class="summary-item">
          <span class="summary-label">신체정보</span>
          <span class="summary-value" id="summaryBasic">--</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">신체정보</span>
          <span class="summary-value" id="summaryBmi">--</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">동반자</span>
          <span class="summary-value" id="summaryCompanion">--</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">승부욕</span>
          <span class="summary-value" id="summaryCompetition">--</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">장소</span>
          <span class="summary-value" id="summaryPlace">--</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">강도</span>
          <span class="summary-value" id="summaryIntensity">--</span>
        </div>
      </div>
    </div>

    <!-- 하단 버튼 영역 -->
    <div class="onboarding-footer">
      <button type="button" class="btn-prev" id="btnPrev" style="display: none;">이전</button>
      <button type="button" class="btn-next active" id="btnNext">다음</button>
    </div>
  </div>
</main>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
</body>
</html>
