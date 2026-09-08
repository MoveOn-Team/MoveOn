/* 지역 스포츠 행사 (목록 / 상세) */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {

        // 현위치는 JSP 가 body 에 실어 보내 준다. 화면을 옮길 때 이어줘야 거리가 안 바뀐다
        var body = document.body;
        var ctx = body.dataset.contextPath || "";
        var lat = body.dataset.lat;
        var lng = body.dataset.lng;

        function withPos(url) {
            if (!lat || !lng) {
                return url;
            }
            return url + (url.indexOf("?") < 0 ? "?" : "&") + "lat=" + lat + "&lng=" + lng;
        }

        // ---------- 정렬 토글 ----------
        // 여기서 다시 줄 세우지 않고 서버에 물어본다. 계산이 전부 SQL 에 있어서
        document.querySelectorAll(".tab-btn").forEach(function (btn) {
            btn.addEventListener("click", function () {
                if (this.classList.contains("active")) {
                    return; // 이미 보고 있는 정렬이면 그냥 둔다
                }
                location.href = withPos(ctx + "/event/eventList?sort=" + this.dataset.filter);
            });
        });

        // ---------- 카드를 누르면 상세로 ----------
        document.querySelectorAll(".event-card").forEach(function (card) {
            card.addEventListener("click", function () {
                var eventId = this.dataset.eventId;
                if (eventId) {
                    location.href = withPos(ctx + "/event/eventDetail/" + eventId);
                }
            });
        });

        // ---------- 갱신 배지 ----------
        var btnRefresh = document.getElementById("btnRefresh");
        if (btnRefresh) {
            btnRefresh.addEventListener("click", function () {
                location.reload();
            });
        }

        // ---------- 상세 화면 아래 버튼 ----------
        // 주소가 비면 새 창에 빈 페이지가 열리므로 그때는 알려만 준다
        var btnLocation = document.getElementById("btnLocation");
        if (btnLocation) {
            btnLocation.addEventListener("click", function () {
                var mapUrl = this.dataset.mapUrl;
                if (mapUrl) {
                    window.open(mapUrl, "_blank", "noopener");
                } else {
                    alert("이 행사는 위치 정보가 아직 없어요.");
                }
            });
        }

        var btnExternal = document.getElementById("btnExternal");
        if (btnExternal) {
            btnExternal.addEventListener("click", function () {
                var targetUrl = this.dataset.targetUrl;

                // window.open 은 javascript: 주소를 그 자리에서 실행한다. 저장할 때도 막지만 여기서도 본다
                if (/^https?:\/\//i.test(targetUrl || "")) {
                    window.open(targetUrl, "_blank", "noopener");
                } else {
                    alert("이 행사는 안내 사이트가 등록되어 있지 않아요.");
                }
            });
        }
    });
})();
