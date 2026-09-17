$(document).ready(function () {
    // 키는 서버에만 있다. 여기서는 우리 서버에 물어보기만 한다.
    const contextPath = $('#weather-icon').data('context-path') || '';

    $.getJSON(contextPath + '/recommend/api/weather')
        .done(function (data) {
            // 못 받으면 빈 값이 온다. 위젯을 통째로 접는다.
            if (!data || data.temp === null || data.temp === undefined) {
                $('#weatherBox').remove();
                return;
            }
            $('#weather-temp').text(data.temp);
            if (data.icon) {
                $('#weather-icon').attr('src', contextPath + '/resources/images/weather/' + data.icon);
            }
        })
        .fail(function () {
            $('#weatherBox').remove();
        });
});
