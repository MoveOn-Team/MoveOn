document.addEventListener('DOMContentLoaded', () => {

    // 1. 비밀번호 보이기 / 가리기 토글
    const eyeButtons = document.querySelectorAll('.input-eye');
    eyeButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-target');
            const targetInput = document.getElementById(targetId);
            const icon = btn.querySelector('i');

            if (targetInput.type === 'password') {
                targetInput.type = 'text';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            } else {
                targetInput.type = 'password';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            }
        });
    });

    // 2. 약관 전체 동의 제어
    const checkAll = document.getElementById('checkAll');
    const termChecks = document.querySelectorAll('.term-check');

    checkAll.addEventListener('change', () => {
        termChecks.forEach(cb => cb.checked = checkAll.checked);
    });

    termChecks.forEach(cb => {
        cb.addEventListener('change', () => {
            const allChecked = Array.from(termChecks).every(c => c.checked);
            checkAll.checked = allChecked;
        });
    });

    // 3. 비밀번호 유효성 검사 (목업에 명시된 규칙 반영)
    const userPw = document.getElementById('userPw');
    const pwHelp = document.getElementById('pwHelp');

    userPw.addEventListener('input', () => {
        const val = userPw.value;
        const hasNum = /[0-9]/.test(val);
        const hasEng = /[a-zA-Z]/.test(val);
        const hasSpe = /[!@#$%^&*(),.?":{}|<>]/.test(val);

        userPw.classList.remove('is-error', 'is-ok');
        pwHelp.classList.remove('error', 'ok');

        if (val.length === 0) {
            pwHelp.textContent = '';
            return;
        }

        if (!hasNum) {
            userPw.classList.add('is-error');
            pwHelp.classList.add('error');
            pwHelp.textContent = '숫자가 포함되지 않았습니다';
        } else if (!hasEng) {
            userPw.classList.add('is-error');
            pwHelp.classList.add('error');
            pwHelp.textContent = '영문이 포함되지 않았습니다';
        } else if (!hasSpe) {
            userPw.classList.add('is-error');
            pwHelp.classList.add('error');
            pwHelp.textContent = '특수문자가 포함되지 않았습니다';
        } else if (val.length < 8 || val.length > 16) {
            userPw.classList.add('is-error');
            pwHelp.classList.add('error');
            pwHelp.textContent = '8~16자리로 입력해 주세요';
        } else {
            userPw.classList.add('is-ok');
            pwHelp.classList.add('ok');
            pwHelp.textContent = '사용 가능한 비밀번호입니다';
        }

        validatePwConfirm();
    });

    // 4. 비밀번호 확인 일치 검사
    const userPwConfirm = document.getElementById('userPwConfirm');
    const pwConfirmHelp = document.getElementById('pwConfirmHelp');

    function validatePwConfirm() {
        const val1 = userPw.value;
        const val2 = userPwConfirm.value;

        userPwConfirm.classList.remove('is-error', 'is-ok');
        pwConfirmHelp.classList.remove('error', 'ok');

        if (val2.length === 0) {
            pwConfirmHelp.textContent = '';
            return;
        }

        if (val1 === val2) {
            userPwConfirm.classList.add('is-ok');
            pwConfirmHelp.classList.add('ok');
            pwConfirmHelp.textContent = '비밀번호가 일치합니다';
        } else {
            userPwConfirm.classList.add('is-error');
            pwConfirmHelp.classList.add('error');
            pwConfirmHelp.textContent = '비밀번호가 일치하지 않습니다';
        }
    }

    userPwConfirm.addEventListener('input', validatePwConfirm);

    // 5. 폼 제출 및 모달 노출 처리
    const joinForm = document.getElementById('joinForm');
    const successModal = document.getElementById('successModal');
    const btnModalConfirm = document.getElementById('btnModalConfirm');

    joinForm.addEventListener('submit', (e) => {
        e.preventDefault();
        // 제출 시 완료 모달 노출 (백엔드 연동 전 UI 가이드 확인용)
        successModal.classList.remove('hidden');
    });

    btnModalConfirm.addEventListener('click', () => {
        window.location.href = '/login';
    });
});
btnModalConfirm.addEventListener('click', () => {
    // 모달 닫기
    successModal.classList.add('hidden');
    // 필요시 리셋 또는 이전 페이지 이동: history.back();
});