/**
 * 현위치 받아오기 (추천 · 즉시운동 · 행사 화면 공통)
 *
 * 브라우저에게 위치를 물어보고, 받으면 쿠키(moveon_coords)에 굽고 화면을 다시 부른다.
 * 서버의 GeoCookieFilter 가 쿠키를 lat/lng 로 바꿔 넣고,
 * 컨트롤러는 좌표가 오면 그 좌표로, 없으면 서울시청으로 계산한다.
 *
 * 전에는 주소에 좌표를 달았는데, 그러면 내 위치가 주소창과 공유 링크에 그대로 보였다.
 *
 * 쿠키는 5분 뒤 저절로 사라진다. 그 뒤 화면을 옮기면 다시 잰다.
 * 화면마다 GPS 를 켜면 느리고 배터리도 닳지만, 한 번 받고 계속 쓰면
 * 집에서 켜 두고 밖에 나가도 집 기준으로 남는다.
 *
 * 주의 : 위치 API 는 https 또는 localhost 에서만 동작한다.
 */
(function () {
    "use strict";

    var COOKIE = "moveon_coords";
    var KEY_STATE = "moveon.geoState";
    var KEY_REASON = "moveon.geoReason";

    /** 받아 둔 좌표를 이만큼(초)만 믿는다 */
    var MAX_AGE_S = 5 * 60;

    // 옛 즐겨찾기처럼 주소에 좌표가 달려 왔으면 주소창에서만 떼어 둔다.
    // 서버는 이미 그 좌표로 그렸으니 다시 부를 필요는 없다.
    hideCoordsInUrl();

    // 권한을 끈 것을 우리가 모르면, 받아 둔 좌표를 계속 쓰게 된다.
    if (navigator.permissions && navigator.permissions.query) {
        navigator.permissions.query({name: "geolocation"})
            .then(function (st) {
                if (st.state !== "denied") {
                    start();
                    return;
                }
                // 받아 둔 것을 버린다. 버리기 전 화면은 그 좌표로 그렸으니 다시 부른다
                var had = hasCookie();
                forget();
                if (had) {
                    location.reload();
                    return;
                }
                showNotice("위치 권한이 꺼져 있어요. 주소창 왼쪽 자물쇠에서 켤 수 있어요.");
            })
            .catch(start);
    } else {
        start();
    }

    function start() {

        // 쿠키가 살아 있으면 서버가 이미 그 좌표로 그렸다. 여기서 끝낸다
        if (hasCookie()) {
            return;
        }

        // 이미 거부했으면 다시 묻지 않는다. 대신 이유를 띄우고 다시 받아볼 길을 준다
        if (sessionStorage.getItem(KEY_STATE) === "denied") {
            showNotice(sessionStorage.getItem(KEY_REASON));
            return;
        }

        if (!navigator.geolocation) {
            return; // 지원하지 않는 브라우저는 기본 좌표로 둔다
        }

        navigator.geolocation.getCurrentPosition(
            function (pos) {
                // 소수점 6자리면 약 0.1m 단위다. 그 이상은 의미가 없다.
                var lat = pos.coords.latitude.toFixed(6);
                var lng = pos.coords.longitude.toFixed(6);

                sessionStorage.setItem(KEY_STATE, "ok");
                document.cookie = COOKIE + "=" + lat + "_" + lng
                    + "; max-age=" + MAX_AGE_S + "; path=/; samesite=lax"
                    + (location.protocol === "https:" ? "; secure" : "");

                // 쿠키를 막아 둔 브라우저에서 새로고침이 끝없이 돌지 않게
                if (hasCookie()) {
                    location.reload();
                }
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

    function hasCookie() {
        return document.cookie.split("; ").some(function (c) {
            return c.indexOf(COOKIE + "=") === 0;
        });
    }

    /** 받아 둔 것을 버린다. 권한이 꺼졌거나 다시 받고 싶을 때 */
    function forget() {
        document.cookie = COOKIE + "=; max-age=0; path=/";
        sessionStorage.removeItem(KEY_STATE);
        sessionStorage.removeItem(KEY_REASON);
    }

    function hideCoordsInUrl() {
        var params = new URLSearchParams(location.search);
        if (!params.has("lat") && !params.has("lng")) {
            return;
        }
        params.delete("lat");
        params.delete("lng");
        var q = params.toString();
        history.replaceState(null, "", location.pathname + (q ? "?" + q : "") + location.hash);
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
            location.reload();
        });
    }
})();
