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

    /**
     * 인증번호 남은 시간 표시
     *
     * 서버(UserController.EMAIL_CODE_VALID_MILLIS)가 5분을 재고 있는데
     * 화면에 아무 표시가 없으면, 사용자는 시간이 지난 뒤 '확인' 을 눌러야
     * 만료된 걸 알게 된다. 남은 시간을 보여줘서 미리 알 수 있게 한다.
     *
     * 회원가입·아이디 찾기·비밀번호 찾기 세 화면이 같이 쓴다.
     *
     * @param element  남은 시간을 찍을 요소 (인증번호 칸 아래 메시지 자리)
     * @param minutes  유효 시간(분). 서버와 같은 값이어야 한다
     * @param onExpire 만료됐을 때 부를 함수 (인증 상태를 지우는 용도)
     * @return         타이머를 멈추는 함수. 인증에 성공하면 이걸 부른다
     */
    function startCodeTimer(element, minutes, onExpire) {

        // 이미 돌고 있던 타이머가 있으면 멈춘다.
        // 인증 요청을 여러 번 누르면 타이머가 겹쳐 시간이 두 배로 줄어든다.
        if (element.dataset.timerId) {
            clearInterval(Number(element.dataset.timerId));
        }

        let left = minutes * 60;

        function draw() {
            if (left <= 0) {
                clearInterval(id);
                delete element.dataset.timerId;
                setFieldMessage(element, "인증번호가 만료되었어요. 다시 요청해 주세요.", "error");
                if (typeof onExpire === "function") {
                    onExpire();
                }
                return;
            }
            const m = Math.floor(left / 60);
            const s = String(left % 60).padStart(2, "0");
            setFieldMessage(element, `남은 시간 ${m}:${s}`, "");
            left--;
        }

        draw(); // 1초 기다리지 않고 바로 보여준다
        const id = setInterval(draw, 1000);
        element.dataset.timerId = String(id);

        return function stop() {
            clearInterval(id);
            delete element.dataset.timerId;
        };
    }

    // 5. 비밀번호 유효성 검사 (영문, 숫자, 특수문자 포함 8자리 이상)
    function isValidPassword(password) {
        return /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d\s]).{8,16}$/.test(password);
    }

    // 5-1. 비밀번호에서 빠진 조건을 찾아 안내 문구를 돌려준다.
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

        // 어느 단계에서 시작할지는 서버가 정해 data-start-step 으로 내려준다.
        //   1  처음 진단      신체정보부터
        //   2  다시 진단      신체정보는 이미 있으므로 성향부터
        const shell = document.querySelector(".auth-shell");
        const minStep = Number((shell && shell.dataset.startStep) || 1);

        let currentStep = minStep;
        const totalSteps = 5;

        // 다시 진단으로 들어온 경우 저장된 신체정보를 미리 채워 둔다.
        // 비워 두면 요약이 'NaN세' 로 나오고, 서버도 '일부만 왔다'고 보고 저장을 막는다.
        const saved = (shell && shell.dataset) || {};

        const formData = {
            birthDate: saved.savedBirth || "",
            gender: saved.savedGender || "",
            height: saved.savedHeight ? Number(saved.savedHeight) : null,
            weight: saved.savedWeight ? Number(saved.savedWeight) : null,
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

        // 생년월일 입력칸
        //
        // 숫자가 아닌 글자와 자릿수만 막는다. 값의 범위는 건드리지 않는다.
        // 2099 를 쳤을 때 몰래 2026 으로 바꿔놓으면 사용자는 자기가 뭘 잘못 눌렀는지
        // 알 수 없다. 친 대로 두고 안내문과 팝업으로 알려주는 편이 낫다.
        const birthLength = {birthYear: 4, birthMonth: 2, birthDay: 2};

        Object.keys(birthLength).forEach(function (id) {
            const input = document.getElementById(id);
            if (!input) return;

            const maxLength = birthLength[id];

            input.addEventListener("input", function () {
                this.value = this.value.replace(/\D/g, "").slice(0, maxLength);
                checkBirth();
            });
        });

        const BIRTH_INVALID = "생년월일이 정확한지 확인해 주세요.";

        /**
         * 생년월일이 쓸 수 있는 날짜인지 본다.
         * 문제가 없으면 null, 있으면 안내 문구를 돌려준다.
         *
         * 입력칸은 자릿수만 막으므로 범위 검사는 전부 여기서 한다.
         *   13 월, 32 일   달력에 없어 Date 가 만들어지지 않는다
         *   2 월 30 일     같은 이유로 걸린다
         *   2099-06-06    달력에는 있지만 아직 오지 않은 날
         *   2026-12-31    올해지만 아직 오지 않은 날
         *   1800-01-01    너무 예전이라 잘못 친 값으로 본다
         */
        function birthError(year, month, day) {
            const date = new Date(`${year}-${month.padStart(2, "0")}-${day.padStart(2, "0")}T00:00:00`);

            if (date.getFullYear() !== Number(year) ||
                date.getMonth() + 1 !== Number(month) ||
                date.getDate() !== Number(day)) {
                return BIRTH_INVALID;
            }

            const today = new Date();
            today.setHours(0, 0, 0, 0);
            if (date > today) return BIRTH_INVALID;
            if (Number(year) < 1900) return BIRTH_INVALID;

            return null;
        }

        /**
         * 세 칸을 다 채웠을 때만 입력칸 아래에 안내 문구를 띄운다.
         * 채우는 도중에 띄우면 한 글자 칠 때마다 빨간 글씨가 깜빡여 거슬린다.
         */
        function checkBirth() {
            const box = document.querySelector(".line-field.box-style");
            const message = document.getElementById("birthMessage");
            if (!message) return null;

            const year = document.getElementById("birthYear").value;
            const month = document.getElementById("birthMonth").value;
            const day = document.getElementById("birthDay").value;

            const filled = year.length === 4 && month.length >= 1 && day.length >= 1;
            const error = filled ? birthError(year, month, day) : null;

            message.textContent = error || "";
            message.className = "field-message" + (error ? " error" : "");
            if (box) box.classList.toggle("has-error", Boolean(error));

            return error;
        }

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
                // 입력칸 아래 안내문과 같은 검사를 쓴다.
                // 안내문을 못 보고 다음을 눌렀을 때를 대비한 마지막 관문이다.
                const birthMessage = checkBirth();
                if (birthMessage) {
                    showMessage(birthMessage);
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

            // 상단 프로그래스 바.
            // 다시 진단이면 minStep 앞의 칸은 아예 감춰서 1부터 세는 것처럼 보이게 한다.
            document.querySelectorAll(".progress-step").forEach(function (bar, idx) {
                const barStep = idx + 1;                       // 이 칸이 가리키는 단계
                bar.style.display = barStep < minStep ? "none" : "";
                bar.classList.toggle("active", barStep <= step);
            });

            // '1 / 4 · 동반자' 처럼 번호를 다시 매겨 찍는다.
            // JSP 에는 라벨만 두고 번호는 여기서 계산한다.
            if (target) {
                const indicator = target.querySelector(".step-indicator[data-label]");
                if (indicator) {
                    indicator.textContent =
                        (step - minStep + 1) + " / " + (totalSteps - minStep + 1)
                        + " · " + indicator.dataset.label;
                }
            }

            // 뒤로가기/이전 버튼 처리
            //
            // 다시 진단(minStep=2)으로 들어왔으면 첫 화면에서도 나갈 수단이 있어야 하므로
            // 뒤로가기는 계속 보여준다. 반면 '이전' 은 갈 단계가 있을 때만 보여준다.
            if (btnBack) btnBack.style.visibility = (step > minStep || minStep > 1) ? "visible" : "hidden";
            if (btnPrev) btnPrev.style.display = step > minStep ? "inline-block" : "none";

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
                        // 서버 문구가 바뀌어도 깨지지 않게 '완료' 라는 말로 판단한다
                        const success = (result.msg || "").includes("완료되었어요");

                        showMessage(result.msg, success ? function () {
                            // 성향조사를 마쳤으므로 맞춤 추천 화면으로 보낸다.
                            location.href = contextPath + "/recommend/recommendList";
                        } : null);
                    } catch (err) {
                        showMessage(err.message);
                    }
                }
            });
        }

        if (btnPrev) {
            btnPrev.addEventListener("click", function () {
                if (currentStep > minStep) {
                    currentStep--;
                    goToStep(currentStep);
                }
            });
        }

        if (btnBack) {
            btnBack.addEventListener("click", function () {
                if (currentStep > minStep) {
                    currentStep--;
                    goToStep(currentStep);
                } else {
                    // 더 돌아갈 단계가 없으면 온보딩 화면을 떠난다.
                    // 다시 진단으로 들어온 경우 추천 화면으로 돌아가는 길이 된다.
                    history.back();
                }
            });
        }

        /**
         * 저장된 신체정보를 1단계 입력칸에 미리 채운다.
         *
         * 다시 진단은 1단계를 건너뛰므로 아무도 입력칸을 채우지 않는다.
         * 그런데 요약 화면의 BMI 는 1단계의 bmiValueText 를 읽어 쓰기 때문에,
         * 채워두고 calculateBMI() 를 한 번 돌려야 '--.- 측정 대기' 가 사라진다.
         */
        function fillSavedBody() {
            if (!formData.birthDate && !formData.height) return; // 처음 진단이면 채울 게 없다

            const parts = String(formData.birthDate).split("-");
            if (parts.length === 3) {
                const y = document.getElementById("birthYear");
                const m = document.getElementById("birthMonth");
                const d = document.getElementById("birthDay");
                if (y) y.value = parts[0];
                if (m) m.value = String(Number(parts[1]));
                if (d) d.value = String(Number(parts[2]));
            }

            const heightEl = document.getElementById("height");
            const weightEl = document.getElementById("weight");
            if (heightEl && formData.height) heightEl.value = formData.height;
            if (weightEl && formData.weight) weightEl.value = formData.weight;

            // 성별 버튼도 눌린 상태로 맞춘다
            document.querySelectorAll(".gender-btn").forEach(function (btn) {
                btn.classList.toggle("active", btn.getAttribute("data-gender") === formData.gender);
            });

            calculateBMI(); // BMI 카드를 채운다. 요약 화면이 이 값을 읽어 간다.
        }

        fillSavedBody();

        // 첫 화면을 그린다.
        // HTML 의 style="display:none" 에만 기대면 다시 진단일 때 1단계가 그대로 보인다.
        goToStep(currentStep);
    }

    // DOM 로드 완료 후 온보딩 초기화
    document.addEventListener("DOMContentLoaded", initOnboarding);

    // 공통 객체 노출
    window.moveOnAuth = {
        post: post,
        postJson: postJson,
        showMessage: showMessage,
        setFieldMessage: setFieldMessage,
        startCodeTimer: startCodeTimer,
        isValidPassword: isValidPassword,
        getPasswordMessage: getPasswordMessage
    };

    // DOM 로드 완료 시 실행 리스트에 추가
    document.addEventListener("DOMContentLoaded", initOnboarding);

    // auth.js 하단에 추가

// 운동 기록 모달 열기 함수 (전역)
    function openWorkoutModal() {
        const modal = document.getElementById("workoutModal");
        if (modal) {
            modal.classList.add("show");
        }
    }

// 운동 기록 모달 닫기 함수 (전역)
    function closeWorkoutModal() {
        const modal = document.getElementById("workoutModal");
        if (modal) {
            modal.classList.remove("show");
        }
    }
}());
