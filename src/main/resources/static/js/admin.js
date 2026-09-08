/* 관리자 화면. 대회 찾기 / 삭제 확인 / 좌표 찾기 */
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

            // fresh 가 true 면 보관해 둔 결과를 무시하고 새로 찾는다
            function runSearch(fresh) {
                var keyword = (document.getElementById("searchKeyword").value || "").trim();

                // 네이버를 여러 번 부르므로 몇 초 걸린다. 멈춘 것처럼 보이지 않게 알린다
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

                            // 반려한 것은 그렇다고 말해 주되 등록 단추는 남겨 둔다
                            var mark = it.registered
                                    ? '<span class="done">이미 등록됨</span>'
                                    : (it.rejected
                                          ? '<span class="rejected">반려함</span>'
                                            + '<a class="btn-line" href="' + url + '">다시 등록</a>'
                                          : '<a class="btn-line" href="' + url + '">이걸로 등록</a>');

                            html += '<li class="' + (it.registered ? "is-done" : "") + '">'
                                  + '<span class="nm">' + escapeHtml(it.name)
                                  + (it.region
                                        ? ' <span class="rg">' + escapeHtml(it.region) + '</span>'
                                        : '')
                                  + '</span>'
                                  + mark
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

            // 화면을 열면 알아서 한 번 찾는다. 이때는 보관해 둔 결과를 쓴다
            runSearch(false);
        }

        // =============================================================
        // 2. 보내기 전에 빈 좌표를 0 으로 채운다
        // =============================================================
        // lat·lng 가 double 이라 빈 문자열을 보내면 400 이 난다.
        // 화면에는 빈 칸으로 두되 보낼 때만 0 으로 바꾼다. 0 이 '아직 없음' 이다.
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
        // 3. 삭제 전에 한 번 물어본다
        // =============================================================
        document.querySelectorAll("form.form-delete").forEach(function (form) {
            form.addEventListener("submit", function (e) {
                var title = this.dataset.title || "이 행사";
                if (!confirm("'" + title + "' 를 완전히 지웁니다.")) {
                    e.preventDefault();
                }
            });
        });

        // =============================================================
        // 4. 좌표 찾기
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
