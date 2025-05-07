$(document).ready(function () {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/weather",
        dataType: "json",
        success: function (response) {
            console.log(response)
            // Load basic information
            $("#location").removeClass("text-danger").addClass("text-info");
            document.getElementById('location').textContent = response.location + " " + changeDateFormat(response.date)
            document.getElementById('sunrise').textContent = response.forecastInformation[0].sunrise;
            document.getElementById('sunset').textContent = response.forecastInformation[0].sunset;

            // Setup the current weather page
            document.getElementById('temp-value').textContent = response.weatherInformation.temperature + " °C";
            document.getElementById('feelsLike-value').textContent = response.weatherInformation.feelsLike + " °C"
            document.getElementById('maxTemp-value').textContent = response.forecastInformation[0].maxTemperature + " °C"
            document.getElementById('wind-value').textContent = response.weatherInformation.windSpeed + " km/h"
            document.getElementById('cloud-value').textContent = response.weatherInformation.cloud + " %"
            document.getElementById('pressure-value').textContent = response.weatherInformation.pressure + " hPa"
            document.getElementById('humidity-value').textContent = response.weatherInformation.humidity + " %"

            loadWeatherCharts(response);
            updateWeatherIcon(response);
        },error: function(xhr){
            console.error('Error while loading weather data:' , xhr.responseText);
            document.getElementById('location').textContent = "Application not configured - check config"
            $("#location").removeClass("text-info").addClass("text-danger");
        }
    });
});
function loadWeatherCharts(response) {
    const hourly = response.forecastInformation[0].hourlyForecast;

    const labels = hourly.map(h => h.hour);
    const temperatures = hourly.map(h => h.temperature);
    const clouds = hourly.map(h => h.cloud);
    const rainChance = hourly.map(h => h.chanceOfRain);

    new Chart(document.getElementById('temperatureChart').getContext('2d'), {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Temperature (°C)',
                data: temperatures,
                borderColor: 'rgb(245, 211, 21)',
                backgroundColor: 'rgba(245, 211, 21, 0.2)',
                fill: true,
                tension: 0.3
            }],
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: true }
            },
            scales: {
                y: {
                    title: { display: true, text: 'Temperature (°C)' }
                },
                x: {
                    title: { display: true, text: 'Hour' }
                }
            }
        }
    });

    new Chart(document.getElementById('rainCloudChart').getContext('2d'), {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Cloud (%)',
                data: clouds,
                borderColor: 'rgb(179, 179, 179)',
                backgroundColor: 'rgba(179, 179, 179,0.2)',
                fill: true,
                tension: 0.3
            },
            {
                label: 'Chance of rain (%)',
                data: rainChance,
                borderColor: 'rgb(0, 58, 184)',
                backgroundColor: 'rgba(0, 58, 184,0.2)',
                fill: true,
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: true }
            },
            scales: {
                y: {
                    title: { display: true, text: "Percent (%)" }
                },
                x: {
                    title: { display: true, text: "Hour" }
                }
            }
        }
    });
}

function changeDateFormat(date) {
    return date.split("-").reverse().join("-");
}

setInterval(getCurrentTime, 1000);
function getCurrentTime() {
    const timeNow = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false });
    document.getElementById('time').textContent = timeNow;
}

function updateWeatherIcon(response) {
    const weatherIcons = [
        {
            name: "WarmDay",
            icon: `<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" fill="currentColor" class="bi bi-thermometer-sun" viewBox="0 0 16 16">
            <path d="M5 12.5a1.5 1.5 0 1 1-2-1.415V2.5a.5.5 0 0 1 1 0v8.585A1.5 1.5 0 0 1 5 12.5"/>
            <path d="M1 2.5a2.5 2.5 0 0 1 5 0v7.55a3.5 3.5 0 1 1-5 0zM3.5 1A1.5 1.5 0 0 0 2 2.5v7.987l-.167.15a2.5 2.5 0 1 0 3.333 0L5 10.486V2.5A1.5 1.5 0 0 0 3.5 1m5 1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-1 0v-1a.5.5 0 0 1 .5-.5m4.243 1.757a.5.5 0 0 1 0 .707l-.707.708a.5.5 0 1 1-.708-.708l.708-.707a.5.5 0 0 1 .707 0M8 5.5a.5.5 0 0 1 .5-.5 3 3 0 1 1 0 6 .5.5 0 0 1 0-1 2 2 0 0 0 0-4 .5.5 0 0 1-.5-.5M12.5 8a.5.5 0 0 1 .5-.5h1a.5.5 0 1 1 0 1h-1a.5.5 0 0 1-.5-.5m-1.172 2.828a.5.5 0 0 1 .708 0l.707.708a.5.5 0 0 1-.707.707l-.708-.707a.5.5 0 0 1 0-.708M8.5 12a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-1 0v-1a.5.5 0 0 1 .5-.5"/>
            </svg>`
        },
        {
            name: "FreezingDay",
            icon: `<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" fill="currentColor" class="bi bi-thermometer-snow" viewBox="0 0 16 16">
            <path d="M5 12.5a1.5 1.5 0 1 1-2-1.415V9.5a.5.5 0 0 1 1 0v1.585A1.5 1.5 0 0 1 5 12.5"/>
            <path d="M1 2.5a2.5 2.5 0 0 1 5 0v7.55a3.5 3.5 0 1 1-5 0zM3.5 1A1.5 1.5 0 0 0 2 2.5v7.987l-.167.15a2.5 2.5 0 1 0 3.333 0L5 10.486V2.5A1.5 1.5 0 0 0 3.5 1m5 1a.5.5 0 0 1 .5.5v1.293l.646-.647a.5.5 0 0 1 .708.708L9 5.207v1.927l1.669-.963.495-1.85a.5.5 0 1 1 .966.26l-.237.882 1.12-.646a.5.5 0 0 1 .5.866l-1.12.646.884.237a.5.5 0 1 1-.26.966l-1.848-.495L9.5 8l1.669.963 1.849-.495a.5.5 0 1 1 .258.966l-.883.237 1.12.646a.5.5 0 0 1-.5.866l-1.12-.646.237.883a.5.5 0 1 1-.966.258L10.67 9.83 9 8.866v1.927l1.354 1.353a.5.5 0 0 1-.708.708L9 12.207V13.5a.5.5 0 0 1-1 0v-11a.5.5 0 0 1 .5-.5"/>
            </svg>`
        },
        {
            name: "ColdDay",
            icon: `<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" fill="currentColor" class="bi bi-thermometer-low" viewBox="0 0 16 16">
            <path d="M9.5 12.5a1.5 1.5 0 1 1-2-1.415V9.5a.5.5 0 0 1 1 0v1.585a1.5 1.5 0 0 1 1 1.415"/>
            <path d="M5.5 2.5a2.5 2.5 0 0 1 5 0v7.55a3.5 3.5 0 1 1-5 0zM8 1a1.5 1.5 0 0 0-1.5 1.5v7.987l-.167.15a2.5 2.5 0 1 0 3.333 0l-.166-.15V2.5A1.5 1.5 0 0 0 8 1"/>
            </svg>`
        },
        {
            name: "SunnyDay",
            icon: `<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" fill="currentColor" class="bi bi-thermometer-half" viewBox="0 0 16 16">
            <path d="M9.5 12.5a1.5 1.5 0 1 1-2-1.415V6.5a.5.5 0 0 1 1 0v4.585a1.5 1.5 0 0 1 1 1.415"/>
            <path d="M5.5 2.5a2.5 2.5 0 0 1 5 0v7.55a3.5 3.5 0 1 1-5 0zM8 1a1.5 1.5 0 0 0-1.5 1.5v7.987l-.167.15a2.5 2.5 0 1 0 3.333 0l-.166-.15V2.5A1.5 1.5 0 0 0 8 1"/>
            </svg>`
        },
        {
            name: "Cloudly",
            icon: `<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" fill="currentColor" class="bi bi-cloud-sun-fill" viewBox="0 0 16 16">
            <path d="M11.473 11a4.5 4.5 0 0 0-8.72-.99A3 3 0 0 0 3 16h8.5a2.5 2.5 0 0 0 0-5z"/>
            <path d="M10.5 1.5a.5.5 0 0 0-1 0v1a.5.5 0 0 0 1 0zm3.743 1.964a.5.5 0 1 0-.707-.707l-.708.707a.5.5 0 0 0 .708.708zm-7.779-.707a.5.5 0 0 0-.707.707l.707.708a.5.5 0 1 0 .708-.708zm1.734 3.374a2 2 0 1 1 3.296 2.198q.3.423.516.898a3 3 0 1 0-4.84-3.225q.529.017 1.028.129m4.484 4.074c.6.215 1.125.59 1.522 1.072a.5.5 0 0 0 .039-.742l-.707-.707a.5.5 0 0 0-.854.377M14.5 6.5a.5.5 0 0 0 0 1h1a.5.5 0 0 0 0-1z"/>
            </svg>`
        },
        {
            name: "Rainy",
            icon: `<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" fill="currentColor" class="bi bi-cloud-rain-heavy-fill" viewBox="0 0 16 16">
            <path d="M4.176 11.032a.5.5 0 0 1 .292.643l-1.5 4a.5.5 0 0 1-.936-.35l1.5-4a.5.5 0 0 1 .644-.293m3 0a.5.5 0 0 1 .292.643l-1.5 4a.5.5 0 0 1-.936-.35l1.5-4a.5.5 0 0 1 .644-.293m3 0a.5.5 0 0 1 .292.643l-1.5 4a.5.5 0 0 1-.936-.35l1.5-4a.5.5 0 0 1 .644-.293m3 0a.5.5 0 0 1 .292.643l-1.5 4a.5.5 0 0 1-.936-.35l1.5-4a.5.5 0 0 1 .644-.293m.229-7.005a5.001 5.001 0 0 0-9.499-1.004A3.5 3.5 0 1 0 3.5 10H13a3 3 0 0 0 .405-5.973"/>
            </svg>`
        }
    ]

    let icon = document.getElementById("weather-icon");

    icon.innerHTML = '';
    if (response.temperature > 15) {
        icon.innerHTML = weatherIcons[0].icon;
    }
    else if (response.temperature < 0) {
        icon.innerHTML = weatherIcons[1].icon;
    }
    else if (response.temperature < 10) {
        icon.innerHTML = weatherIcons[2].icon;
    }
    else if (response.temperature >= 10 && response.temperature <= 15) {
        icon.innerHTML = weatherIcons[3].icon;
    }
    else if (response.cloud > 50) {
        icon.innerHTML = weatherIcons[4].icon;
    }
    else if (response.chanceOfRain > 50) {
        icon.innerHTML = weatherIcons[5].icon;
    }
    else {
        icon.innerHTML = weatherIcons[3].icon;
    }
}