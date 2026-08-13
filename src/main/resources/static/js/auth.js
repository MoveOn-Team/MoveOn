(function () {
    "use strict";

    // 폼 또는 객체를 스프링 컨트롤러가 받을 수 있는 형식으로 전송한다.
    async function post(url, source) {
        const params = new URLSearchParams();

        if (source instanceof HTMLFormElement) {
            new FormData(source).forEach(function (value, key) {
                params.append(key, value);
            });
        } else {
            Object.keys(source || {}).forEach(function (key) {
                if (source[key] !== undefined && source[key] !== null) {
                    params.append(key, source[key]);
                }
            });
        }

        const response = await fetch(url, {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"},
            body: params.toString(),
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("요청 처리 중 오류가 발생했습니다. (HTTP " + response.status + ")");
        }

        return response.json();
    }

    // 서버의 실행 결과를 피그마 형태의 간단한 알림창으로 표시한다.
    function showMessage(message, onClose) {
        let modal = document.getElementById("moveOnMessageModal");

        if (!modal) {
            modal = document.createElement("dialog");
            modal.id = "moveOnMessageModal";
            modal.className = "message-modal";
            modal.innerHTML = '<div class="message-modal-body"><p></p><button type="button">확인</button></div>';
            document.body.appendChild(modal);
        }

        modal.querySelector("p").textContent = message || "처리 결과를 확인해주세요.";
        const closeButton = modal.querySelector("button");
        closeButton.onclick = function () {
            modal.close();
            if (typeof onClose === "function") {
                onClose();
            }
        };

        if (typeof modal.showModal === "function") {
            if (!modal.open) {
                modal.showModal();
            }
        } else {
            window.alert(message);
            if (typeof onClose === "function") {
                onClose();
            }
        }
    }

    // 입력칸 아래 안내 문구의 내용과 성공·실패 색상을 바꾼다.
    function setFieldMessage(element, message, type) {
        element.textContent = message;
        element.className = "field-message" + (type ? " " + type : "");

        const field = element.closest(".line-field");
        if (field) {
            field.classList.toggle("has-error", type === "error");
            field.classList.toggle("has-ok", type === "ok");
        }
    }

    // 비밀번호가 영문, 숫자, 특수문자를 포함한 8~16자리인지 확인한다.
    function isValidPassword(password) {
        return /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d\s]).{8,16}$/.test(password);
    }

    // 비밀번호 눈 아이콘을 피그마의 감긴 눈 상태로 준비한다.
    const eyeHidden = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3l18 18"></path><path d="M10.6 10.7a2 2 0 0 0 2.7 2.7"></path><path d="M9.9 4.3A10.7 10.7 0 0 1 12 4c5 0 8.5 4.2 9 5.4a1.5 1.5 0 0 1 0 1.2 12.8 12.8 0 0 1-2.4 3.3"></path><path d="M6.2 6.2A13.3 13.3 0 0 0 3 9.4a1.5 1.5 0 0 0 0 1.2C3.5 11.8 7 16 12 16c1 0 2-.2 2.8-.5"></path></svg>';
    const eyeVisible = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 9.4C3.5 8.2 7 4 12 4s8.5 4.2 9 5.4a1.5 1.5 0 0 1 0 1.2C20.5 11.8 17 16 12 16s-8.5-4.2-9-5.4a1.5 1.5 0 0 1 0-1.2Z"></path><circle cx="12" cy="10" r="2.5"></circle></svg>';

    document.querySelectorAll("[data-password-toggle]").forEach(function (button) {
        button.innerHTML = eyeHidden;
    });

    // 비밀번호 눈 아이콘을 누르면 입력값의 숨김·표시 상태를 전환한다.
    document.addEventListener("click", function (event) {
        const button = event.target.closest("[data-password-toggle]");
        if (!button) {
            return;
        }

        const input = document.querySelector(button.getAttribute("data-password-toggle"));
        const visible = input.type === "text";
        input.type = visible ? "password" : "text";
        button.classList.toggle("is-visible", !visible);
        button.innerHTML = visible ? eyeHidden : eyeVisible;
        button.setAttribute("aria-label", visible ? "비밀번호 보기" : "비밀번호 숨기기");
    });

    window.moveOnAuth = {
        post: post,
        showMessage: showMessage,
        setFieldMessage: setFieldMessage,
        isValidPassword: isValidPassword
    };
}());
