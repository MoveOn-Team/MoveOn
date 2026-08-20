document.addEventListener("DOMContentLoaded", function () {

    // 1. 종목 서브 태그 클릭 이벤트 (수영, 배드민턴, 헬스 등)
    const subTagBtns = document.querySelectorAll(".sub-tag-btn");

    subTagBtns.forEach(function (btn) {
        btn.addEventListener("click", function () {
            // 기존 활성화 클래스 제거 후 클릭한 버튼에 추가
            subTagBtns.forEach(b => b.classList.remove("is-active"));
            this.classList.add("is-active");

            // TODO: 나중에 해당 종목 데이터만 필터링하는 백엔드/AJAX 연동 위치
            console.log("선택된 종목:", this.innerText);
        });
    });

    // 2. 상세 페이지 버튼 이벤트 연결
    const btnOutline = document.querySelector(".btn-outline");
    const btnPrimary = document.querySelector(".btn-primary");

    if (btnOutline) {
        btnOutline.addEventListener("click", function () {
            alert("카카오맵/네이버지도 길찾기로 연결될 예정입니다.");
        });
    }

    if (btnPrimary) {
        btnPrimary.addEventListener("click", function () {
            alert("해당 시설의 공식 안내 페이지로 이동합니다.");
        });
    }
});