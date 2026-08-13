// js/login.js

document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('password');
    const toggleButton = document.getElementById('togglePassword');
    const toggleIcon = toggleButton.querySelector('i');

    toggleButton.addEventListener('click', function() {
        // 입력 타입을 password <-> text로 전환
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);

        // 아이콘 클래스 변경 (font-awesome 기준)
        if (type === 'text') {
            // 보이기 상태 (화면 2)
            toggleIcon.classList.remove('fa-eye-slash');
            toggleIcon.classList.add('fa-eye');
        } else {
            // 숨김 상태 (화면 1)
            toggleIcon.classList.remove('fa-eye');
            toggleIcon.classList.add('fa-eye-slash');
        }
    });
});