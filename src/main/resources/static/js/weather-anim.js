$(document).ready(function () {
    // 키는 서버에만 있다. 여기서는 우리 서버에 물어보기만 한다.
    const icon = $('#weather-icon');
    const contextPath = icon.data('context-path') || '';

    // 좌표는 화면이 그려질 때 이미 정해져 있다. 위치를 못 받았으면
    // 서버가 넣어 둔 기준점이 그대로 들어온다.
    $.getJSON(contextPath + '/recommend/api/weather',
              { lat: icon.data('lat'), lng: icon.data('lng') })
        .done(function (data) {
            // 못 받으면 빈 값이 온다. 위젯을 통째로 접는다.
            if (!data || data.temp === null || data.temp === undefined) {
                $('#weatherBox').remove();
                return;
            }
            $('#weather-temp').text(data.temp);
            if (data.icon) {
                icon.attr('src', contextPath + '/resources/images/weather/' + data.icon);
            }
        })
        .fail(function () {
            $('#weatherBox').remove();
        });
});
