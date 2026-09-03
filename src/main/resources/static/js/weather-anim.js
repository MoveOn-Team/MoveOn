$(document).ready(function () {
    // img 태그의 data 속성에서 contextPath 가져오기
    const $weatherIcon = $('#weather-icon');
    const contextPath = $('#weather-icon').data('context-path') || '';
    const API_KEY = $weatherIcon.data('api-key');

    function fetchWeather() {
        const url = `https://api.openweathermap.org/data/2.5/weather?q=${CITY}&appid=${API_KEY}&units=metric&lang=kr`;

        $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            success: function (data) {
                const temp = Math.round(data.main.temp);
                $('#weather-temp').text(temp);

                const description = data.weather[0].description;
                $('#summary').text(description);

                const iconCode = data.weather[0].icon;
                const svgFileName = getMatchingSvg(iconCode);

                // 이미지 경로 결합
                $('#weather-icon').attr('src', `${contextPath}/resources/images/weather/${svgFileName}`);
            },
            error: function (err) {
                console.error("날씨 정보 불러오기 실패:", err);
                $('#summary').text("날씨 정보 오류");
            }
        });
    }

    function getMatchingSvg(iconCode) {
        const mapping = {
            "01d": "day.svg",
            "01n": "night.svg",
            "02d": "cloudy-day-1.svg",
            "02n": "cloudy-night-1.svg",
            "03d": "cloudy-day-3.svg",
            "03n": "cloudy-night-3.svg",
            "04d": "cloudy.svg",
            "04n": "cloudy.svg",
            "09d": "rainy-6.svg",
            "09n": "rainy-6.svg",
            "10d": "rainy-1.svg",
            "10n": "rainy-4.svg",
            "11d": "thunder.svg",
            "11n": "thunder.svg",
            "13d": "snowy-1.svg",
            "13n": "snowy-1.svg",
            "50d": "cloudy.svg",
            "50n": "cloudy.svg"
        };

        return mapping[iconCode] || "day.svg";
    }

    fetchWeather();
});