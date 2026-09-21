/**
 * 현위치 받아오기 (거리를 쓰는 화면 공통)
 *
 * 브라우저에게 위치를 물어보고, 받으면 좌표를 달아 같은 주소를 다시 부른다.
 * 컨트롤러는 lat/lng 가 오면 그 좌표로, 없으면 서울시청으로 계산한다.
 *
 * 받아 둔 좌표는 5분까지만 쓴다. 그 뒤에는 화면을 옮길 때 다시 잰다.
 * 화면마다 GPS 를 켜면 느리고 배터리도 닳지만, 한 번 받고 탭을 닫을 때까지
 * 계속 쓰면 집에서 켜 두고 밖에 나가도 집 기준으로 남는다.
 *
 * 주의 : 위치 API 는 https 또는 localhost 에서만 동작한다.
 */
(function () {
    "use strict";

    var KEY_COORDS = "moveon.coords";
    var KEY_AT = "moveon.coordsAt";
    var KEY_STATE = "moveon.geoState";
    var KEY_REASON = "moveon.geoReason";

    /** 받아 둔 좌표를 이만큼만 믿는다 */
    var MAX_AGE_MS = 5 * 60 * 1000;

    var params = new URLSearchParams(location.search);

    // 권한을 끈 것을 우리가 모르면, 받아 둔 좌표를 계속 쓰게 된다.
    // 껐는데도 내 좌표가 주소창에 실려 나가는 셈이라 먼저 확인한다.
    if (navigator.permissions && navigator.permissions.query) {
        navigator.permissions.query({name: "geolocation"})
            .then(function (st) {
                if (st.state !== "denied") {
                    start();
                    return;
                }
                // 받아 둔 것을 버리고, 주소에 남은 좌표까지 뗀다.
                // 떼지 않으면 껐는데도 그 좌표로 계산한 화면이 그대로 나온다.
                forget();
                if (params.has("lat") || params.has("lng")) {
                    stripCoords();
                    return;
                }
                showNotice("위치 권한이 꺼져 있어요. 주소창 왼쪽 자물쇠에서 켤 수 있어요.");
            })
            .catch(start);
    } else {
        start();
    }

    function start() {

        // 좌표를 달고 온 요청이라도 그 좌표가 오래됐으면 다시 잰다.
        // 싱싱하면 여기서 끝낸다. 이 확인이 없으면 새로고침이 무한 반복된다.
        if (params.has("lat") && params.has("lng") && fresh()) {
            return;
        }

        // 이미 거부했으면 다시 묻지 않는다. 대신 이유를 띄우고 다시 받아볼 길을 준다
        if (sessionStorage.getItem(KEY_STATE) === "denied") {
            showNotice(sessionStorage.getItem(KEY_REASON));
            return;
        }

        // 받아둔 좌표가 아직 쓸 만하면 그대로 쓴다
        var saved = sessionStorage.getItem(KEY_COORDS);
        if (saved && fresh()) {
            try {
                var c = JSON.parse(saved);
                goWith(c.lat, c.lng);
                return;
            } catch (e) {
                forget();
            }
        }

        if (!navigator.geolocation) {
            return; // 지원하지 않는 브라우저는 기본 좌표로 둔다
        }

        navigator.geolocation.getCurrentPosition(
            function (pos) {
                // 소수점 6자리면 약 0.1m 단위다. 그 이상은 의미가 없다.
                var lat = pos.coords.latitude.toFixed(6);
                var lng = pos.coords.longitude.toFixed(6);

                sessionStorage.setItem(KEY_COORDS, JSON.stringify({lat: lat, lng: lng}));
                sessionStorage.setItem(KEY_AT, String(Date.now()));
                sessionStorage.setItem(KEY_STATE, "ok");
                goWith(lat, lng);
            },
            function (err) {
                // 못 받아도 화면은 그대로 두고 기본 좌표를 쓴다. 다만 조용히 넘어가지는 않는다
                sessionStorage.setItem(KEY_STATE, "denied");
                sessionStorage.setItem(KEY_REASON, reasonOf(err));
                showNotice(reasonOf(err));
            },
            {
                enableHighAccuracy: true,
                timeout: 8000,
                // 이 값이 위의 5분보다 크면 브라우저가 옛 값을 돌려줘 다시 재는 뜻이 없어진다
                maximumAge: 60000
            }
        );
    }

    /** 받아 둔 좌표가 아직 쓸 만한지 */
    function fresh() {
        var at = parseInt(sessionStorage.getItem(KEY_AT), 10);
        return at > 0 && (Date.now() - at) < MAX_AGE_MS;
    }

    /** 받아 둔 것을 버린다. 권한이 꺼졌거나 값이 깨졌을 때 */
    function forget() {
        sessionStorage.removeItem(KEY_COORDS);
        sessionStorage.removeItem(KEY_AT);
        sessionStorage.removeItem(KEY_STATE);
        sessionStorage.removeItem(KEY_REASON);
    }

    /**
     * 좌표를 붙여 같은 주소를 다시 연다. 뒤로가기에 빈 페이지가 남지 않게 replace 를 쓴다.
     *
     * 주소에 이미 같은 좌표가 있으면 아무것도 하지 않는다.
     * 5분이 지나 다시 쟀는데 제자리였을 때 화면을 한 번 더 그리지 않기 위해서다.
     */
    function goWith(lat, lng) {
        if (params.get("lat") === String(lat) && params.get("lng") === String(lng)) {
            return;
        }
        params.set("lat", lat);
        params.set("lng", lng);
        location.replace(location.pathname + "?" + params.toString());
    }

    /** 왜 못 받았는지 회원이 알아들을 말로 바꾼다 */
    function reasonOf(err) {
        if (!err) {
            return "위치를 받지 못했어요.";
        }
        if (err.code === 1) {   // PERMISSION_DENIED
            return "위치 권한이 꺼져 있어요. 주소창 왼쪽 자물쇠에서 켤 수 있어요.";
        }
        if (err.code === 2) {   // POSITION_UNAVAILABLE
            return "위치를 찾지 못했어요. 실내에서는 어려울 수 있어요.";
        }
        if (err.code === 3) {   // TIMEOUT
            return "위치를 받는 데 오래 걸려요.";
        }
        return "위치를 받지 못했어요.";
    }

    /** 화면 맨 위에 한 줄 띄운다. 알리기만 하면 할 수 있는 게 없어 단추도 같이 둔다 */
    function showNotice(reason) {
        if (document.getElementById("geoNotice")) {
            return;
        }
        var shell = document.querySelector(".auth-shell, .container");
        if (!shell) {
            return;
        }

        var box = document.createElement("div");
        box.id = "geoNotice";
        box.className = "geo-notice";
        box.innerHTML =
            '<span class="geo-notice-text">' +
            (reason || "위치를 받지 못했어요.") +
            ' 서울시청 기준으로 보여드려요.</span>' +
            '<button type="button" class="geo-notice-btn">현위치로 다시 보기</button>';

        // 제목보다 위에 둔다. 결과를 보기 전에 어떤 기준인지 알아야 한다
        shell.insertBefore(box, shell.firstChild);

        box.querySelector(".geo-notice-btn").addEventListener("click", function () {
            forget();
            stripCoords();
        });
    }

    /** 주소에서 좌표를 떼고 같은 화면을 다시 연다 */
    function stripCoords() {
        params.delete("lat");
        params.delete("lng");
        var q = params.toString();
        location.replace(location.pathname + (q ? "?" + q : ""));
    }
})();
