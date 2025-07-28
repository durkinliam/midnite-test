# Midnite Take Home Test - Liam Durkin
This repository contains the code for the Midnite Take Home Test, which is a Spring Boot application written in Kotlin. 
The application currently has a single POST endpoint `/event` that accepts a JSON payload and returns a response based on the input.

## Assumptions/Future Improvements
This is a MVP application, therefore the local storage is not persistent and will be lost when the application is restarted. The way that the cache is organised, it is to allow further extension of capabilities, perhaps to retrieve data for a given user over a set period and see how frequently they're causing certain alerts i.e `user_id=123` has caused an alert code of 123 5 times in the last 7 days. This could easily be implemented by adding a new GET endpoint within the controller class.

The project is structured in a way that allows for easy extension in the future, should additional features be required, but also to aid the easy replacement of the in-memory storage with a more robust solution such as a database.

## API Schema
The accepted JSON payload for the `/event` endpoint is as follows:

```json
{
  "type": "string",
  "amount": "string",
  "user_id": number,
  "timestamp": number
}
```

The rules for the request are as follows:
- `type`: A string that can only be either `deposit` or `withdraw`.
- `user_id`: A number that represents a unique ID of the user.
- `t`: A number that represents the timestamp of the event in milliseconds. This should always be increasing and unique per user.

The server will respond with a JSON object, though the response will be dependent upon whether it's deemed successful or unsuccessful.

Successful responses will return a 200 status code and the following JSON object:

```json
{
  "alert": boolean,
  "alert_codes": [number],
  "user_id": number
}
```

The rules for the response are as follows:
- `alert`: This will only be `true` if there's at least one `alert_code` present in the response.
- `alert_codes`: An array of numbers that represent the alert codes. By nature of the Array, multiple alerts may be present in a single response. The possible alert codes are:
  - `1100`: If the user withdraws more than 100 in a single transaction.
  - `30`: If the user has 3 consecutive withdrawals.
  - `300`: If the user has 3 consecutive increasing deposits (ignoring withdrawals).
  - `123`: If the user has an accumulative deposit amount of more than 200 in a 30-second window.
- `user_id`: The ID of the user that the event belongs to.

Unsuccessful responses will return a 400 or a 500 status code, depending on the nature of the error. The response will contain a JSON object with a `reason` field that describes the error:
```json
{
  "reason": "string"
}
```


## Running The Application Locally
To run the application locally, you need to have Java 21 installed on your machine along with Gradle 8.0.
You can run the application using the following command:

```
  ./gradlew bootRun
```

## Running The Application via Docker Desktop
Alternatively, if you prefer not to install Java and Gradle onto your machine, you can run the application using Docker Desktop. Though, please ensure you have Docker installed and running on your machine.
You can run the application using the following command:

```
  docker-compose up
```

## Tests
In order to run the tests, you can run the following command: 
```
  ./gradlew clean test
```
