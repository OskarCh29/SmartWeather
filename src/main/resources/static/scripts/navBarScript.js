$(document).ready(function () {
    $("#homeButton").click(function (e) {
        e.preventDefault();
        window.location.href = "/index.html";
    });

    $("#forecastButton").click(function (e) {
        e.preventDefault();
        window.location.href = "/forecast.html";
    });
    $("#configButton").click(function (e) {
        e.preventDefault();
        window.location.href = "/config.html";
    });

    const path = window.location.pathname;
    $("#homeButton, #forecastButton, #configButton").removeClass("active");

    if (path.includes("index.html") || path === "/") {
        $("#homeButton").addClass("active");
    }
    else if (path.includes("forecast.html")) {
        $("#forecastButton").addClass("active");
    }
    else if (path.includes("config.html")) {
        $("#configButton").addClass("active");
    }
});