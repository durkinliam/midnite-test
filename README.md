# Midnite Take Home Test - Liam Durkin

This repository contains the code for the Midnite Take Home Test, which is a Spring Boot application written in Kotlin.
The application currently has a single **POST** endpoint `/event` that accepts a JSON payload and returns a response
based on the input.

## Assumptions, Acknowledgements and Future Improvements

This is a MVP application, therefore the local storage is not persistent and will be lost when the application is
restarted. The way that the cache is organised, it is to allow further extension of capabilities, perhaps to retrieve
data for a given user over a set period and see how frequently they're causing certain alerts i.e `user_id=123` has
caused an alert code of 123 5 times in the last 7 days. This could easily be implemented by adding a new GET endpoint
within the controller class.

Assuming this is an MVP, only a small number of users are expected to use the endpoint. For a production service at
Midnite with a larger customer base, the system would need redesigning. I would suggest using Kafka to produce new
events from a microservice upstream. An event microservice would listen for messages from the main raw topic, acting as
a conductor, producing downstream to another eventType-based topic, allowing respective microservices to listen for new
messages and process them accordingly. As already stated, a database would also be implemented to store the events
persistently, allowing for easy retrieval and analysis of user activity over time.

The project is structured in a way that allows for easy extension in the future, should additional features be required,
but also to aid the easy replacement of the in-memory storage with a more robust database solution.

## API Schema

### Request

JSON schema for making requests to the `/event` endpoint:

```json
{
  "type": "object",
  "properties": {
    "type": {
      "type": "string",
      "enum": [
        "deposit",
        "withdraw"
      ]
    },
    "amount": {
      "type": "string"
    },
    "user_id": {
      "type": "integer"
    },
    "t": {
      "type": "integer"
    }
  },
  "required": [
    "type",
    "amount",
    "user_id",
    "t"
  ]
}
```

#### Request Example

```json
{
  "type": "deposit",
  "amount": "42.00",
  "user_id": 1,
  "t": 10
}
```

### Response

The server will respond with a JSON object, though the response will be dependent upon whether it's deemed successful or
unsuccessful.

#### Successful Response

Successful responses will return a 200 status code and the following JSON object:

```json
{
  "type": "object",
  "properties": {
    "alert": {
      "type": "boolean"
    },
    "alert_codes": {
      "type": "array",
      "items": {
        "type": "integer",
        "enum": [
          1100,
          30,
          300,
          123
        ]
      }
    },
    "user_id": {
      "type": "integer"
    }
  },
  "required": [
    "alert",
    "alert_codes",
    "user_id"
  ]
}
```

#### Successful Response Example

```json
{
  "alert": true,
  "alert_codes": [
    1100,
    30
  ],
  "user_id": 1
}
```

The rules for the response are as follows:

- `alert`: This will only be `true` if there's at least one `alert_code` present in the response.
- `alert_codes`: An array of numbers that represent the alert codes. By nature of the Array, multiple alerts may be
  present in a single response. The possible alert codes are:
    - `1100`: If the user withdraws more than 100 in a single transaction.
    - `30`: If the user has 3 consecutive withdrawals.
    - `300`: If the user has 3 consecutive increasing deposits (ignoring withdrawals).
    - `123`: If the user has an accumulative deposit amount of more than 200 in a 30-second window.
- `user_id`: The ID of the user that the event belongs to.

#### Unsuccessful Response

Unsuccessful responses will return a 400 or a 500 status code, depending on the nature of the error. The response will
contain a JSON object with a `reason` field that describes the error:

```json
{
  "type": "object",
  "properties": {
    "reason": {
      "type": "string"
    }
  },
  "required": [
    "reason"
  ]
}
```

#### Unsuccessful Response Example

```json
{
  "reason": "Event request timestamp is earlier than the last event for userId: 1. All events must be increasing in timestamp order and unique per user"
}
```

## Running The Application

The application will start on port 1234, and you can access the endpoint at `http://localhost:1234/event`.

### Running The Application Locally

To run the application locally, you need to have Java 21+ installed on your machine along with Gradle 8.0+.
You can run the application using the following command:```./gradlew bootRun```

### Running The Application via Docker Desktop

Alternatively, if you prefer not to install Java and Gradle onto your machine, you can run the application using [Docker
Desktop](https://www.docker.com/products/docker-desktop/). Though, please ensure you have Docker installed and running
on your machine.
You can run the application using the following command:```./deploy.sh```

After you have finished, you should stop the remove the container in your Docker Desktop. This can be done after you
have already exited the application, or if the application is still running, and you wish to exit and stop in one
command. To do this run: ```./tear-down.sh```

## Tests

In order to run the tests, you can run the following command:```./gradlew clean test```
