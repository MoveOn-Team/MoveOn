<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>아이디 찾기 - MOVEON</title>

    <!-- FontAwesome 아이콘 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

    <!-- CSS -->
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/find-id.css">
</head>
<body>

<div class="page">

    <!-- STEP 1: 아이디 찾기 정보 입력 및 인증 -->
    <div id="stepInput" class="step-container">
        <header class="topbar">
            <button type="button" class="topbar-back" onclick="history.back()">
                <i class="fa-solid fa-chevron-left"></i>
            </button>
            <div class="topbar-title"></div>
            <div class="topbar-spacer"></div>
        </header>

        <h1 class="page-title">아이디 찾기</h1>
        <p class="page-subtext">회원 가입 시 입력한 이름과 이메일 주소를 입력해 주세요</p>

        <form id="findIdForm" class="mt-24">
            <!-- 이름 -->
            <div class="field">
                <label for="userName" class="field-label">이름 <span class="required">*</span></label>
                <input type="text" id="userName" name="userName" class="input underline-input" placeholder="김백호" required>
            </div>

            <!-- 이메일 주소 -->
            <div class="field">
                <label for="email" class="field-label">이메일 주소 <span class="required">*</span></label>
                <div class="input-row">
                    <input type="email" id="email" name="email" class="input underline-input" placeholder="이메일 주소를 입력하세요" required>
                    <button type="button" id="btnSendAuth" class="btn btn-primary btn-sm">인증요청</button>
                </div>
            </div>

            <!-- 인증 번호 (인증요청 클릭 시 노출) -->
            <div id="authGroup" class="field hidden">
                <label for="authCode" class="field-label">인증 번호 <span class="required">*</span></label>
                <div class="input-row">
                    <input type="text" id="authCode" class="input underline-input" placeholder="인증 번호를 입력하세요">
                    <button type="button" id="btnVerifyAuth" class="btn btn-outline btn-sm">확인</button>
                </div>
                <div id="authHelp" class="help"></div>
            </div>

            <!-- 다음 버튼 -->
            <div class="bottom-action">
                <button type="button" id="btnNext" class="btn btn-primary btn-block btn-submit">다음</button>
            </div>
        </form>
    </div>

    <!-- STEP 2: 아이디 찾기 결과 화면 -->
    <div id="stepResult" class="step-container hidden">
        <header class="topbar">
            <button type="button" class="topbar-back" id="btnResultBack">
                <i class="fa-solid fa-chevron-left"></i>
            </button>
            <div class="topbar-title"></div>
            <div class="topbar-spacer"></div>
        </header>

        <h1 class="page-title">아이디 찾기 결과</h1>

        <div class="result-content">
            <p class="result-message">
                회원님의 이메일로<br>
                가입된 아이디가 있습니다.
            </p>

            <div class="result-box mt-24">
                <span id="foundUserId" class="result-id">moveon123</span>
                <span id="foundJoinDate" class="result-date">2025.08.25 가입</span>
            </div>
        </div>

        <div class="bottom-action">
            <p class="find-pw-prompt">
                비밀번호가 기억나지 않으세요? <a href="/user/find-pw" class="link-find-pw">비밀번호 찾기</a>
            </p>
            <button type="button" id="btnLogin" class="btn btn-primary btn-block btn-submit">로그인</button>
        </div>
    </div>

</div>

<script src="/js/find-id.js"></script>
</body>
</html>