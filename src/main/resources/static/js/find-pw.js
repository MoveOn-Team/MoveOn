document.addEventListener('DOMContentLoaded', () => {

    const btnSendAuth = document.getElementById('btnSendAuth');
    const authGroup = document.getElementById('authGroup');
    const btnVerifyAuth = document.getElementById('btnVerifyAuth');
    const authCodeInput = document.getElementById('authCode');
    const authHelp = document.getElementById('authHelp');

    const btnToReset = document.getElementById('btnToReset');
    const stepFindPw = document.getElementById('stepFindPw');
    const stepResetPw = document.getElementById('stepResetPw');

    const btnResetBack = document.getElementById('btnResetBack');
    const btnResetSubmit = document.getElementById('btnResetSubmit');
    const pwHelp = document.getElementById('pwHelp');

    let isVerified = false; // 이메일 인증 통과 여부

    // 1. [인증요청] 버튼 클릭 -> 인증 번호 입력란 노출
    btnSendAuth.addEventListener('click', () => {
        const userIdVal = document.getElementById('userId').value.trim();
        const emailVal = document.getElementById('email').value.trim();

        if (!userIdVal) {
            alert('아이디를 입력해 주세요.');
            return;
        }
        if (!emailVal) {
            alert('이메일 주소를 입력해 주세요.');
            return;
        }

        authGroup.classList.remove('hidden');
        authCodeInput.focus();
    });

    // 2. [확인] 버튼 클릭 -> 인증 번호 검증 (테스트 모크)
    btnVerifyAuth.addEventListener('click', () => {
        const code = authCodeInput.value.trim();

        authCodeInput.classList.remove('is-error', 'is-ok');
        authHelp.classList.remove('error', 'ok');

        if (code === '') {
            authCodeInput.classList.add('is-error');
            authHelp.classList.add('error');
            authHelp.textContent = '인증번호를 입력해 주세요.';
            isVerified = false;
            return;
        }

        // 테스트 기준: '123456' 입력 시 실패, 그 외('025165' 등) 입력 시 성공
        if (code === '123456') {
            authCodeInput.classList.add('is-error');
            authHelp.classList.add('error');
            authHelp.textContent = '인증번호가 일치하지 않습니다';
            isVerified = false;
        } else {
            authCodeInput.classList.add('is-ok');
            authHelp.classList.add('ok');
            authHelp.textContent = '인증번호가 일치합니다';
            isVerified = true;
        }
    });

    // 3. STEP 1 [변경하기] 버튼 클릭 -> 비밀번호 재설정 화면으로 전환
    btnToReset.addEventListener('click', () => {
        const userIdVal = document.getElementById('userId').value.trim();
        const emailVal = document.getElementById('email').value.trim();

        if (!userIdVal || !emailVal) {
            alert('아이디와 이메일 주소를 입력해 주세요.');
            return;
        }

        if (!isVerified) {
            alert('이메일 인증을 완료해 주세요.');
            return;
        }

        stepFindPw.classList.add('hidden');
        stepResetPw.classList.remove('hidden');
    });

    // 4. STEP 2 상단 뒤로가기 버튼 클릭 -> STEP 1 화면으로 복귀
    btnResetBack.addEventListener('click', () => {
        stepResetPw.classList.add('hidden');
        stepFindPw.classList.remove('hidden');
    });

    // 5. 비밀번호 보이기/숨기기 토글
    document.querySelectorAll('.toggle-pw-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const inputField = document.getElementById(targetId);
            const icon = this.querySelector('i');

            if (inputField.type === 'password') {
                inputField.type = 'text';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            } else {
                inputField.type = 'password';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            }
        });
    });

    // 6. STEP 2 [변경하기] 버튼 클릭 -> 최종 비밀번호 변경
    btnResetSubmit.addEventListener('click', () => {
        const newPw = document.getElementById('newPassword').value;
        const confirmPw = document.getElementById('confirmPassword').value;

        pwHelp.textContent = '';
        pwHelp.classList.remove('error', 'ok');

        if (!newPw || !confirmPw) {
            alert('새 비밀번호를 입력해 주세요.');
            return;
        }

        if (newPw !== confirmPw) {
            pwHelp.classList.add('error');
            pwHelp.textContent = '비밀번호가 일치하지 않습니다.';
            return;
        }

        alert('비밀번호가 성공적으로 변경되었습니다.\n로그인 페이지로 이동합니다.');
        window.location.href = '/user/login';
    });
});