$(document).ready(function () {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/api",
        dataType: "json",
        success: function (response) {
            console.log(response)
            // Load basic information
            document.getElementById('location').textContent = response.location + " " + changeDateFormat(response.date)
            document.getElementById('sunrise').textContent = to24HourFormat(response.forecastInformation[0].sunrise);
            document.getElementById('sunset').textContent = to24HourFormat(response.forecastInformation[0].sunset);

            // Setup the current weather page
            document.getElementById('temp-value').textContent = response.weatherInformation.temperature + " °C";
            document.getElementById('feelsLike-value').textContent = response.weatherInformation.feelsLike + " °C"
            document.getElementById('maxTemp-value').textContent = response.forecastInformation[0].maxTemperature + " °C"
            document.getElementById('wind-value').textContent = response.weatherInformation.windSpeed + " km/h"
            document.getElementById('cloud-value').textContent = response.weatherInformation.cloud + "%"
            document.getElementById('pressure-value').textContent = response.weatherInformation.pressure + " hPa"
            document.getElementById('humidity-value').textContent = response.weatherInformation.humidity + " %"

            loadWeatherCharts(response);
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
function to24HourFormat(time12h) {
    const [time, modifier] = time12h.split(' ');

    let [hour, minutes] = time.split(":");

    hour = parseInt(hour, 10);

    if (modifier == 'PM' && hour !== 12) {
        hour += 12;
    }
    if (modifier == 'AM' && hour == 12) {
        hour = 0;
    }
    return `${hour.toString().padStart(2, '0')}:${minutes}`;

}