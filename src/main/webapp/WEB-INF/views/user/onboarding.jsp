<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>성향 조사 - MOVEON</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/onboarding.css">
</head>
<body>

<div class="page">

    <!-- 상단 헤더 & 프로그래스 바 -->
    <header class="topbar">
        <button type="button" class="topbar-back" id="btnHeaderBack">
            <i class="fa-solid fa-chevron-left"></i>
        </button>
        <div class="topbar-title"></div>
        <div class="topbar-spacer"></div>
    </header>

    <div class="progress-bar-container">
        <div class="progress-step" id="bar1"></div>
        <div class="progress-step" id="bar2"></div>
        <div class="progress-step" id="bar3"></div>
        <div class="progress-step" id="bar4"></div>
        <div class="progress-step" id="bar5"></div>
    </div>

    <!-- STEP 1: 신체정보 -->
    <div id="step1" class="step-container">
        <div class="step-badge">1 / 5 · 신체정보</div>
        <h1 class="page-title">기본 정보를 알려주세요</h1>
        <p class="page-subtext">나이 · 체형에 맞는 강도로 종목을 골라드려요</p>

        <form class="survey-form">
            <!-- 생년월일 -->
            <div class="card-input-box">
                <span class="card-label">생년월일</span>
                <div class="birth-inputs">
                    <input type="number" id="birthYear" class="inline-input year" placeholder="YYYY" maxlength="4">
                    <span class="unit">년</span>
                    <input type="number" id="birthMonth" class="inline-input month" placeholder="MM" maxlength="2">
                    <span class="unit">월</span>
                    <input type="number" id="birthDay" class="inline-input day" placeholder="DD" maxlength="2">
                    <span class="unit">일</span>
                </div>
            </div>

            <!-- 성별 -->
            <div class="gender-group">
                <button type="button" class="gender-btn" data-gender="M">남성</button>
                <button type="button" class="gender-btn" data-gender="F">여성</button>
            </div>

            <!-- 키 -->
            <div class="card-input-box flex-between">
                <span class="card-label">키</span>
                <div class="input-with-unit">
                    <input type="number" id="heightInput" class="inline-input number-input" placeholder="">
                    <span class="unit">cm</span>
                </div>
            </div>

            <!-- 몸무게 -->
            <div class="card-input-box flex-between">
                <span class="card-label">몸무게</span>
                <div class="input-with-unit">
                    <input type="number" id="weightInput" class="inline-input number-input" placeholder="">
                    <span class="unit">kg</span>
                </div>
            </div>

            <!-- BMI 계산 영역 -->
            <div class="bmi-card" id="bmiCard">
                <!-- 미입력 상태 -->
                <div id="bmiEmpty" class="bmi-empty">
                    <span class="bmi-label">BMI</span>
                    <span class="bmi-placeholder">키와 몸무게를 입력해주세요</span>
                </div>

                <!-- 계산 완료 상태 -->
                <div id="bmiResult" class="bmi-result hidden">
                    <div class="bmi-header">
                        <div>
                            <span class="bmi-subtext">산출된 BMI</span>
                            <div class="bmi-value" id="bmiVal">22.4</div>
                        </div>
                        <span class="bmi-status-badge" id="bmiStatusBadge">정상 체중</span>
                    </div>

                    <!-- BMI 프로그래스 바 -->
                    <div class="bmi-bar-wrapper">
                        <div class="bmi-bar-bg"></div>
                        <div class="bmi-pointer" id="bmiPointer"></div>
                    </div>
                    <div class="bmi-scale-labels">
                        <span>저체중</span>
                        <span>정상</span>
                        <span>과체중</span>
                        <span>비만</span>
                    </div>
                </div>
            </div>

            <p class="notice-text">입력한 정보는 나중에 내 정보에서 수정할 수 있어요</p>

            <div class="bottom-action">
                <button type="button" class="btn btn-primary btn-block btn-next" data-next="2">다음</button>
            </div>
        </form>
    </div>

    <!-- STEP 2: 동반자 -->
    <div id="step2" class="step-container hidden">
        <div class="step-badge">2 / 5 · 동반자</div>
        <h1 class="page-title">운동은 주로<br>누구와 하고 싶으세요?</h1>
        <p class="page-subtext">답에 따라 추천 종목이 달라져요</p>

        <div class="option-list" data-step="companion">
            <div class="option-card" data-value="혼자가 편해요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>혼자가 편해요</span>
            </div>
            <div class="option-card" data-value="둘이서 같이 하고 싶어요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>둘이서 같이 하고 싶어요</span>
            </div>
            <div class="option-card" data-value="여럿이 어울리는 게 좋아요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>여럿이 어울리는 게 좋아요</span>
            </div>
        </div>

        <div class="bottom-action flex-buttons">
            <button type="button" class="btn btn-outline btn-prev" data-prev="1">이전</button>
            <button type="button" class="btn btn-primary btn-next" data-next="3">다음</button>
        </div>
    </div>

    <!-- STEP 3: 승부욕 -->
    <div id="step3" class="step-container hidden">
        <div class="step-badge">3 / 5 · 승부욕</div>
        <h1 class="page-title">경쟁하는 운동이<br>즐거우신가요?</h1>
        <p class="page-subtext">답에 따라 추천 종목이 달라져요</p>

        <div class="option-list" data-step="competition">
            <div class="option-card" data-value="내 페이스대로 하고 싶어요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>내 페이스대로 하고 싶어요</span>
            </div>
            <div class="option-card" data-value="상관없어요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>상관없어요</span>
            </div>
            <div class="option-card" data-value="겨루는 게 제일 재밌어요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>겨루는 게 제일 재밌어요</span>
            </div>
        </div>

        <div class="bottom-action flex-buttons">
            <button type="button" class="btn btn-outline btn-prev" data-prev="2">이전</button>
            <button type="button" class="btn btn-primary btn-next" data-next="4">다음</button>
        </div>
    </div>

    <!-- STEP 4: 장소 -->
    <div id="step4" class="step-container hidden">
        <div class="step-badge">4 / 5 · 장소</div>
        <h1 class="page-title">실내와 실외 중<br>어디가 더 좋으세요?</h1>
        <p class="page-subtext">답에 따라 추천 종목이 달라져요</p>

        <div class="option-list" data-step="place">
            <div class="option-card" data-value="날씨 상관없는 실내가 좋아요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>날씨 상관없는 실내가 좋아요</span>
            </div>
            <div class="option-card" data-value="상관없어요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>상관없어요</span>
            </div>
            <div class="option-card" data-value="바깥에서 하고 싶어요">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>바깥에서 하고 싶어요</span>
            </div>
        </div>

        <div class="bottom-action flex-buttons">
            <button type="button" class="btn btn-outline btn-prev" data-prev="3">이전</button>
            <button type="button" class="btn btn-primary btn-next" data-next="5">다음</button>
        </div>
    </div>

    <!-- STEP 5: 강도 -->
    <div id="step5" class="step-container hidden">
        <div class="step-badge">5 / 5 · 강도</div>
        <h1 class="page-title">어느 정도로<br>움직이고 싶으세요?</h1>
        <p class="page-subtext">답에 따라 추천 종목이 달라져요</p>

        <div class="option-list" data-step="intensity">
            <div class="option-card" data-value="가볍게 몸만 풀 정도">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>가볍게 몸만 풀 정도</span>
            </div>
            <div class="option-card" data-value="적당히 땀이 날 정도">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>적당히 땀이 날 정도</span>
            </div>
            <div class="option-card" data-value="숨이 찰 만큼 확실하게">
                <div class="radio-icon"><i class="fa-solid fa-check"></i></div>
                <span>숨이 찰 만큼 확실하게</span>
            </div>
        </div>

        <div class="bottom-action flex-buttons">
            <button type="button" class="btn btn-outline btn-prev" data-prev="4">이전</button>
            <button type="button" class="btn btn-primary btn-next" data-next="result">다음</button>
        </div>
    </div>

    <!-- RESULT STEP: 결과 요약 -->
    <div id="stepResult" class="step-container hidden">
        <div class="step-badge">입력 완료</div>
        <h1 class="page-title">이제 나에게 맞는<br>운동을 찾아볼게요</h1>
        <p class="page-subtext">성향과 BMI로 지속률 TOP3를 계산해요</p>

        <div class="summary-list">
            <div class="summary-card">
                <span class="summary-title">신체정보</span>
                <span class="summary-value" id="summaryAgeGender">만 33세 · 남성</span>
            </div>
            <div class="summary-card">
                <span class="summary-title">신체정보</span>
                <span class="summary-value" id="summaryBmiInfo">170cm · 65kg · BMI 22.5 정상</span>
            </div>
            <div class="summary-card">
                <span class="summary-title">동반자</span>
                <span class="summary-value" id="summaryCompanion">-</span>
            </div>
            <div class="summary-card">
                <span class="summary-title">승부욕</span>
                <span class="summary-value" id="summaryCompetition">-</span>
            </div>
            <div class="summary-card">
                <span class="summary-title">장소</span>
                <span class="summary-value" id="summaryPlace">-</span>
            </div>
            <div class="summary-card">
                <span class="summary-title">강도</span>
                <span class="summary-value" id="summaryIntensity">-</span>
            </div>
        </div>

        <div class="bottom-action flex-buttons">
            <button type="button" class="btn btn-outline" id="btnRestart">다시 선택</button>
            <button type="button" class="btn btn-primary" id="btnFinalSubmit">결과 확인</button>
        </div>
    </div>

</div>

<script src="/js/onboarding.js"></script>
</body>
</html>