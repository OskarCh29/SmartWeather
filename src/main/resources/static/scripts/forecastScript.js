$(document).ready(function () {
    $.ajax({
        type: "GET",
        url: window.API_BASE_URL + "/weather/forecast",
        dataType: "json",
        success: function (response) {
            $("#forecastError").textContent = "";
            document.getElementById('today').textContent = "Today: " + addDaysToDate(response.date, 0);
            document.getElementById('tomorrow').textContent = "Tomorrow:  " + addDaysToDate(response.date, 1);
            document.getElementById('thirdDay').textContent = "Day after tomorrow: " + addDaysToDate(response.date, 2);
            loadForecastChart(response);

        }, error: function (xhr) {
            console.error('Cannot load foreacast information: ', xhr.responseText);
            document.getElementById('forecastError').textContent = "Application not configured - check config"
        }

    });
});

function loadForecastChart(response) {
    const forecast = response.forecastInformation;

    forecast.slice(0, 3).forEach((dayData, index) => {
        const hourly = dayData.hourlyForecast;
        const labels = hourly.map(h => h.hour);
        const temperatures = hourly.map(h => h.temperature);
        const clouds = hourly.map(h => h.cloud);
        const rainChance = hourly.map(h => h.chanceOfRain);
        const wind = hourly.map(h => h.windSpeed);

        const day = index + 1;

        new Chart(document.getElementById(`temperatureChartDay${day}`).getContext('2d'), {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Temperature [°C]',
                    data: temperatures,
                    borderColor: 'rgb(245, 211, 21)',
                    backgroundColor: 'rgba(245, 211, 21, 0.2)',
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
                        title: { display: true, text: 'Temperature (°C)' }
                    },
                    x: {
                        title: { display: true, text: 'Hour' }
                    }
                }
            }
        });
        new Chart(document.getElementById(`rainCloudChartDay${day}`).getContext('2d'), {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
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
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } },
                scales: {
                    y: { title: { display: true, text: "Percent (%)" } },
                    x: { title: { display: true, text: "Hour" } }
                }
            }
        });

        new Chart(document.getElementById(`windChartDay${day}`).getContext('2d'), {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Wind (km/h)',
                    data: wind,
                    borderColor: 'rgb(0, 130, 200)',
                    backgroundColor: 'rgba(0, 130, 200, 0.2)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } },
                scales: {
                    y: { title: { display: true, text: 'Wind (km/h)' } },
                    x: { title: { display: true, text: 'Hour' } }
                }
            }
        });

    })
}
function addDaysToDate(date, daysToAdd) {
    const dateObj = new Date(date);
    dateObj.setDate(dateObj.getDate() + daysToAdd);
    return formatDate(dateObj);
}
function formatDate(dateObj) {
    const days = String(dateObj.getDate()).padStart(2, '0');
    const month = String(dateObj.getMonth() + 1).padStart(2, '0'); // month +1 due to starting from 0
    const year = dateObj.getFullYear();
    return `${days}-${month}-${year}`;
}
