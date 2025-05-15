# SmartWeather on RaspberryPI

Weather&Forecast app for informing user about weather based on the location and provided schedule. Application based on the external weather api genering e-mail templates.

## Table of Contents

- [Requirements](#requirements)
- [Installation and configuration](#installation-and-configuration)
- [Raspberry Pi configuration](#raspberry-pi-configuration)
- [Important Notes](#important-notes)
- [Documentation](#documentation)
- [Usage](#usage)
- [Technologies](#technologies)
- [Testing](#testing)
- [Roadmap](#roadmap)

## Requirements
- Java 21
- Maven 3.9.4 +
- Docker (for testing purpose)
- Docker Compose (together with docker to run on Raspberry Pi)
- MongoDB 4.4.18 (compatible version with Raspberry Pi architecture)

## Installation and configuration
1. Clone app repository to your local machine <br>
```shell
https://github.com/OskarCh29/SmartWeather.git
```
2. Ensure that you set up MongoDB and Docker to be ready for running (for PI information check next section)
3. Basically, MongoDB is running as a docker container database on <b>27017</b>. If configuring for other port navigate to `resource` folder in application directory and `application.yaml`.
4. Configure MongoDB port if working on other than 27017 (`mongodb-uri section`)
5. After finished lunch your database as docker container with the following command in your command window:
````shell
docker-compose up -d
````
<b>This step is obligatory for raspberry pi. On local machine local mongo database could be used.</b><br>
<b>Be aware that lunching this command you must be located in the application folder `SmartWeather`</b>
6. When finished, run the application with your IDE or with command window using maven and the following command:
```shell
mvn spring-boot:run
```
## Raspberry Pi configuration
The Project was made on the Raspberry Pi 3 device and needs more like the same configuration as local machine. Configuration will be for device connected with SSH. If using device with all setup just navigate to the proper folders and provide configuration with command window<br>
1. Connect to your Raspberry Pi by SSH.
Check if Pi is available and responding.
   Then connect by SSH
``` shell
ping raspberrypi.local
ssh pi@raspberrypi.local
```
Using ssh: `pi` is your device username. It's also possible to connect with Raspberry ip
```shell
ssh pi@{YourIP}
```
2. Before starting, be sure you have docker-compose loaded for MongoDB
File docker-compose is located in the main app folder. It provides MongoDB database by image, so it's no need to have a database manually installed. If you installed on your raspberrypi docker and docker-compose run this command in the main app folder:
```shell
sudo docker-compose up -d
```
This will run the mongoDB container that provides you a database connection.
To check if everything is ready and mongoDB is running provide command:
```shell
sudo docker-compose ps
```
3. Start the application
Start the application with the maven command:
```shell
mvn spring-boot:run
```
## Important Notes
1. Application is cooperating with email templates provided by thymeleaf.
At the moment,
mail addresses like <b>Gmail</b> or <b>Outlook</b> are blocking thymeleaf templates due to an unknown source of an HTML file.
If you are interested in daily reports sending by email, consider setting up weather-email other than mentioned above.
In the future release this inconvenience will try to be fixed
2. <b>Gmail users</b> if you are using gmail for sending weather reports `sender-mail` be aware not to use your personal password.
Instead of using personal password, generate your application 16-characters password in the security section

## Documentation
The API is fully documented using **Swagger**.
You can access the documentation at the following endpoint: <br>
`http://localhost:8080/api-doc`<br>
The Swagger UI provides a detailed overview of all available endpoints including:
- Request methods and paths
- Required parameters
- Example request bodies
- Example response
- Fields examples / description

This makes it easy to explore, test, and integrate with the API directly from your browser.<br>
Here is a quick preview of the Swagger UI:
### Preview:
![Image](https://github.com/user-attachments/assets/81fafe2c-a5f3-45b4-b543-a4d086a1e876)

## Usage
- The Application is configured to work as a server on the WI-FI. You could connect to the project website using the server IP (for example, raspberry).
Navigate to the main page located on `http://{Your_IP}:8080`
1. After visiting the main page, you will be able to see **Application not configured** status.
![Image](https://github.com/user-attachments/assets/4c4b3b3b-48c0-41ba-bbe3-5a39ee1f88ef)
2. Please navigate to the config tab located in the top and then follow the instructions.
![Image](https://github.com/user-attachments/assets/87077765-3405-4207-ab21-02ecdad087fa)
3. The system asks you to provide your root password for all application configurations. After finished, click **"lock"** icons to provide all necessary information. 
For proper working, all fields should be filled
![Image](https://github.com/user-attachments/assets/3af93f72-9e4f-4a18-8e15-7ad6f8ad3d67)
4. Your `API-KEY` can be generated for free for non-commercial use from `WeatherApi.com`. It is obligatory to get the api key to set up application correctly

### Daily Reports:
1. If you are interested not only in the weather page but also in daily email reporting add user's email as `subscribers` on the user panel next to the configuration.
2. Provided mails would be stored in a database. Application will send daily weather forecast every day as per scheduler.
3. The Base schedule is set to generate reports every day at 08:00 in the morning. If you like to change basic setting, follow the steps below:
   - Navigate to the main app folder and find `scheduler` folder. Then open file `DailyForecastScheduler.java`. On your raspberry use a command window and then `nano DailyForecastScheduler.java`
   - At the top you will see `@Scheduled(cron = "0 0 8 * * *")`. Modify cron as per requirements according to the cron expression. For easy change of hour modify values **second-minute-hour**

   
Example of the report:<br>
### Current weather:
![Image](https://github.com/user-attachments/assets/c65f9896-cf78-4282-b53e-4f42f5e38d4f)
### Weather chart:
![Image](https://github.com/user-attachments/assets/ae06bf09-182f-4317-b66c-d838b4237d6c)
### Weather table:
![Image](https://github.com/user-attachments/assets/7405354f-cd81-431c-9209-58ad43d9139d)

## Technologies

<b>Java:</b> The primary programming language for building backend logic

<b>Spring Boot:</b> A framework used to develop the RESTful application that handles intereacting with the weather data API etc

<b>Maven:</b> Used for dependency management and project build automation

<b>Docker:</b> Used for testing, making it easier to isolate the application and making integrating testing

<b>Docker Compose:</b> Utilized to set up and manage the MongoDB database on Raspberry Pi, allowing database integration

<b>MongoDB:</b> A NoSQL databased used to store a weather report. It Will be also used for statistics in the future

<b>HTML:</b> The primary language for building websites and thymeleaf schemas

<b>JavaScript:</b> Programming language for frontend scripts providing better UX

<b>Bootstrap:</b> CSS framework used to build more user-friendly website

## Testing

To run tests, run the following command. Be aware that all integration tests would be run and docker should be running.

```bash
  mvn clean test
```
The project uses checkstyle validation, so if there are any checkstyle errors, the application will not run. All errors would be provided in the checkstyle-result file.<br>
Project also provided Jacoco test coverage reports. If you are an interested run:
```bash
mvn jacoco:report
```
Features used in testing:
- WireMock for testing external API 
- Docker (Test containers) integration test for database operations
- MockMVC for controller testing and exception handling coverage
- Unit tests for testing features independently<br>

<br>

At the moment coverage is almost 100% as the following report:
![Image](https://github.com/user-attachments/assets/9aede461-6364-47eb-88f4-b4d7bcd02218)

## Roadmap
- Improve UX
- Improve email templates (try to prevent Google from blocking images)
- Optimize backend structure to be more flexible and vulnerable for future functions
