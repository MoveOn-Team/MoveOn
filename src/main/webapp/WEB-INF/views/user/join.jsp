<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>회원가입 - MOVEON</title>

    <!-- FontAwesome 아이콘 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

    <!-- CSS -->
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/join.css">
</head>
<body>

<div class="page">
    <!-- 상단 바 -->
    <header class="topbar">
        <button type="button" class="topbar-back" onclick="history.back()">
            <i class="fa-solid fa-chevron-left"></i>
        </button>
        <div class="topbar-title"></div>
        <div class="topbar-spacer"></div>
    </header>

    <h1 class="page-title">회원가입</h1>

    <form id="joinForm" action="/user/join-proc" method="POST" class="mt-24">

        <!-- 이름 -->
        <div class="field">
            <label for="userName" class="field-label">이름 <span class="required">*</span></label>
            <input type="text" id="userName" name="userName" class="input underline-input" placeholder="김백호" required>
        </div>

        <!-- 아이디 -->
        <div class="field">
            <label for="userId" class="field-label">아이디 <span class="required">*</span></label>
            <input type="text" id="userId" name="userId" class="input underline-input" placeholder="moveon123" required>
            <div id="idHelp" class="help"></div>
        </div>

        <!-- 이메일 주소 -->
        <div class="field">
            <label for="email" class="field-label">이메일 주소 <span class="required">*</span></label>
            <div class="input-row">
                <input type="email" id="email" name="email" class="input underline-input" placeholder="이메일 주소를 입력하세요" required>
                <button type="button" id="btnSendAuth" class="btn btn-primary btn-sm">인증요청</button>
            </div>
        </div>

        <!-- 인증 번호 -->
        <div class="field">
            <label for="authCode" class="field-label">인증 번호 <span class="required">*</span></label>
            <div class="input-row">
                <input type="text" id="authCode" name="authCode" class="input underline-input" placeholder="인증 번호를 입력하세요" required>
                <button type="button" id="btnVerifyAuth" class="btn btn-outline btn-sm">확인</button>
            </div>
            <div id="authHelp" class="help"></div>
        </div>

        <!-- 비밀번호 -->
        <div class="field">
            <label for="userPw" class="field-label">비밀번호 <span class="required">*</span></label>
            <div class="input-wrap">
                <input type="password" id="userPw" name="userPw" class="input underline-input" placeholder="8~16자리로 영문, 숫자, 특수문자 포함" required>
                <button type="button" class="input-eye" data-target="userPw">
                    <i class="fa-regular fa-eye-slash"></i>
                </button>
            </div>
            <div id="pwHelp" class="help"></div>
        </div>

        <!-- 비밀번호 확인 -->
        <div class="field">
            <label for="userPwConfirm" class="field-label">비밀번호 확인 <span class="required">*</span></label>
            <div class="input-wrap">
                <input type="password" id="userPwConfirm" class="input underline-input" placeholder="비밀번호 확인" required>
                <button type="button" class="input-eye" data-target="userPwConfirm">
                    <i class="fa-regular fa-eye-slash"></i>
                </button>
            </div>
            <div id="pwConfirmHelp" class="help"></div>
        </div>

        <!-- 약관 동의 -->
        <div class="terms-container mt-24">
            <label class="checkbox-label check-all">
                <input type="checkbox" id="checkAll">
                <span class="checkbox-custom"></span>
                <span class="checkbox-text"><strong>모두 동의합니다</strong></span>
            </label>

            <div class="terms-list mt-8">
                <label class="checkbox-label item">
                    <input type="checkbox" class="term-check required-check" required>
                    <i class="fa-solid fa-check check-icon"></i>
                    <span class="checkbox-text">[필수] 만 14세 이상입니다</span>
                </label>
                <label class="checkbox-label item">
                    <input type="checkbox" class="term-check required-check" required>
                    <i class="fa-solid fa-check check-icon"></i>
                    <span class="checkbox-text">[필수] 이용약관 동의</span>
                </label>
                <label class="checkbox-label item">
                    <input type="checkbox" class="term-check">
                    <i class="fa-solid fa-check check-icon"></i>
                    <span class="checkbox-text">[선택] 개인정보 수집 및 이용 동의</span>
                </label>
            </div>
        </div>

        <!-- 가입하기 버튼 -->
        <button type="submit" class="btn btn-primary btn-block mt-24 btn-submit">가입하기</button>
    </form>
</div>

<!-- 완료 모달 -->
<div id="successModal" class="modal-overlay hidden">
    <div class="modal-card">
        <p class="modal-text">회원가입이 완료되었습니다.</p>
        <button type="button" id="btnModalConfirm" class="btn btn-primary btn-block">확인</button>
    </div>
</div>

<script src="/js/join.js"></script>
</body>
</html>