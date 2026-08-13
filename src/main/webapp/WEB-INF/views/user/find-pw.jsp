<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>비밀번호 찾기 - MOVEON</title>

    <!-- FontAwesome 아이콘 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

    <!-- CSS -->
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/find-pw.css">
</head>
<body>

<div class="page">

    <!-- STEP 1: 비밀번호 찾기 (아이디/이메일 입력 및 인증) -->
    <div id="stepFindPw" class="step-container">
        <header class="topbar">
            <button type="button" class="topbar-back" onclick="history.back()">
                <i class="fa-solid fa-chevron-left"></i>
            </button>
            <div class="topbar-title"></div>
            <div class="topbar-spacer"></div>
        </header>

        <h1 class="page-title">비밀번호 찾기</h1>
        <p class="page-subtext">회원 가입 시 입력한 아이디와 이메일 주소를 입력해 주세요</p>

        <form id="findPwForm" class="mt-24">
            <!-- 아이디 -->
            <div class="field">
                <label for="userId" class="field-label">아이디 <span class="required">*</span></label>
                <input type="text" id="userId" name="userId" class="input underline-input" placeholder="move123" required>
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

            <!-- 하단 변경하기 버튼 -->
            <div class="bottom-action">
                <button type="button" id="btnToReset" class="btn btn-primary btn-block btn-submit">변경하기</button>
            </div>
        </form>
    </div>

    <!-- STEP 2: 비밀번호 재설정 -->
    <div id="stepResetPw" class="step-container hidden">
        <header class="topbar">
            <button type="button" class="topbar-back" id="btnResetBack">
                <i class="fa-solid fa-chevron-left"></i>
            </button>
            <div class="topbar-title"></div>
            <div class="topbar-spacer"></div>
        </header>

        <h1 class="page-title">비밀번호 재설정</h1>

        <form id="resetPwForm" class="mt-32">
            <!-- 새 비밀번호 -->
            <div class="field">
                <label for="newPassword" class="field-label">새 비밀번호 <span class="required">*</span></label>
                <div class="input-with-icon">
                    <input type="password" id="newPassword" class="input underline-input" placeholder="8~16자로 영문, 숫자, 특수문자 포함" required>
                    <button type="button" class="toggle-pw-btn" data-target="newPassword">
                        <i class="fa-regular fa-eye-slash"></i>
                    </button>
                </div>
            </div>

            <!-- 새 비밀번호 확인 -->
            <div class="field">
                <label for="confirmPassword" class="field-label">새 비밀번호 확인 <span class="required">*</span></label>
                <div class="input-with-icon">
                    <input type="password" id="confirmPassword" class="input underline-input" placeholder="비밀번호 확인" required>
                    <button type="button" class="toggle-pw-btn" data-target="confirmPassword">
                        <i class="fa-regular fa-eye-slash"></i>
                    </button>
                </div>
                <div id="pwHelp" class="help"></div>
            </div>

            <!-- 하단 변경하기 버튼 -->
            <div class="bottom-action">
                <button type="button" id="btnResetSubmit" class="btn btn-primary btn-block btn-submit">변경하기</button>
            </div>
        </form>
    </div>

</div>

<script src="/js/find-pw.js"></script>
</body>
</html>