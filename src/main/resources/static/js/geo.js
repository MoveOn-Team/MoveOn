/**
 * 현위치 받아오기 (거리를 쓰는 화면 공통)
 *
 * 브라우저에게 위치를 물어보고, 받으면 좌표를 달아 같은 주소를 다시 부른다.
 * 컨트롤러는 lat/lng 가 오면 그 좌표로, 없으면 서울시청으로 계산한다.
 *
 * 주의 : 위치 API 는 https 또는 localhost 에서만 동작한다.
 */
(function () {
    "use strict";

    var KEY_COORDS = "moveon.coords";
    var KEY_STATE = "moveon.geoState";

    var KEY_REASON = "moveon.geoReason";

    var params = new URLSearchParams(location.search);

    // 이미 좌표를 달고 온 요청이면 다시 물어볼 이유가 없다.
    // 이 확인이 없으면 새로고침이 무한 반복된다.
    if (params.has("lat") && params.has("lng")) {
        return;
    }

    // 이미 거부했으면 다시 묻지 않는다. 대신 이유를 띄우고 다시 받아볼 길을 준다
    if (sessionStorage.getItem(KEY_STATE) === "denied") {
        showNotice(sessionStorage.getItem(KEY_REASON));
        return;
    }

    // 받아둔 좌표가 있으면 그대로 쓴다. 화면마다 GPS 를 켜면 느리고 배터리도 닳는다
    var saved = sessionStorage.getItem(KEY_COORDS);
    if (saved) {
        try {
            var c = JSON.parse(saved);
            goWith(c.lat, c.lng);
            return;
        } catch (e) {
            sessionStorage.removeItem(KEY_COORDS);
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
            maximumAge: 300000   // 5분 안에 받은 값이 있으면 재사용
        }
    );

    /** 좌표를 붙여 같은 주소를 다시 연다. 뒤로가기에 빈 페이지가 남지 않게 replace 를 쓴다. */
    function goWith(lat, lng) {
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
            sessionStorage.removeItem(KEY_STATE);
            sessionStorage.removeItem(KEY_REASON);
            sessionStorage.removeItem(KEY_COORDS);
            location.reload();
        });
    }
})();
