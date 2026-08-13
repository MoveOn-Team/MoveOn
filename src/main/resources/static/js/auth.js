(function () {
    "use strict";

    // 1. 폼 또는 객체를 x-www-form-urlencoded 형식으로 전송
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

    // 2. 객체를 application/json 형식으로 전송 (온보딩 등 JSON API용)
    async function postJson(url, data) {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            },
            body: JSON.stringify(data),
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("요청 처리 중 오류가 발생했습니다. (HTTP " + response.status + ")");
        }

        return response.json();
    }

    // 3. 피그마 형태의 메세지 모달 알림창
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

    // 4. 입력칸 아래 안내 문구 상태 제어
    function setFieldMessage(element, message, type) {
        element.textContent = message;
        element.className = "field-message" + (type ? " " + type : "");

        const field = element.closest(".line-field");
        if (field) {
            field.classList.toggle("has-error", type === "error");
            field.classList.toggle("has-ok", type === "ok");
        }
    }

    // 5. 비밀번호 유효성 검사 (영문, 숫자, 특수문자 포함 8자리 이상)
    function isValidPassword(password) {
        return /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d\s]).{8,16}$/.test(password);
    }

    // 5-1. 비밀번호에서 빠진 조건을 찾아 안내 문구를 돌려준다.
    //      길이는 맨 뒤에서 본다. 타이핑 도중엔 항상 짧아서 다른 안내가 묻히기 때문이다.
    function getPasswordMessage(password) {
        if (!password) return "";
        if (password.length < 8) return "8자 이상 입력해 주세요.";
        if (password.length > 16) return "16자 이하로 입력해 주세요.";
        if (!/[A-Za-z]/.test(password)) return "영문이 포함되지 않았습니다.";
        if (!/\d/.test(password)) return "숫자가 포함되지 않았습니다.";
        if (!/[^A-Za-z\d\s]/.test(password)) return "특수문자가 포함되지 않았습니다.";
        return "사용 가능한 비밀번호입니다.";
    }

    // 6. 비밀번호 눈 아이콘 및 토글 이벤트 처리
    const eyeHidden = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3l18 18"></path><path d="M10.6 10.7a2 2 0 0 0 2.7 2.7"></path><path d="M9.9 4.3A10.7 10.7 0 0 1 12 4c5 0 8.5 4.2 9 5.4a1.5 1.5 0 0 1 0 1.2 12.8 12.8 0 0 1-2.4 3.3"></path><path d="M6.2 6.2A13.3 13.3 0 0 0 3 9.4a1.5 1.5 0 0 0 0 1.2C3.5 11.8 7 16 12 16c1 0 2-.2 2.8-.5"></path></svg>';
    const eyeVisible = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 9.4C3.5 8.2 7 4 12 4s8.5 4.2 9 5.4a1.5 1.5 0 0 1 0 1.2C20.5 11.8 17 16 12 16s-8.5-4.2-9-5.4a1.5 1.5 0 0 1 0-1.2Z"></path><circle cx="12" cy="10" r="2.5"></circle></svg>';

    document.querySelectorAll("[data-password-toggle]").forEach(function (button) {
        button.innerHTML = eyeHidden;
    });

    document.addEventListener("click", function (event) {
        const button = event.target.closest("[data-password-toggle]");
        if (!button) return;

        const input = document.querySelector(button.getAttribute("data-password-toggle"));
        if (!input) return;

        const visible = input.type === "text";
        input.type = visible ? "password" : "text";
        button.classList.toggle("is-visible", !visible);
        button.innerHTML = visible ? eyeHidden : eyeVisible;
        button.setAttribute("aria-label", visible ? "비밀번호 보기" : "비밀번호 숨기기");
    });

    // =========================================================
    // 7. 온보딩(Onboarding) 전용 로직
    // =========================================================
    function initOnboarding() {
        const onboardingContainer = document.querySelector(".onboarding-container");
        if (!onboardingContainer) return; // 온보딩 페이지가 아닐 경우 실행 안 함

        const contextPath = document.body.dataset.contextPath || "";

        let currentStep = 1;
        const totalSteps = 5;

        const formData = {
            birthDate: "",
            gender: "",
            height: null,
            weight: null,
            companion: "",
            competition: "",
            place: "",
            intensity: ""
        };

        const btnBack = document.getElementById("btnBack");
        const btnPrev = document.getElementById("btnPrev");
        const btnNext = document.getElementById("btnNext");

        // BMI 라이브 실시간 계산
        function calculateBMI() {
            const h = parseFloat(document.getElementById("height").value);
            const w = parseFloat(document.getElementById("weight").value);

            const pointer = document.getElementById("bmiPointer");
            const statusText = document.getElementById("bmiStatusText");
            const valueText = document.getElementById("bmiValueText");

            if (h > 0 && w > 0) {
                const heightM = h / 100;
                const bmi = (w / (heightM * heightM)).toFixed(1);
                valueText.textContent = bmi;

                let status = "정상";
                let percent = 0;

                if (bmi < 18.5) {
                    status = "저체중";
                    percent = Math.min((bmi / 18.5) * 25, 25);
                } else if (bmi < 23) {
                    status = "정상 체중";
                    percent = 25 + ((bmi - 18.5) / 4.5) * 25;
                } else if (bmi < 25) {
                    status = "과체중";
                    percent = 50 + ((bmi - 23) / 2) * 25;
                } else {
                    status = "비만";
                    percent = Math.min(75 + ((bmi - 25) / 10) * 25, 100);
                }

                if (statusText) statusText.textContent = status;
                if (pointer) pointer.style.left = percent + "%";
                formData.height = h;
                formData.weight = w;
            } else {
                if (valueText) valueText.textContent = "--.-";
                if (statusText) statusText.textContent = "측정 대기";
                if (pointer) pointer.style.left = "0%";
            }
        }

        // 키/몸무게 입력 바인딩
        ["height", "weight"].forEach(function (id) {
            const input = document.getElementById(id);
            if (input) {
                input.addEventListener("input", calculateBMI);
            }
        });

        // 성별 버튼 바인딩
        document.querySelectorAll(".gender-btn").forEach(function (btn) {
            btn.addEventListener("click", function () {
                document.querySelectorAll(".gender-btn").forEach(function (b) { b.classList.remove("active"); });
                this.classList.add("active");
                formData.gender = this.getAttribute("data-gender");
            });
        });

        // 카드 옵션 클릭 시 활성화 및 값 저장
        document.querySelectorAll(".option-group").forEach(function (group) {
            const name = group.getAttribute("data-name");
            group.querySelectorAll(".option-card").forEach(function (card) {
                card.addEventListener("click", function () {
                    group.querySelectorAll(".option-card").forEach(function (c) { c.classList.remove("active"); });
                    this.classList.add("active");

                    const radio = this.querySelector('input[type="radio"]');
                    if (radio) {
                        radio.checked = true;
                        formData[name] = radio.value;
                    }
                });
            });
        });

        // 스텝 입력 검증
        function validateStep(step) {
            if (step === 1) {
                const year = document.getElementById("birthYear") ? document.getElementById("birthYear").value : "";
                const month = document.getElementById("birthMonth") ? document.getElementById("birthMonth").value.padStart(2, "0") : "";
                const day = document.getElementById("birthDay") ? document.getElementById("birthDay").value.padStart(2, "0") : "";

                if (!year || !month || !day) {
                    showMessage("생년월일을 입력해주세요.");
                    return false;
                }
                if (!formData.gender) {
                    showMessage("성별을 선택해주세요.");
                    return false;
                }
                if (!formData.height || !formData.weight) {
                    showMessage("키와 몸무게를 올바르게 입력해주세요.");
                    return false;
                }
                const birthDate = new Date(`${year}-${month}-${day}T00:00:00`);
                if (birthDate.getFullYear() !== Number(year) ||
                    birthDate.getMonth() + 1 !== Number(month) ||
                    birthDate.getDate() !== Number(day)) {
                    showMessage("올바른 생년월일을 입력해주세요.");
                    return false;
                }

                formData.birthDate = `${year}-${month}-${day}`;
                return true;
            } else if (step === 2) {
                if (!formData.companion) { showMessage("동반자 옵션을 선택해주세요."); return false; }
            } else if (step === 3) {
                if (!formData.competition) { showMessage("승부욕 옵션을 선택해주세요."); return false; }
            } else if (step === 4) {
                if (!formData.place) { showMessage("장소 옵션을 선택해주세요."); return false; }
            } else if (step === 5) {
                if (!formData.intensity) { showMessage("강도 옵션을 선택해주세요."); return false; }
            }
            return true;
        }

        // 스텝 6 요약 렌더링
        function renderSummary() {
            // 생일이 지났는지까지 따져 서버(TIMESTAMPDIFF)와 같은 만 나이를 구한다.
            // 연도만 빼면 생일 전인 사람이 한 살 많게 나와 추천 계산과 어긋난다.
            // birthDate 는 "1993-6-6" 처럼 0 채움이 없을 수 있어 숫자로 직접 만든다.
            const birthParts = formData.birthDate.split("-");
            const birth = new Date(Number(birthParts[0]), Number(birthParts[1]) - 1, Number(birthParts[2]));
            const today = new Date();
            let age = today.getFullYear() - birth.getFullYear();
            if (today.getMonth() < birth.getMonth() ||
                (today.getMonth() === birth.getMonth() && today.getDate() < birth.getDate())) {
                age--;
            }

            const genderText = formData.gender === "M" ? "남성" : "여성";
            const bmiValEl = document.getElementById("bmiValueText");
            const bmiStatusEl = document.getElementById("bmiStatusText");
            const bmiVal = bmiValEl ? bmiValEl.textContent : "";
            const bmiStatus = bmiStatusEl ? bmiStatusEl.textContent : "";

            const basicEl = document.getElementById("summaryBasic");
            const bmiEl = document.getElementById("summaryBmi");
            if (basicEl) basicEl.textContent = `만 ${age}세 · ${genderText}`;
            if (bmiEl) bmiEl.textContent = `${formData.height}cm·${formData.weight}kg·BMI ${bmiVal} ${bmiStatus}`;

            const labels = {
                companion: { ALONE: "혼자가 편해요", PAIR: "둘이서 같이 하고 싶어요", GROUP: "여럿이 어울리는 게 좋아요" },
                competition: { OWN_PACE: "내 페이스대로 하고 싶어요", ANY: "상관없어요", WIN: "겨루는 게 제일 재밌어요" },
                place: { INDOOR: "날씨 상관없는 실내가 좋아요", ANY: "상관없어요", OUTDOOR: "바깥에서 하고 싶어요" },
                intensity: { LIGHT: "가볍게 몸만 풀 정도", MODERATE: "적당히 땀이 날 정도", HARD: "숨이 찰 만큼 확실하게" }
            };

            const summaryCompanion = document.getElementById("summaryCompanion");
            const summaryCompetition = document.getElementById("summaryCompetition");
            const summaryPlace = document.getElementById("summaryPlace");
            const summaryIntensity = document.getElementById("summaryIntensity");

            if (summaryCompanion) summaryCompanion.textContent = labels.companion[formData.companion] || "--";
            if (summaryCompetition) summaryCompetition.textContent = labels.competition[formData.competition] || "--";
            if (summaryPlace) summaryPlace.textContent = labels.place[formData.place] || "--";
            if (summaryIntensity) summaryIntensity.textContent = labels.intensity[formData.intensity] || "--";
        }

        // 스텝 화면 전환
        function goToStep(step) {
            document.querySelectorAll(".step-content").forEach(function (el) { el.style.display = "none"; });
            const target = document.getElementById("step" + step);
            if (target) target.style.display = "block";

            // 상단 프로그래스 바 업데이트
            document.querySelectorAll(".progress-step").forEach(function (bar, idx) {
                bar.classList.toggle("active", idx < step);
            });

            // 뒤로가기/이전 버튼 처리
            if (btnBack) btnBack.style.visibility = step > 1 ? "visible" : "hidden";
            if (btnPrev) btnPrev.style.display = step > 1 ? "inline-block" : "none";

            if (step === 6) {
                renderSummary();
                if (btnNext) btnNext.textContent = "결과 확인";
                if (btnPrev) btnPrev.textContent = "다시 선택";
            } else {
                if (btnNext) btnNext.textContent = "다음";
                if (btnPrev) btnPrev.textContent = "이전";
            }
        }

        // 다음 / 이전 버튼 이벤트 바인딩
        if (btnNext) {
            btnNext.addEventListener("click", async function () {
                if (currentStep <= totalSteps) {
                    if (!validateStep(currentStep)) return;
                    currentStep++;
                    goToStep(currentStep);
                } else {
                    // 최종 제출 (POST /user/onboarding)
                    try {
                        const result = await postJson(contextPath + "/user/onboarding", formData);
                        const success = result.msg === "온보딩 정보가 저장되었습니다.";

                        showMessage(result.msg, success ? function () {
                            // 성향조사를 마쳤으므로 맞춤 추천 화면으로 보낸다.
                            location.href = contextPath + "/recommend";
                        } : null);
                    } catch (err) {
                        showMessage(err.message);
                    }
                }
            });
        }

        if (btnPrev) {
            btnPrev.addEventListener("click", function () {
                if (currentStep > 1) {
                    currentStep--;
                    goToStep(currentStep);
                }
            });
        }

        if (btnBack) {
            btnBack.addEventListener("click", function () {
                if (currentStep > 1) {
                    currentStep--;
                    goToStep(currentStep);
                }
            });
        }
    }

    // DOM 로드 완료 후 온보딩 초기화
    document.addEventListener("DOMContentLoaded", initOnboarding);

    // 공통 객체 노출
    window.moveOnAuth = {
        post: post,
        postJson: postJson,
        showMessage: showMessage,
        setFieldMessage: setFieldMessage,
        isValidPassword: isValidPassword,
        getPasswordMessage: getPasswordMessage
    };
}());
