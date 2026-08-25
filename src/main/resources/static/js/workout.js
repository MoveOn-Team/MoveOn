/* ---------------------------------------------------------------------
   즉시 운동하기 (SC-020 목록 / SC-021 상세)

   화면에서 하는 일이 많지 않다.
   종목·코스 고르기는 링크라 서버가 다시 그려 주고,
   여기서는 현위치를 이어 붙이는 일과 바깥 지도를 여는 일만 한다.

   목록을 화면에서 다시 줄 세우지 않는 이유는 행사 탭과 같다.
   거리 계산이 SQL 에 있어서, 여기서 또 정렬하면 규칙이 두 군데로 갈린다.
   --------------------------------------------------------------------- */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {

        var body = document.body;
        var lat = body.dataset.lat;
        var lng = body.dataset.lng;

        // ---------- 현위치 이어 붙이기 ----------
        // 목록 화면의 종목·코스 단추와 위쪽 탭은 모두 링크다.
        // 서버가 넘겨준 좌표가 이미 주소에 들어 있지만, GPS 로 처음 들어온 경우에는
        // 링크가 만들어질 때 좌표가 없을 수 있어 여기서 한 번 더 확인한다.
        if (lat && lng) {
            document.querySelectorAll(".main-tab-btn, .sub-tag-btn").forEach(function (a) {
                if (!a.href || a.href.indexOf("lat=") >= 0) {
                    return;
                }
                a.href += (a.href.indexOf("?") < 0 ? "?" : "&") + "lat=" + lat + "&lng=" + lng;
            });
        }

        // ---------- 더보기 ----------
        // 목록은 이미 스무 개를 다 받아 왔다. 접어 둔 것을 펴기만 하면 되므로
        // 서버에 다시 묻지 않는다. 누르고 나면 단추는 할 일이 없어 사라진다.
        var btnMore = document.querySelector("[data-more]");
        if (btnMore) {
            btnMore.addEventListener("click", function () {
                document.querySelectorAll(".facility-card.is-folded").forEach(function (card) {
                    card.classList.remove("is-folded");
                });
                this.remove();
            });
        }

        // ---------- 코스 지도 ----------
        // 서버가 넘겨준 좌표를 카카오 지도 위에 선으로 얹는다.
        // 키가 없거나 도메인 등록을 안 했으면 kakao 가 아예 없다.
        // 그때는 화면이 좌표로 그린 개념도(SVG)를 대신 보여주므로 여기서는 조용히 넘어간다.
        var mapBox = document.getElementById("courseMap");
        if (mapBox && window.kakao && kakao.maps) {
            drawCourseMap(mapBox);
        }

        // ---------- 시설 지도 ----------
        // 코스와 달리 점 하나뿐이라 선을 그을 것이 없다.
        // 좌표는 시설 1,280곳 모두 갖고 있어 빠지는 곳이 없다.
        var facBox = document.getElementById("facilityMap");
        if (facBox && window.kakao && kakao.maps) {
            var at = new kakao.maps.LatLng(
                parseFloat(facBox.dataset.lat), parseFloat(facBox.dataset.lng));
            var facMap = new kakao.maps.Map(facBox, {center: at, level: 4});
            addMark(facMap, at, facBox.dataset.name || "");
            // 지도가 크기 0 으로 그려질 때가 있어 한 번 더 맞춘다
            setTimeout(function () {
                facMap.relayout();
                facMap.setCenter(at);
            }, 200);
        }

        function drawCourseMap(box) {
            var pts;
            try {
                pts = JSON.parse(box.dataset.points || "[]");
            } catch (e) {
                return;
            }
            if (pts.length < 2) {
                return;
            }

            var path = pts.map(function (p) {
                return new kakao.maps.LatLng(p[0], p[1]);
            });

            var map = new kakao.maps.Map(box, {
                center: path[0],
                level: 5
            });

            // 코스가 다 들어오게 지도를 맞춘다.
            // 배율을 고정하면 짧은 코스는 점처럼, 긴 코스는 화면 밖으로 나간다.
            var bounds = new kakao.maps.LatLngBounds();
            path.forEach(function (ll) { bounds.extend(ll); });
            map.setBounds(bounds, 40, 40, 40, 40);

            var loop = box.dataset.loop === "true";

            // 점이 서너 개뿐인 코스는 선을 긋지 않는다.
            // 이으면 건물과 공원을 가로지르는 삼각형이 되는데,
            // 지도 위에 있으니 '진짜 저 길' 로 읽혀서 없는 길을 그리는 셈이 된다.
            // 그때는 지나는 자리만 찍어 '이 근처를 돈다' 까지만 말한다.
            var drawLine = box.dataset.line !== "false";

            if (drawLine) {
                var line = path.slice();
                if (loop) {
                    // 순환형은 마지막 점에서 첫 점으로 닫는다
                    line.push(line[0]);
                }
                new kakao.maps.Polyline({
                    map: map,
                    path: line,
                    strokeWeight: 5,
                    strokeColor: "#4a9a72",
                    strokeOpacity: 0.9
                });
            } else {
                // 선이 없으면 지나는 자리를 작은 동그라미로 찍는다
                path.slice(1, -1).forEach(function (ll) {
                    new kakao.maps.Circle({
                        map: map,
                        center: ll,
                        radius: 18,
                        strokeWeight: 2,
                        strokeColor: "#4a9a72",
                        fillColor: "#4a9a72",
                        fillOpacity: 0.5
                    });
                });
            }

            // 출발점. 순환형이면 도착점이기도 하다.
            addMark(map, path[0], loop ? "출발·도착" : "출발");
            if (!loop) {
                addMark(map, path[path.length - 1], "도착");
            }

            // 지도는 처음에 크기를 0 으로 잡고 그려질 때가 있다.
            // 한 번 더 맞춰 주면 코스가 가운데로 온다.
            setTimeout(function () {
                map.relayout();
                map.setBounds(bounds, 40, 40, 40, 40);
            }, 200);
        }

        function addMark(map, latlng, text) {
            new kakao.maps.Marker({map: map, position: latlng});
            new kakao.maps.CustomOverlay({
                map: map,
                position: latlng,
                yAnchor: 2.2,
                content: '<span class="map-pin-label">' + text + '</span>'
            });
        }

        // ---------- 상세 화면 아래 버튼 ----------
        // 주소가 비어 있으면 새 창에 빈 페이지가 열리므로 그때는 알려만 준다.
        function openFrom(el, attr, emptyMsg) {
            if (!el) {
                return;
            }
            el.addEventListener("click", function () {
                var url = this.dataset[attr];
                if (url) {
                    window.open(url, "_blank", "noopener");
                } else {
                    alert(emptyMsg);
                }
            });
        }

        openFrom(document.getElementById("btnCourseRoute"), "mapUrl",
                 "이 코스는 위치 정보가 아직 없어요.");

        // 시설 상세도 같은 두 버튼을 쓴다
        openFrom(document.getElementById("btnFacilityMap"), "mapUrl",
                 "이 시설은 위치 정보가 아직 없어요.");
        openFrom(document.getElementById("btnFacilitySite"), "targetUrl",
                 "이 시설은 안내 페이지가 등록되어 있지 않아요.");
    });
})();
