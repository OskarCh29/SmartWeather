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

    var activeTab = localStorage.getItem("activeTab");
    if (activeTab == "home") {
        $("#homeButton").addClass("active");
        $("#forecastButton").removeClass("active");
        $("#configButton").removeClass("active");
    } else if (activeTab == "forecast") {
        $("#homeButton").removeClass("active");
        $("#forecastButton").addClass("active");
        $("#configButton").removeClass("active");
    } else if (activeTab == "config") {
        $("#homeButton").removeClass("active");
        $("#forecastButton").removeClass("active");
        $("#configButton").addClass("active");
    }
});