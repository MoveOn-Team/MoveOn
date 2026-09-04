/**
 * 현위치 받아오기 (거리를 쓰는 화면 공통)
 *
 * 컨트롤러는 lat / lng 파라미터를 받으면 그 좌표로, 없으면 서울시청 좌표로 계산한다.
 * 여기서는 브라우저에게 위치를 물어보고, 받으면 좌표를 달아 같은 주소를 다시 부른다.
 *
 * 전에는 이 파일 이름이 recommend.js 라 추천 탭에서만 불렀다.
 * 그래서 행사·즉시운동 탭은 현위치를 아예 못 받고 늘 서울시청 기준으로 재고 있었다.
 * 강서구에서 열어도 '인사동삼청동 나들길 1.3km' 처럼 말이 안 되는 거리가 나왔다.
 * 하는 일이 추천과 상관없으므로 이름을 geo.js 로 바꾸고 네 화면이 함께 쓴다.
 *
 * 주의
 *   위치 API 는 https 또는 localhost 에서만 동작한다.
 *   폰에서 http://192.168.x.x:8080 으로 접속하면 브라우저가 아예 막는다.
 *   앱으로 감싼 뒤에는 안드로이드가 직접 좌표를 넘겨줄 예정이라 이 파일은 웹 테스트용이다.
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

    // 이번 세션에서 이미 거부했으면 다시 묻지 않는다.
    // 대신 왜 못 받았는지 화면에 띄우고, 다시 받아볼 길을 준다.
    if (sessionStorage.getItem(KEY_STATE) === "denied") {
        showNotice(sessionStorage.getItem(KEY_REASON));
        return;
    }

    // 이번 세션에서 이미 받아둔 좌표가 있으면 그대로 쓴다.
    // 화면을 옮길 때마다 GPS 를 다시 켜면 느리고 배터리도 닳는다.
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
            // 거부·시간초과·실패 모두 화면은 그대로 두고 기본 좌표를 쓴다.
            // 위치를 못 받았다고 추천을 못 보여줄 이유는 없다.
            //
            // 다만 조용히 넘어가지는 않는다.
            // 서울시청에서 잰 거리를 '현위치' 로 읽으면 회원이 속는 셈이고,
            // 왜 그런지 모르면 고칠 방법도 없다.
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

    /**
     * 왜 못 받았는지 회원이 알아들을 말로 바꾼다.
     * 브라우저가 주는 code 는 셋뿐이라 각각 할 수 있는 일이 다르다.
     */
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

    /**
     * 화면 맨 위에 한 줄 띄운다.
     *
     * 결과에 붙는 작은 글씨('서울시청 기준')만으로는 놓치기 쉽다.
     * 알리기만 하고 끝내면 회원이 할 수 있는 게 없으므로 다시 받는 단추를 같이 둔다.
     */
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

        // 제목보다 위에 둔다. 결과를 보기 전에 어떤 기준인지 알아야 한다.
        shell.insertBefore(box, shell.firstChild);

        box.querySelector(".geo-notice-btn").addEventListener("click", function () {
            // 막아 둔 것을 풀고 처음부터 다시 물어본다.
            sessionStorage.removeItem(KEY_STATE);
            sessionStorage.removeItem(KEY_REASON);
            sessionStorage.removeItem(KEY_COORDS);
            location.reload();
        });
    }
})();
