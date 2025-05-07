let settingsUnlocked = false;
let locationUnlocked = false;
$(document).ready(function () {

    // Check if already configurated
    checkConfigStatus();

    $("#unlockSettings").click(function (e) {
        e.preventDefault();
        if (settingsUnlocked) {
            lockSettings();
        }
        else {
            $("#passwordConfig").modal('show');
        }
    });
    $("#updateConfig").submit(function (e) {
        e.preventDefault();
        validateRootPassword($("#rootConfig").val());
    });

    $("#unlockLocation").click(function (e) {
        if (locationUnlocked) {
            e.preventDefault();
            lockLocation();
        } else {
            unlockLocation();
        }
    })

});

// Check config
function checkConfigStatus() {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/config",
        dataType: "json",
        success: function (response) {
            console.log(response);
            document.getElementById("locationInput").value = response.location;
            document.getElementById("mailHost").value = response.config.mail_host;
            document.getElementById("mailPort").value = response.config.mail_port;
            document.getElementById("mailName").value = response.config.mail_name;
            document.getElementById("rootEmail").value = response.rootEmail;
        }, error: function (xhr) {
            if (xhr.responseText.includes("First configuration not provided")) {
                $("#configurationModal").modal('show');
                checkPassword();
                $("#rootPassword").submit(function (e) {
                    e.preventDefault();
                    updateRootPassword();
                });
            }

        }
    });
}
// Update Root Password - First configuration
function updateRootPassword() {
    const rootPassword = {
        rootPassword: $("#root").val()
    }
    $.ajax({
        type: "POST",
        url: "http://localhost:8080/config/root",
        data: JSON.stringify(rootPassword),
        contentType: "application/json",
        success: function () {
            $("#rootPassword")[0].reset();
            $("#configurationModal").modal('hide');
        }, error: function () {
            console.log("Password was previously configured");
        }
    });
}
// Validate RootPassword by front req
function updatePasswordValidation(elementId, isValid) {
    const element = $("#" + elementId);
    if (isValid) {
        element.addClass("req-Meet");
    } else {
        element.removeClass("req-Meet");
    }
}
// Password checker function - colors for requirements
function checkPassword() {
    const input = document.getElementById('root');
    const saveButton = document.querySelector('button[form="rootPassword"]');

    input.addEventListener('input', () => {
        const value = input.value;

        const isLengthValid = value.length >= 6;
        const hasCapitalLetter = /[A-Z]/.test(value);
        const hasDigit = /\d/.test(value);

        updatePasswordValidation("correctLength", isLengthValid);
        updatePasswordValidation("capitalLetter", hasCapitalLetter);
        updatePasswordValidation("oneDigit", hasDigit);

        if (isLengthValid && hasCapitalLetter && hasDigit) {
            saveButton.disabled = false;
        }
        else {
            saveButton.disabled = true;
        }
    });

};
// Send root password to server - Validation
let rootAuthToken = null;
function validateRootPassword(password) {
    let rootPass = {
        rootPassword: password
    }
    $.ajax({
        type: "POST",
        url: "http://localhost:8080/config/validate",
        data: JSON.stringify(rootPass),
        contentType: "application/json",
        success: function (response) {
            rootAuthToken = response.userToken;
            unlockSettings();
            $("#wrongPassword").hide();
            $("#passwordConfig").modal('hide');


        }, error: function (xhr) {
            if (xhr.status == 401 || xhr.responseText.includes("Password not valid - Check Requirements")) {
                $("#wrongPassword").text("Password incorrect").show();
            }
        }
    });
}

// Unlocking root main settings
function unlockSettings() {
    $("#mailHost, #mailPort, #mailName, #mailPassword, #apiKey, #rootEmail").prop("disabled", false);

    $("#unlockSettings").removeClass("btn-outline-warning").addClass("btn-outline-success")
        .text("🔓 Lock main settings")

    settingsUnlocked = true;
}

// Lock the root settings + SAVE
function lockSettings() {
    console.log(rootAuthToken);
    $("#unlockSettings").prop("disabled", true)
    const configuration = {
        rootEmail: $("#rootEmail").val(),
        newConfig: {
            "mail_host": $("#mailHost").val(),
            "mail_port": $("#mailPort").val(),
            "mail_name": $("#mailName").val(),
            "mail_pass": $("#mailPassword").val(),
            "api_key": $("#apiKey").val()
        }
    };

    $.ajax({
        type: "POST",
        url: "http://localhost:8080/config",
        data: JSON.stringify(configuration),
        headers: {Authorization: rootAuthToken},
        contentType: "application/json",
        success: function () {
            $("#unlockSettings").prop("disabled", false)
            $("#mailHost,#mailPort, #mailName, #mailPassword,#apiKey,#rootEmail").prop("disabled", true);
            $("#unlockSettings").removeClass("btn-outline-success").addClass("btn-outline-warning")
                .text("🔒 Unlock main settings");

            settingsUnlocked = false;
            checkConfigStatus();
            $("#status-message").hide();
            $("#status-message").text("")

        }, error: function (xhr) {
            $("#unlockSettings").prop("disabled", false)
            $("#status-message").hide();
            $("#status-message").text("");

            if (xhr.status == 401) {
                displayStatusMessage("Security error: Unauthorized access to sender mail. Please check your mail configuration");
            }
            else if (xhr.status == 403) {
                displayStatusMessage("Authorization error: Provided API key is invalid - Please check your configuration");
            }
            else if (xhr.status == 400) {
                displayStatusMessage("Incorrect input error: Please check your configuration");
            }
        }
    });
}
function displayStatusMessage(message) {

    $("#status-message").text(message).show();
}

// Location field
function unlockLocation() {
    $("#locationInput").prop("disabled", false);
    $("#unlockLocation").removeClass("btn-outline-warning").addClass("btn-outline-success")
        .text("🔓")

    locationUnlocked = true;
}
function lockLocation() {
    $("#unlockLocation").prop("disabled", true)
    const locationRequest = {
        location: $("#locationInput").val()
    }
    $.ajax({
        type: "POST",
        url: "http://localhost:8080/config/location",
        data: JSON.stringify(locationRequest),
        contentType: "application/json",
        success: function () {
            $("#unlockLocation").removeClass("btn-outline-success").addClass("btn-outline-warning")
                .text("🔒");
            $("#locationInput").prop("disabled", true);
            locationUnlocked = false;
            $("#location-status").text("").hide();
        }, error(xhr) {
            if (xhr.responseText.includes("Api key not provided")) {
                $("#location-status").text("Authorization error: Cannot check location - API Key missing");
                $("#location-status").show();
            } else {
                $("#location-status").text("Location not found").show();
            }
        },
        complete: function () {
            $("#unlockLocation").prop("disabled", false);
        }
    });
}
