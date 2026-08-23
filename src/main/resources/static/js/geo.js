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

    var params = new URLSearchParams(location.search);

    // 이미 좌표를 달고 온 요청이면 다시 물어볼 이유가 없다.
    // 이 확인이 없으면 새로고침이 무한 반복된다.
    if (params.has("lat") && params.has("lng")) {
        return;
    }

    // 이번 세션에서 이미 거부했으면 다시 묻지 않는다
    if (sessionStorage.getItem(KEY_STATE) === "denied") {
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
            sessionStorage.setItem(KEY_STATE, "denied");
            console.log("현위치를 받지 못해 기본 좌표(서울시청)로 계산합니다.", err.message);
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
})();
