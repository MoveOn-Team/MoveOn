<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON 로그인</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">
<main class="auth-shell login-shell">
    <!-- MOVE:ON 소개 영역 -->
    <header class="login-intro">
        <strong class="brand">MOVE<span>:ON</span></strong>
        <p>오늘의 움직임이<br>내일의 변화를 만듭니다.</p>
    </header>

    <!-- 아이디와 비밀번호 입력 영역 -->
    <form id="loginForm" class="auth-form" novalidate>
        <div id="loginIdField" class="line-field">
            <label for="loginId">아이디</label>
            <input id="loginId" name="loginId" type="text" autocomplete="username"
                   placeholder="아이디를 입력해 주세요." required>
            <p id="loginMessage" class="field-message"></p>
        </div>

        <div id="passwordField" class="line-field">
            <label for="password">비밀번호</label>
            <div class="password-line">
                <input id="password" name="password" type="password" autocomplete="current-password"
                       placeholder="비밀번호를 입력해 주세요." required>
                <button class="password-eye" type="button" data-password-toggle="#password"
                        aria-label="비밀번호 보기"></button>
            </div>
        </div>

        <button id="loginButton" class="primary-button login-button" type="submit">로그인</button>
        <a class="outline-button" href="${pageContext.request.contextPath}/user/join">회원가입</a>

        <!-- 계정 찾기 화면 이동 링크 -->
        <nav class="find-links" aria-label="계정 찾기">
            <a href="${pageContext.request.contextPath}/user/find-id">아이디 찾기</a>
            <span>|</span>
            <a href="${pageContext.request.contextPath}/user/find-password">비밀번호 찾기</a>
        </nav>
    </form>
</main>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
<script>
    document.getElementById("loginForm").addEventListener("submit", async function (event) {
        event.preventDefault();
        const contextPath = document.body.dataset.contextPath || "";
        const loginId = document.getElementById("loginId").value.trim();
        const password = document.getElementById("password").value;
        const message = document.getElementById("loginMessage");
        const button = document.getElementById("loginButton");

        // 필수 입력값을 먼저 확인한다.
        if (!loginId || !password) {
            moveOnAuth.setFieldMessage(message, "아이디와 비밀번호를 모두 입력해 주세요.", "error");
            moveOnAuth.showMessage(message.textContent);
            return;
        }

        button.disabled = true;
        try {
            const result = await moveOnAuth.post(contextPath + "/user/loginProc", {loginId, password});
            const success = (result.msg || "").includes("로그인되었습니다");

            // 성공은 팝업으로 알리고 바로 넘어가므로 입력칸 아래에는 남기지 않는다.
            // 남겨두면 뒤로가기로 돌아왔을 때 초록 줄과 문구가 그대로 보인다.
            moveOnAuth.setFieldMessage(message, success ? "" : result.msg, success ? "" : "error");

            if (success) {
                // 성향조사를 안 했으면 온보딩부터, 마쳤으면 맞춤 추천으로 보낸다.
                const next = result.onboardingCompleted
                    ? contextPath + "/recommend/recommendList"
                    : contextPath + "/user/onboarding-page";
                moveOnAuth.showMessage(result.msg, function () { location.href = next; });
            } else {
                moveOnAuth.showMessage(result.msg);
            }
        } catch (error) {
            moveOnAuth.setFieldMessage(message, error.message, "error");
            moveOnAuth.showMessage(error.message);
        } finally {
            button.disabled = false;
        }
    });
</script>

<jsp:include page="/WEB-INF/views/common/tabbar.jsp" />
</body>
</html>
