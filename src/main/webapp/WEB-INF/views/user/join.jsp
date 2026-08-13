<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    <title>MOVE:ON 회원가입</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">
<main class="auth-shell join-shell">
    <!-- 회원가입 화면 제목 영역 -->
    <header class="auth-header">
        <a class="back-button" href="${pageContext.request.contextPath}/login" aria-label="로그인으로 돌아가기">‹</a>
        <h1>회원가입</h1>
        <p>회원 정보를 입력하고 이메일 인증을 완료해 주세요.</p>
    </header>

    <!-- 회원가입 정보 입력 영역 -->
    <form id="joinForm" class="auth-form" novalidate>
        <div class="line-field">
            <label for="name">이름 <em>*</em></label>
            <input id="name" name="name" type="text" autocomplete="name" placeholder="이름을 입력해 주세요." required>
        </div>

        <div class="line-field">
            <label for="loginId">아이디 <em>*</em></label>
            <div class="action-line">
                <input id="loginId" name="loginId" type="text" autocomplete="username" placeholder="사용할 아이디">
                <button id="checkLoginIdButton" class="line-button" type="button">중복 확인</button>
            </div>
            <p id="loginIdMessage" class="field-message">아이디 중복 확인이 필요합니다.</p>
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

        <div class="line-field">
            <label for="password">비밀번호 <em>*</em></label>
            <div class="password-line">
                <input id="password" name="password" type="password" autocomplete="new-password" placeholder="영문, 숫자, 특수문자 포함 8~16자">
                <button class="password-eye" type="button" data-password-toggle="#password" aria-label="비밀번호 보기"></button>
            </div>
            <p id="passwordMessage" class="field-message"></p>
        </div>

        <div class="line-field">
            <label for="passwordConfirm">비밀번호 확인 <em>*</em></label>
            <div class="password-line">
                <input id="passwordConfirm" name="passwordConfirm" type="password" autocomplete="new-password" placeholder="비밀번호를 다시 입력해 주세요.">
                <button class="password-eye" type="button" data-password-toggle="#passwordConfirm" aria-label="비밀번호 확인 보기"></button>
            </div>
            <p id="passwordConfirmMessage" class="field-message"></p>
        </div>

        <!-- 필수 및 선택 약관 동의 영역 -->
        <div class="agreement-box">
            <label class="check-row all-check"><input id="agreeAll" type="checkbox">전체 동의</label>
            <label class="check-row"><input id="ageConfirmed" name="ageConfirmed" type="checkbox" value="true">[필수] 만 14세 이상입니다.</label>
            <label class="check-row"><input id="termsAgreed" name="termsAgreed" type="checkbox" value="true">[필수] 이용약관에 동의합니다.</label>
            <label class="check-row"><input id="privacyAgreed" name="privacyAgreed" type="checkbox" value="1">[선택] 개인정보 활용에 동의합니다.</label>
        </div>

        <button id="joinButton" class="primary-button" type="submit">가입하기</button>
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
        let checkedLoginId = "";
        let requestedEmail = "";
        let verifiedEmail = "";

        // 입력값이 바뀌면 기존 중복 확인과 이메일 인증 결과를 무효화한다.
        loginId.addEventListener("input", function () {
            if (loginId.value.trim() !== checkedLoginId) checkedLoginId = "";
        });
        email.addEventListener("input", function () {
            if (email.value.trim() !== verifiedEmail) verifiedEmail = "";
            if (email.value.trim() !== requestedEmail) document.getElementById("emailCodeField").classList.remove("is-visible");
        });

        // 백엔드의 1과 0 결과로 아이디 중복 여부를 확인한다.
        document.getElementById("checkLoginIdButton").addEventListener("click", async function () {
            const value = loginId.value.trim();
            const message = document.getElementById("loginIdMessage");
            if (!value) return moveOnAuth.showMessage("아이디를 먼저 입력해 주세요.");
            try {
                const result = await moveOnAuth.post(contextPath + "/user/getUserIdExists", {loginId: value});
                const available = Number(result.exists) === 0;
                checkedLoginId = available ? value : "";
                moveOnAuth.setFieldMessage(message, available ? "사용할 수 있는 아이디입니다." : "이미 사용 중인 아이디입니다.", available ? "ok" : "error");
                if (!available) moveOnAuth.showMessage(message.textContent);
            } catch (error) {
                moveOnAuth.setFieldMessage(message, error.message, "error");
                moveOnAuth.showMessage(error.message);
            }
        });

        // 회원가입용 이메일 인증번호를 요청한다.
        document.getElementById("sendEmailButton").addEventListener("click", async function () {
            const value = email.value.trim();
            const message = document.getElementById("emailMessage");
            if (!value || !email.checkValidity()) return moveOnAuth.showMessage("올바른 이메일을 입력해 주세요.");
            try {
                const result = await moveOnAuth.post(contextPath + "/user/sendEmailCode", {email: value, purpose: "JOIN"});
                const success = (result.msg || "").includes("발송") && !(result.msg || "").includes("실패");
                moveOnAuth.setFieldMessage(message, result.msg, success ? "ok" : "error");
                if (success) {
                    requestedEmail = value;
                    verifiedEmail = "";
                    document.getElementById("emailCodeField").classList.add("is-visible");
                    emailCode.focus();
                } else moveOnAuth.showMessage(result.msg);
            } catch (error) {
                moveOnAuth.setFieldMessage(message, error.message, "error");
                moveOnAuth.showMessage(error.message);
            }
        });

        // 인증번호가 틀리면 밑줄과 메시지를 빨간색으로 표시한다.
        document.getElementById("verifyEmailButton").addEventListener("click", async function () {
            const message = document.getElementById("emailCodeMessage");
            if (!emailCode.value.trim()) {
                moveOnAuth.setFieldMessage(message, "인증번호를 입력해 주세요.", "error");
                return moveOnAuth.showMessage(message.textContent);
            }
            try {
                const result = await moveOnAuth.post(contextPath + "/user/verifyEmailCode", {email: email.value.trim(), emailCode: emailCode.value.trim(), purpose: "JOIN"});
                const success = (result.msg || "").includes("완료");
                verifiedEmail = success ? email.value.trim() : "";
                moveOnAuth.setFieldMessage(message, result.msg, success ? "ok" : "error");
                moveOnAuth.showMessage(result.msg);
            } catch (error) {
                verifiedEmail = "";
                moveOnAuth.setFieldMessage(message, error.message, "error");
                moveOnAuth.showMessage(error.message);
            }
        });

        // 비밀번호 형식을 백엔드와 같은 규칙으로 확인한다.
        function validatePassword() {
            const passwordOk = moveOnAuth.isValidPassword(password.value);
            const confirmOk = password.value === passwordConfirm.value && passwordConfirm.value !== "";
            moveOnAuth.setFieldMessage(document.getElementById("passwordMessage"), password.value ? (passwordOk ? "사용할 수 있는 비밀번호입니다." : "영문, 숫자, 특수문자를 포함한 8~16자로 입력해 주세요.") : "", password.value ? (passwordOk ? "ok" : "error") : "");
            moveOnAuth.setFieldMessage(document.getElementById("passwordConfirmMessage"), passwordConfirm.value ? (confirmOk ? "비밀번호가 일치합니다." : "비밀번호가 일치하지 않습니다.") : "", passwordConfirm.value ? (confirmOk ? "ok" : "error") : "");
            return passwordOk && confirmOk;
        }
        password.addEventListener("input", validatePassword);
        passwordConfirm.addEventListener("input", validatePassword);

        // 전체 동의 체크 상태를 세 약관에 함께 적용한다.
        const agreements = [document.getElementById("ageConfirmed"), document.getElementById("termsAgreed"), document.getElementById("privacyAgreed")];
        document.getElementById("agreeAll").addEventListener("change", function (event) {
            agreements.forEach(function (input) { input.checked = event.target.checked; });
        });

        // 검증이 끝난 회원 정보를 현재 백엔드 필드명으로 전송한다.
        document.getElementById("joinForm").addEventListener("submit", async function (event) {
            event.preventDefault();
            const name = document.getElementById("name").value.trim();
            if (!name) return moveOnAuth.showMessage("이름을 입력해 주세요.");
            if (checkedLoginId !== loginId.value.trim()) return moveOnAuth.showMessage("아이디 중복 확인을 완료해 주세요.");
            if (verifiedEmail !== email.value.trim()) return moveOnAuth.showMessage("이메일 인증을 완료해 주세요.");
            if (!validatePassword()) return moveOnAuth.showMessage("비밀번호 형식을 확인해 주세요.");
            if (!agreements[0].checked || !agreements[1].checked) return moveOnAuth.showMessage("필수 약관에 동의해 주세요.");

            const button = document.getElementById("joinButton");
            button.disabled = true;
            try {
                const result = await moveOnAuth.post(contextPath + "/user/insertUserInfo", {
                    name: name,
                    loginId: loginId.value.trim(),
                    email: email.value.trim(),
                    password: password.value,
                    passwordConfirm: passwordConfirm.value,
                    ageConfirmed: "true",
                    termsAgreed: "true",
                    privacyAgreed: agreements[2].checked ? "1" : "0"
                });
                const success = (result.msg || "").includes("회원가입") && (result.msg || "").includes("완료");
                moveOnAuth.showMessage(result.msg, success ? function () { location.href = contextPath + "/login"; } : null);
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
