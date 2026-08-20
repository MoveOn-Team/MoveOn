/* ---------------------------------------------------------------------
   지역 스포츠 행사 (SC-030 목록 / SC-031 상세)

   auth.js 에 섞여 있던 것을 옮겼다.
   회원가입·성향조사와 아무 관련이 없어서 같은 파일에 둘 이유가 없다.
   이 파일은 행사 화면 두 개에서만 부른다.
   --------------------------------------------------------------------- */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {

        // 현위치는 목록 화면이 body 에 실어 보내 준다.
        // 정렬을 바꾸거나 상세로 넘어갈 때 이 값을 이어줘야 거리 표시가 달라지지 않는다.
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
        // 화면에서 목록을 다시 줄 세우지 않고 서버에 다시 물어본다.
        // 거리와 마감일 계산이 모두 SQL 에 있어서, 여기서 또 정렬하면 규칙이 두 군데로 갈린다.
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
        // 주소가 비어 있으면 새 창에 빈 페이지가 열리므로 그때는 알려만 준다.
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
                if (targetUrl) {
                    window.open(targetUrl, "_blank", "noopener");
                } else {
                    alert("이 행사는 안내 사이트가 등록되어 있지 않아요.");
                }
            });
        }
    });
})();
