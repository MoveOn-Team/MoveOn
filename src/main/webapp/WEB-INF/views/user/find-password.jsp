<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON 비밀번호 찾기</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">
<main class="auth-shell">
    <!-- 비밀번호 찾기 화면 제목 영역 -->
    <header id="findHeader" class="auth-header">
        <a class="back-button" href="${pageContext.request.contextPath}/user/login" aria-label="로그인으로 돌아가기">‹</a>
        <h1>비밀번호 찾기</h1>
        <p>아이디와 가입한 이메일을 입력해 주세요.</p>
    </header>

    <!-- 아이디와 이메일 인증 입력 영역 -->
    <form id="findPasswordForm" class="auth-form find-form" novalidate>
        <div class="line-field">
            <label for="loginId">아이디 <em>*</em></label>
            <input id="loginId" name="loginId" type="text" autocomplete="username" placeholder="아이디를 입력해 주세요.">
        </div>
        <div class="line-field">
            <label for="email">이메일 <em>*</em></label>
            <div class="action-line">
                <input id="email" name="email" type="email" autocomplete="email" placeholder="이메일을 입력해 주세요.">
                <button id="sendEmailButton" class="line-button" type="button">인증 요청</button>
            </div>
            <p id="emailMessage" class="field-message"></p>
        </div>
        <div id="emailCodeField" class="line-field hidden-area">
            <label for="emailCode">인증번호 <em>*</em></label>
            <div class="action-line">
                <input id="emailCode" name="emailCode" type="text" inputmode="numeric" maxlength="6" placeholder="인증번호 6자리">
                <button id="verifyEmailButton" class="line-button filled" type="button">확인</button>
            </div>
            <p id="emailCodeMessage" class="field-message"></p>
        </div>
    </form>

    <!-- 새 비밀번호 입력 영역 -->
    <form id="resetPasswordForm" class="auth-form reset-form is-hidden" novalidate>
        <h2>새 비밀번호</h2>
        <p>영문, 숫자, 특수문자를 포함한 8~16자로 설정해 주세요.</p>
        <input id="resetLoginId" type="hidden">
        <input id="resetEmail" type="hidden">
        <div class="line-field">
            <label for="password">새 비밀번호 <em>*</em></label>
            <div class="password-line">
                <input id="password" type="password" autocomplete="new-password" placeholder="영문, 숫자, 특수문자 포함 8~16자">
                <button class="password-eye" type="button" data-password-toggle="#password" aria-label="새 비밀번호 보기"></button>
            </div>
            <p id="passwordMessage" class="field-message"></p>
        </div>
        <div class="line-field">
            <label for="passwordConfirm">새 비밀번호 확인 <em>*</em></label>
            <div class="password-line">
                <input id="passwordConfirm" type="password" autocomplete="new-password" placeholder="비밀번호를 다시 입력해 주세요.">
                <button class="password-eye" type="button" data-password-toggle="#passwordConfirm" aria-label="새 비밀번호 확인 보기"></button>
            </div>
            <p id="passwordConfirmMessage" class="field-message"></p>
        </div>
        <button id="resetPasswordButton" class="primary-button bottom-button" type="submit">비밀번호 변경</button>
    </form>
</main>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
<script>
    (function () {
        const contextPath = document.body.dataset.contextPath || "";
        const loginId = document.getElementById("loginId");
        const email = document.getElementById("email");
        const emailCode = document.getElementById("emailCode");
        const password = document.getElementById("password");
        const passwordConfirm = document.getElementById("passwordConfirm");
        let requestedEmail = "";
        let stopTimer = null;   // 인증번호 타이머를 멈추는 함수
        let verifiedEmail = "";

        // 회원 정보가 일치할 때 비밀번호 찾기 인증번호를 요청한다.
        document.getElementById("sendEmailButton").addEventListener("click", async function () {
            const message = document.getElementById("emailMessage");
            if (!loginId.value.trim() || !email.value.trim() || !email.checkValidity()) return moveOnAuth.showMessage("아이디와 올바른 이메일을 입력해 주세요.");
            try {
                const result = await moveOnAuth.post(contextPath + "/user/sendEmailCode", {loginId: loginId.value.trim(), email: email.value.trim(), purpose: "FIND_PW"});
                const success = (result.msg || "").includes("발송") && !(result.msg || "").includes("실패");
                moveOnAuth.setFieldMessage(message, result.msg, success ? "ok" : "error");
                if (success) {
                    requestedEmail = email.value.trim();
                    verifiedEmail = "";
                    document.getElementById("emailCodeField").classList.add("is-visible");
                    emailCode.focus();

                    // 서버가 5분을 재고 있으므로 화면에도 같은 시간을 보여준다
                    stopTimer = moveOnAuth.startCodeTimer(
                            document.getElementById("emailCodeMessage"), 5,
                            function () { verifiedEmail = ""; });
                } else moveOnAuth.showMessage(result.msg);
            } catch (error) {
                moveOnAuth.setFieldMessage(message, error.message, "error");
                moveOnAuth.showMessage(error.message);
            }
        });

        // 인증번호가 틀리면 밑줄과 메시지를 빨간색으로 표시한다.
        document.getElementById("verifyEmailButton").addEventListener("click", async function () {
            const message = document.getElementById("emailCodeMessage");
            if (email.value.trim() !== requestedEmail || !emailCode.value.trim()) {
                moveOnAuth.setFieldMessage(message, "인증번호를 입력해 주세요.", "error");
                return moveOnAuth.showMessage(message.textContent);
            }
            try {
                const result = await moveOnAuth.post(contextPath + "/user/verifyEmailCode", {email: email.value.trim(), emailCode: emailCode.value.trim(), purpose: "FIND_PW"});
                const success = (result.msg || "").includes("완료");
                verifiedEmail = success ? email.value.trim() : "";

                // 인증이 끝났으면 남은 시간 표시를 멈춘다.
                // 안 멈추면 성공 문구를 타이머가 1초 뒤 덮어쓴다.
                if (success && stopTimer) {
                    stopTimer();
                    stopTimer = null;
                }
                moveOnAuth.setFieldMessage(message, result.msg, success ? "ok" : "error");

                // 인증이 되면 새 비밀번호 단계로 바로 넘긴다.
                // 버튼을 한 번 더 누르게 하지 않는다.
                if (success) {
                    goToReset();
                } else {
                    moveOnAuth.showMessage(result.msg);
                }
            } catch (error) {
                verifiedEmail = "";
                moveOnAuth.setFieldMessage(message, error.message, "error");
                moveOnAuth.showMessage(error.message);
            }
        });

        // 인증된 아이디와 이메일이 존재하면 새 비밀번호 단계로 이동한다.
        // 인증 성공 직후에 바로 부른다.
        async function goToReset() {
            if (verifiedEmail !== email.value.trim()) return moveOnAuth.showMessage("이메일 인증을 완료해 주세요.");
            try {
                const result = await moveOnAuth.post(contextPath + "/user/searchPassword", {loginId: loginId.value.trim(), email: email.value.trim()});
                if (Number(result.exists) === 1) {
                    document.getElementById("resetLoginId").value = loginId.value.trim();
                    document.getElementById("resetEmail").value = email.value.trim();
                    document.getElementById("findHeader").classList.add("is-hidden");
                    document.getElementById("findPasswordForm").classList.add("is-hidden");
                    document.getElementById("resetPasswordForm").classList.remove("is-hidden");
                    document.getElementById("resetPasswordForm").classList.add("is-visible");
                } else moveOnAuth.showMessage("일치하는 회원 정보를 찾지 못했습니다.");
            } catch (error) {
                moveOnAuth.showMessage(error.message);
            }
        }


        // 새 비밀번호를 백엔드와 같은 규칙으로 확인한다.
        function validatePassword() {
            const passwordOk = moveOnAuth.isValidPassword(password.value);
            const confirmOk = password.value === passwordConfirm.value && passwordConfirm.value !== "";
            moveOnAuth.setFieldMessage(
                document.getElementById("passwordMessage"),
                moveOnAuth.getPasswordMessage(password.value),
                password.value ? (passwordOk ? "ok" : "error") : ""
            );
            moveOnAuth.setFieldMessage(document.getElementById("passwordConfirmMessage"), passwordConfirm.value ? (confirmOk ? "비밀번호가 일치합니다." : "비밀번호가 일치하지 않습니다.") : "", passwordConfirm.value ? (confirmOk ? "ok" : "error") : "");
            return passwordOk && confirmOk;
        }
        password.addEventListener("input", validatePassword);
        passwordConfirm.addEventListener("input", validatePassword);

        // 새 비밀번호를 현재 백엔드 필드명으로 전송한다.
        document.getElementById("resetPasswordForm").addEventListener("submit", async function (event) {
            event.preventDefault();
            if (!validatePassword()) return moveOnAuth.showMessage("새 비밀번호 형식을 확인해 주세요.");
            const button = document.getElementById("resetPasswordButton");
            button.disabled = true;
            try {
                const result = await moveOnAuth.post(contextPath + "/user/newPassword", {
                    loginId: document.getElementById("resetLoginId").value,
                    email: document.getElementById("resetEmail").value,
                    password: password.value,
                    passwordConfirm: passwordConfirm.value
                });
                const success = (result.msg || "").includes("변경되었습니다");
                moveOnAuth.showMessage(result.msg, success ? function () { location.href = contextPath + "/user/login"; } : null);
            } catch (error) {
                moveOnAuth.showMessage(error.message);
            } finally {
                button.disabled = false;
            }
        });
    }());
</script>
</body>
</html>
