document.addEventListener('DOMContentLoaded', () => {

    // 저장 데이터
    const surveyData = {
        birthYear: '',
        birthMonth: '',
        birthDay: '',
        gender: '',
        height: '',
        weight: '',
        bmi: 0,
        bmiStatus: '',
        companion: '',
        competition: '',
        place: '',
        intensity: ''
    };

    let currentStep = 1;

    // --- 1. 성별 선택 ---
    document.querySelectorAll('.gender-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.gender-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            surveyData.gender = btn.getAttribute('data-gender');
        });
    });

    // --- 2. BMI 실시간 계산 ---
    const heightInput = document.getElementById('heightInput');
    const weightInput = document.getElementById('weightInput');
    const bmiEmpty = document.getElementById('bmiEmpty');
    const bmiResult = document.getElementById('bmiResult');
    const bmiValEl = document.getElementById('bmiVal');
    const bmiStatusBadge = document.getElementById('bmiStatusBadge');
    const bmiPointer = document.getElementById('bmiPointer');

    function calculateBMI() {
        const h = parseFloat(heightInput.value);
        const w = parseFloat(weightInput.value);

        if (h > 0 && w > 0) {
            const hMeter = h / 100;
            const bmi = (w / (hMeter * hMeter)).toFixed(1);
            surveyData.bmi = parseFloat(bmi);

            bmiValEl.textContent = bmi;

            // 판정 기준 (대한비만학회 기준)
            let status = '정상 체중';
            let percent = 50; // 위치 백분율 (0% ~ 100%)

            if (bmi < 18.5) {
                status = '저체중';
                percent = Math.max(5, (bmi / 18.5) * 25);
            } else if (bmi < 23.0) {
                status = '정상 체중';
                percent = 25 + ((bmi - 18.5) / 4.5) * 25;
            } else if (bmi < 25.0) {
                status = '과체중';
                percent = 50 + ((bmi - 23.0) / 2.0) * 25;
            } else {
                status = '비만';
                percent = Math.min(95, 75 + ((bmi - 25.0) / 10.0) * 25);
            }

            surveyData.bmiStatus = status;
            bmiStatusBadge.textContent = status;
            bmiPointer.style.left = `${percent}%`;

            bmiEmpty.classList.add('hidden');
            bmiResult.classList.remove('hidden');
        } else {
            bmiEmpty.classList.remove('hidden');
            bmiResult.classList.add('hidden');
        }
    }

    heightInput.addEventListener('input', calculateBMI);
    weightInput.addEventListener('input', calculateBMI);

    // --- 3. 객관식 선택 카드 클릭 ---
    document.querySelectorAll('.option-list').forEach(list => {
        const key = list.getAttribute('data-step');
        list.querySelectorAll('.option-card').forEach(card => {
            card.addEventListener('click', () => {
                list.querySelectorAll('.option-card').forEach(c => c.classList.remove('selected'));
                card.classList.add('selected');
                surveyData[key] = card.getAttribute('data-value');
            });
        });
    });

    // --- 4. 스텝 이동 관리 ---
    function goToStep(step) {
        // 프로그래스 바 업데이트
        for (let i = 1; i <= 5; i++) {
            const bar = document.getElementById(`bar${i}`);
            if (i <= step && step !== 'result') {
                bar.classList.add('active');
            } else if (step === 'result') {
                bar.classList.add('active');
            } else {
                bar.classList.remove('active');
            }
        }

        // 스텝 컨테이너 전환
        document.querySelectorAll('.step-container').forEach(el => el.classList.add('hidden'));

        if (step === 'result') {
            renderSummary();
            document.getElementById('stepResult').classList.remove('hidden');
            currentStep = 'result';
        } else {
            document.getElementById(`step${step}`).classList.remove('hidden');
            currentStep = step;
        }
    }

    // [다음] 버튼 클릭 이벤트
    document.querySelectorAll('.btn-next').forEach(btn => {
        btn.addEventListener('click', () => {
            const nextStep = btn.getAttribute('data-next');

            // Step 1 유효성 검사
            if (currentStep === 1) {
                surveyData.birthYear = document.getElementById('birthYear').value.trim();
                surveyData.birthMonth = document.getElementById('birthMonth').value.trim();
                surveyData.birthDay = document.getElementById('birthDay').value.trim();
                surveyData.height = heightInput.value.trim();
                surveyData.weight = weightInput.value.trim();

                if (!surveyData.birthYear || !surveyData.birthMonth || !surveyData.birthDay) {
                    alert('생년월일을 올바르게 입력해 주세요.');
                    return;
                }
                if (!surveyData.gender) {
                    alert('성별을 선택해 주세요.');
                    return;
                }
                if (!surveyData.height || !surveyData.weight) {
                    alert('키와 몸무게를 입력해 주세요.');
                    return;
                }
            }

            // Step 2~5 유효성 검사
            if (currentStep === 2 && !surveyData.companion) {
                alert('항목을 선택해 주세요.'); return;
            }
            if (currentStep === 3 && !surveyData.competition) {
                alert('항목을 선택해 주세요.'); return;
            }
            if (currentStep === 4 && !surveyData.place) {
                alert('항목을 선택해 주세요.'); return;
            }
            if (currentStep === 5 && !surveyData.intensity) {
                alert('항목을 선택해 주세요.'); return;
            }

            goToStep(nextStep === 'result' ? 'result' : parseInt(nextStep));
        });
    });

    // [이전] 버튼 클릭 이벤트
    document.querySelectorAll('.btn-prev').forEach(btn => {
        btn.addEventListener('click', () => {
            const prevStep = parseInt(btn.getAttribute('data-prev'));
            goToStep(prevStep);
        });
    });

    // 상단 헤더 뒤로가기
    document.getElementById('btnHeaderBack').addEventListener('click', () => {
        if (currentStep === 1) {
            history.back();
        } else if (currentStep === 'result') {
            goToStep(5);
        } else {
            goToStep(currentStep - 1);
        }
    });

    // --- 5. 결과 화면 데이터 렌더링 ---
    function renderSummary() {
        // 만 나이 계산 (기준 2026년)
        const currentYear = 2026;
        const age = currentYear - parseInt(surveyData.birthYear);
        const genderText = surveyData.gender === 'M' ? '남성' : '여성';

        document.getElementById('summaryAgeGender').textContent = `만 ${age}세 · ${genderText}`;
        document.getElementById('summaryBmiInfo').textContent = `${surveyData.height}cm · ${surveyData.weight}kg · BMI ${surveyData.bmi} ${surveyData.bmiStatus}`;
        document.getElementById('summaryCompanion').textContent = surveyData.companion;
        document.getElementById('summaryCompetition').textContent = surveyData.competition;
        document.getElementById('summaryPlace').textContent = surveyData.place;
        document.getElementById('summaryIntensity').textContent = surveyData.intensity;
    }

    // 다시 선택
    document.getElementById('btnRestart').addEventListener('click', () => {
        goToStep(1);
    });

    // 최종 결과 확인 (서버 전송)
    // onboarding.js 하단 부분 수정
    document.getElementById('btnFinalSubmit').addEventListener('click', async () => {
        try {
            const response = await fetch('/user/onboarding-proc', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(surveyData)
            });

            const result = await response.json();

            if (result.success) {
                // 성향 저장 성공 시 추천 결과 페이지로 이동
                window.location.href = '/user/recommend';
            } else {
                alert(result.message || '저장 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            // 서버 연동 전 임시 확인용 (에러 시에도 이동하도록 처리)
            window.location.href = '/user/recommend';
        }
    });

    // 초기 실행
    goToStep(1);
});