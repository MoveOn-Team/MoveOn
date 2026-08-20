/* ---------------------------------------------------------------------
   관리자 화면

   두 가지를 한다.
     1. 대회 찾기   화면을 열면 알아서 검색해 목록으로 보여준다
     2. 좌표 찾기   장소 이름으로 카카오에 물어 폼을 채운다

   결과를 DB 에 저장하지 않는다. 화면에 뿌리고 끝이다.
   --------------------------------------------------------------------- */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {

        var ctx = document.body.dataset.contextPath || "";

        // =============================================================
        // 1. 대회 찾기
        // =============================================================
        var btnSearch = document.getElementById("btnSearch");
        var box = document.getElementById("searchResult");

        if (btnSearch && box) {

            // fresh 가 true 면 보관해 둔 결과를 무시하고 새로 찾는다.
            // 화면을 열 때는 보관분을 쓴다. 그래야 오갈 때마다 목록이 흔들리지 않는다.
            function runSearch(fresh) {
                var keyword = (document.getElementById("searchKeyword").value || "").trim();

                // 블로그·뉴스를 여러 번 부르므로 몇 초 걸린다. 멈춘 것처럼 보이지 않게 알린다.
                box.innerHTML = '<p class="searching">찾는 중입니다…</p>';
                btnSearch.disabled = true;

                fetch(ctx + "/admin/searchEvents?keyword=" + encodeURIComponent(keyword)
                          + (fresh ? "&refresh=1" : ""),
                      {credentials: "same-origin"})
                    .then(function (r) { return r.json(); })
                    .then(function (list) {
                        btnSearch.disabled = false;

                        if (!list || list.length === 0) {
                            box.innerHTML = '<p class="empty">찾은 대회가 없습니다. 검색어를 바꿔 보세요.</p>';
                            return;
                        }

                        var html = '<p class="found">' + list.length + '개를 찾았습니다. ' +
                                   '아직 등록하지 않은 대회가 위에 있습니다.</p><ul class="found-list">';

                        list.forEach(function (it) {
                            var url = ctx + "/admin/eventForm?name=" + encodeURIComponent(it.name);
                            html += '<li class="' + (it.registered ? "is-done" : "") + '">'
                                  + (it.mentions
                                        ? '<span class="cnt">' + it.mentions + '</span>'
                                        : '')
                                  + '<span class="nm">' + escapeHtml(it.name)
                                  + (it.region
                                        ? ' <span class="rg">' + escapeHtml(it.region) + '</span>'
                                        : '')
                                  + '</span>'
                                  + (it.registered
                                        ? '<span class="done">이미 등록됨</span>'
                                        : '<a class="btn-line" href="' + url + '">이걸로 등록</a>')
                                  + '</li>';
                        });

                        box.innerHTML = html + "</ul>";
                    })
                    .catch(function (e) {
                        btnSearch.disabled = false;
                        box.innerHTML = '<p class="empty">검색에 실패했습니다. ' +
                                        '잠시 뒤 다시 눌러 보세요.</p>';
                        console.error(e);
                    });
            }

            // 버튼을 누르면 새로 찾는다
            btnSearch.addEventListener("click", function () { runSearch(true); });

            // 화면을 열면 알아서 한 번 찾는다.
            // 관리자가 할 일이 '새로 올라온 대회 확인' 이라 버튼을 누르게 할 이유가 없다.
            // 이때는 보관해 둔 결과를 쓴다.
            runSearch(false);
        }

        // =============================================================
        // 2. 보내기 전에 빈 좌표를 0 으로 채운다
        // =============================================================
        // EventDTO 의 lat·lng 는 double 이라 빈 문자열을 못 받는다.
        // 그대로 보내면 "Failed to convert java.lang.String to double" 로 400 이 난다.
        //
        // 좌표는 아직 안 채워도 저장할 수 있어야 한다.
        // 공지가 안 올라온 대회를 '검수 대기' 로 담아 두는 게 이 화면의 쓸모라서다.
        // 그래서 화면에는 빈 칸으로 두되, 보낼 때만 0 으로 바꾼다. 0 이 '아직 없음' 이다.
        var eventForm = document.querySelector("form.panel");
        if (eventForm) {
            eventForm.addEventListener("submit", function () {
                ["lat", "lng"].forEach(function (id) {
                    var el = document.getElementById(id);
                    if (el && !el.value.trim()) {
                        el.value = "0";
                    }
                });
            });
        }

        // =============================================================
        // 3. 좌표 찾기
        // =============================================================
        var btnFindPlace = document.getElementById("btnFindPlace");

        if (btnFindPlace) {
            btnFindPlace.addEventListener("click", function () {
                var name = (document.getElementById("placeName").value || "").trim();
                if (!name) {
                    alert("장소 이름을 먼저 적어 주세요.");
                    return;
                }

                btnFindPlace.disabled = true;
                btnFindPlace.textContent = "찾는 중";

                fetch(ctx + "/admin/findPlace?placeName=" + encodeURIComponent(name),
                      {credentials: "same-origin"})
                    .then(function (r) { return r.json(); })
                    .then(function (res) {
                        btnFindPlace.disabled = false;
                        btnFindPlace.textContent = "좌표 찾기";

                        if (!res.ok || !res.place) {
                            alert("그 이름으로는 못 찾았습니다.\n" +
                                  "'여의도 한강공원' 처럼 조금 더 넓은 이름으로 해 보세요.");
                            return;
                        }

                        var p = res.place;
                        document.getElementById("lat").value = p.lat;
                        document.getElementById("lng").value = p.lng;
                        document.getElementById("sido").value = p.sido || "";
                        document.getElementById("sigungu").value = p.sigungu || "";

                        // 카카오가 아는 정식 이름으로 바꿔 둔다. 길찾기 링크가 정확해진다.
                        if (p.placeName) {
                            document.getElementById("placeName").value = p.placeName;
                        }
                    })
                    .catch(function (e) {
                        btnFindPlace.disabled = false;
                        btnFindPlace.textContent = "좌표 찾기";
                        alert("좌표를 찾지 못했습니다.");
                        console.error(e);
                    });
            });
        }

        function escapeHtml(s) {
            return String(s).replace(/[&<>"']/g, function (c) {
                return {"&": "&amp;", "<": "&lt;", ">": "&gt;",
                        '"': "&quot;", "'": "&#39;"}[c];
            });
        }
    });
})();
