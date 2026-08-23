<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원 정보 수정</title>
    <!-- 기존 auth.css -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <!-- 저번에 만들어둔 탭바 전용 CSS 링크 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tabbar.css">
</head>
<body>
<div class="container">

    <!-- 상단 헤더 -->
    <header class="edit-header">
        <a href="${pageContext.request.contextPath}/user/myPage" class="btn-back">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#8E8E93" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M15 18l-6-6 6-6"/>
            </svg>
        </a>
        <h1 class="header-title">회원 정보 수정</h1>
        <div class="header-empty"></div>
    </header>

    <!-- 프로필 수정 폼 -->
    <form action="${pageContext.request.contextPath}/user/profileEdit" method="post" class="edit-content-form">

        <!-- 1. 기본 정보 -->
        <div class="section-group">
            <h2 class="group-title">기본 정보</h2>
            <div class="edit-input-card">
                <label class="card-label">이름</label>
                <input type="text" name="userName" id="userName" class="card-input" value="${user.name}" placeholder="이름 입력">
            </div>
        </div>

        <!-- 2. 신체 정보 -->
        <div class="section-group">
            <h2 class="group-title">신체 정보</h2>
            <div class="edit-input-card">
                <label class="card-label">키 (cm)</label>
                <input type="number" name="height" id="height" class="card-input" value="${user.height}" placeholder="170">
            </div>

            <div class="edit-input-card mt-12">
                <label class="card-label">몸무게 (kg)</label>
                <input type="number" name="weight" id="weight" class="card-input" value="${user.weight}" placeholder="65">
            </div>
        </div>

        <!-- 3. 산출된 BMI 카드 -->
        <div class="bmi-result-card">
            <div class="bmi-card-header">
                <span class="bmi-title">산출된 BMI</span>
                <span class="bmi-status-badge" id="bmiStatusBadge">정상 체중</span>
            </div>
            <div class="bmi-value-text" id="bmiValueText">22.5</div>

            <!-- BMI 게이지 바 -->
            <div class="bmi-gauge-container">
                <div class="bmi-gauge-bar">
                    <div class="bmi-pointer" id="bmiPointer"></div>
                </div>
                <div class="bmi-labels">
                    <span>저체중</span>
                    <span>정상</span>
                    <span>과체중</span>
                    <span>비만</span>
                </div>
            </div>
        </div>

        <!-- 하단 버튼 영역 -->
        <div class="edit-bottom-actions">
            <a href="${pageContext.request.contextPath}/user/myPage" class="btn-cancel">취소</a>
            <button type="submit" class="btn-save">저장</button>
        </div>

    </form>

    <!-- 하단 탭바 -->
    <jsp:include page="/WEB-INF/views/common/tabbar.jsp" />
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const heightInput = document.getElementById('height');
        const weightInput = document.getElementById('weight');
        const bmiValueText = document.getElementById('bmiValueText');
        const bmiStatusBadge = document.getElementById('bmiStatusBadge');
        const bmiPointer = document.getElementById('bmiPointer');

        function calculateBMI() {
            const h = parseFloat(heightInput.value);
            const w = parseFloat(weightInput.value);

            if (!h || !w || h <= 0 || w <= 0) {
                bmiValueText.textContent = '-';
                return;
            }

            const heightInMeters = h / 100;
            const bmi = (w / (heightInMeters * heightInMeters)).toFixed(1);
            bmiValueText.textContent = bmi;

            let status = '정상 체중';
            let percent = 35;

            if (bmi < 18.5) {
                status = '저체중';
                percent = Math.max(5, (bmi / 18.5) * 25);
            } else if (bmi >= 18.5 && bmi < 23) {
                status = '정상 체중';
                percent = 25 + ((bmi - 18.5) / 4.5) * 25;
            } else if (bmi >= 23 && bmi < 25) {
                status = '과체중';
                percent = 50 + ((bmi - 23) / 2) * 25;
            } else {
                status = '비만';
                percent = Math.min(95, 75 + ((bmi - 25) / 10) * 25);
            }

            bmiStatusBadge.textContent = status;
            bmiPointer.style.left = percent + '%';
        }

        heightInput.addEventListener('input', calculateBMI);
        weightInput.addEventListener('input', calculateBMI);
        calculateBMI();
    });
</script>
</body>
</html>