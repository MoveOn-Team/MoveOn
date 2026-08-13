<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>로그인 - MOVEON</title>

    <!-- FontAwesome 아이콘 CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

    <!-- CSS 불러오기 -->
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/login.css">
</head>
<body>

<div class="mobile-wrapper">
    <div class="login-card">

        <!-- 로고 및 슬로건 영역 -->
        <header class="login-header">
            <h1 class="logo-text">MOVE<span class="logo-point">:</span>ON</h1>
            <p class="slogan-text">내 성향과 현위치에 맞는<br>공공체육 운동을 찾아드려요</p>
        </header>

        <!-- 로그인 폼 영역 -->
        <form action="/user/login-proc" method="POST" class="login-form">

            <!-- 아이디 입력창 -->
            <div class="input-container">
                <label for="loginId" class="input-label">아이디</label>
                <div class="input-underline-group">
                    <input type="text" id="loginId" name="loginId" required placeholder="moveon" autocomplete="username">
                </div>
            </div>

            <!-- 비밀번호 입력창 -->
            <div class="input-container">
                <label for="password" class="input-label">비밀번호</label>
                <div class="input-underline-group password-group">
                    <input type="password" id="password" name="password" required placeholder="●●●●●●●●" autocomplete="current-password">
                    <button type="button" id="togglePassword" class="toggle-password-btn">
                        <i class="fa-regular fa-eye-slash"></i>
                    </button>
                </div>
            </div>

            <!-- 버튼 영역 -->
            <div class="button-group">
                <button type="submit" class="btn btn-primary">로그인</button>
                <a href="/user/join" class="btn btn-secondary">회원가입</a>
            </div>

        </form>

        <!-- 하단 링크 영역 -->
        <footer class="login-footer-links">
            <a href="/user/find-id">아이디 찾기</a>
            <span class="divider">|</span>
            <a href="/user/find-pw">비밀번호 찾기</a>
        </footer>

    </div>
</div>

<!-- 스크립트 불러오기 -->
<script src="/js/login.js"></script>

</body>
</html>