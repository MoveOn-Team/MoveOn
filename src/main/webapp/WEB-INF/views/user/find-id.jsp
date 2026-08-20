<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>MOVE:ON 아이디 찾기</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=1.1">
</head>
<body class="auth-page" data-context-path="${pageContext.request.contextPath}">
<main class="auth-shell">
    <a class="back-button" href="${pageContext.request.contextPath}/user/login" aria-label="로그인으로 돌아가기">‹</a>

    <!-- 이름과 이메일 인증 입력 영역 -->
    <form id="findIdForm" class="auth-form find-form" novalidate>
        <header class="auth-header">
            <h1>아이디 찾기</h1>
            <p>가입할 때 사용한 이름과 이메일을 입력해 주세요.</p>
        </header>
        <div class="line-field">
            <label for="name">이름 <em>*</em></label>
            <input id="name" name="name" type="text" autocomplete="name" placeholder="이름을 입력해 주세요.">
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

    <!-- 찾은 아이디 결과 영역 -->
    <section id="findIdResult" class="auth-form is-hidden">
        <header class="auth-header">
            <h1>아이디를 찾았어요.</h1>
            <p>회원님의 MOVE:ON 아이디입니다.</p>
        </header>
        <div class="line-field">
            <label>아이디</label>
            <input id="foundLoginId" type="text" readonly>
        </div>
        <a class="primary-button" href="${pageContext.request.contextPath}/user/login">로그인 하기</a>
        <a class="outline-button" href="${pageContext.request.contextPath}/user/find-password">비밀번호 찾기</a>
    </section>
</main>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
<script>
    (function () {
        const contextPath = document.body.dataset.contextPath || "";
        const name = document.getElementById("name");
        const email = document.getElementById("email");
        const emailCode = document.getElementById("emailCode");
        let requestedEmail = "";
        let stopTimer = null;   // 인증번호 타이머를 멈추는 함수
        let verifiedEmail = "";

        // 회원 정보가 일치할 때 아이디 찾기 인증번호를 요청한다.
        document.getElementById("sendEmailButton").addEventListener("click", async function () {
            const message = document.getElementById("emailMessage");
            if (!name.value.trim() || !email.value.trim() || !email.checkValidity()) return moveOnAuth.showMessage("이름과 올바른 이메일을 입력해 주세요.");
            try {
                const result = await moveOnAuth.post(contextPath + "/user/sendEmailCode", {name: name.value.trim(), email: email.value.trim(), purpose: "FIND_ID"});
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
                const result = await moveOnAuth.post(contextPath + "/user/verifyEmailCode", {email: email.value.trim(), emailCode: emailCode.value.trim(), purpose: "FIND_ID"});
                const success = (result.msg || "").includes("완료");
                verifiedEmail = success ? email.value.trim() : "";

                // 인증이 끝났으면 남은 시간 표시를 멈춘다.
                // 안 멈추면 성공 문구를 타이머가 1초 뒤 덮어쓴다.
                if (success && stopTimer) {
                    stopTimer();
                    stopTimer = null;
                }
                moveOnAuth.setFieldMessage(message, result.msg, success ? "ok" : "error");

                // 인증이 되면 아이디를 바로 찾아 준다.
                // 버튼을 한 번 더 누르게 하지 않는다.
                if (success) {
                    findId();
                } else {
                    moveOnAuth.showMessage(result.msg);
                }
            } catch (error) {
                verifiedEmail = "";
                moveOnAuth.setFieldMessage(message, error.message, "error");
                moveOnAuth.showMessage(error.message);
            }
        });

        // 인증된 이름과 이메일로 아이디를 조회한다.
        // 인증 성공 직후에 바로 부른다.
        async function findId() {
            if (verifiedEmail !== email.value.trim()) return moveOnAuth.showMessage("이메일 인증을 완료해 주세요.");
            try {
                const result = await moveOnAuth.post(contextPath + "/user/searchUserId", {name: name.value.trim(), email: email.value.trim()});
                if (Number(result.exists) === 1 && result.loginId) {
                    document.getElementById("foundLoginId").value = result.loginId;
                    document.getElementById("findIdForm").classList.add("is-hidden");
                    document.getElementById("findIdResult").classList.remove("is-hidden");
                } else moveOnAuth.showMessage("일치하는 회원 정보를 찾지 못했습니다.");
            } catch (error) {
                moveOnAuth.showMessage(error.message);
            }
        }

    }());
</script>
</body>
</html>
