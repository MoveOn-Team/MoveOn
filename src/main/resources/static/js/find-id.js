document.addEventListener('DOMContentLoaded', () => {

    const btnSendAuth = document.getElementById('btnSendAuth');
    const authGroup = document.getElementById('authGroup');
    const btnVerifyAuth = document.getElementById('btnVerifyAuth');
    const authCodeInput = document.getElementById('authCode');
    const authHelp = document.getElementById('authHelp');

    const btnNext = document.getElementById('btnNext');
    const stepInput = document.getElementById('stepInput');
    const stepResult = document.getElementById('stepResult');

    const btnResultBack = document.getElementById('btnResultBack');
    const btnLogin = document.getElementById('btnLogin');

    let isVerified = false; // 인증 여부 상태값

    // 1. [인증요청] 버튼 클릭 -> 인증 번호 입력란 표시
    btnSendAuth.addEventListener('click', () => {
        const emailVal = document.getElementById('email').value.trim();
        if (!emailVal) {
            alert('이메일 주소를 입력해 주세요.');
            return;
        }

        authGroup.classList.remove('hidden');
        authCodeInput.focus();
    });

    // 2. [확인] 버튼 클릭 -> 인증 번호 일치 검사 (테스트 모크)
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

        // 목업 테스트 기준: '123456' 입력 시 실패, '197314' 입력 또는 그 외 입력 시 성공 처리
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

    // 3. [다음] 버튼 클릭 -> 결과 화면 전환
    btnNext.addEventListener('click', () => {
        const nameVal = document.getElementById('userName').value.trim();
        const emailVal = document.getElementById('email').value.trim();

        if (!nameVal || !emailVal) {
            alert('이름과 이메일 주소를 입력해 주세요.');
            return;
        }

        if (!isVerified) {
            alert('이메일 인증을 완료해 주세요.');
            return;
        }

        // 입력 화면 숨기고 결과 화면 표시
        stepInput.classList.add('hidden');
        stepResult.classList.remove('hidden');
    });

    // 4. 결과 화면에서 뒤로가기 버튼 클릭 -> 다시 입력 화면으로
    btnResultBack.addEventListener('click', () => {
        stepResult.classList.add('hidden');
        stepInput.classList.remove('hidden');
    });

    // 5. [로그인] 버튼 클릭 -> 로그인 페이지 이동
    btnLogin.addEventListener('click', () => {
        window.location.href = '/user/login';
    });
});