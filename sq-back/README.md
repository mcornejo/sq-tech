## sq-back

sq-back is webapp that receives a POST request, and it forwards it to different configurable targets. 
Among the different features, it allows adding new targets as well delete all the targets just with an API call. 
We will present the current API:
## API

- [Main webhook](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/controllers/ApplicationController.scala#L30): `POST /` It handles the HTTP 
request from Sqreen.io. It verifies the HMAC matches the body of the request, and it forwards the body to the different 
targets configured. The requests are sent using the Sqreen format defined in their [documentation](https://docs.sqreen.com/integrations/webhooks/).

Example of a request
```bash
curl --location --request POST "localhost:9000" \
--header 'X-Sqreen-Integrity: 2069da86a9c3067d10eb8d2d14aea1cc48b9db624dba7f2264ef2f32d450a22e' \
--header 'Content-Type: application/json' \
--data-raw '[{"message_id": null, "api_version": "2", "date_created": "2020-11-06T13:55:09.503282+00:00", "message_type": "security_event", "retry_count": 0, "message": {"risk_coefficient": 25, "event_category": "http_error", "event_kind": "waf", "application_id": "5fa41cbdb87595001cb7fd03", "application_name": "my-express-app", "environment": "development", "date_occurred": "2020-11-06T13:35:47.487000+00:00", "event_id": "5fa551335922d9000f88ecbf", "event_url": "https://my.sqreen.com/application/5fa41cbdb87595001cb7fd03/events/5fa551335922d9000f88ecbf", "humanized_description": "Attack tentative from 127.0.0.1", "ips": [{"address": "127.0.0.1", "is_tor": false, "geo": {}, "date_resolved": "2020-11-06T13:35:47.514000+00:00"}]}}]'
```

- [Home](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/controllers/ApplicationController.scala#L74): `GET /` It does absolutely nothing, 
it is just not to have a error page when calling the `/`

Example of a request
```bash
curl "localhost:9000/"
```

- [Logs](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/controllers/ApplicationController.scala#L63): `GET /logs` It reads the logs of the 
application and print them in as the body of the response in text format.

Example of a request
```bash
curl "localhost:9000/logs"
```

- [Add target](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/controllers/TargetController.scala#L24): `POST /target` It adds a new target 
to forward the message received in the Main webhoook.

Examples of requests
```bash
curl --location --request POST "localhost:9000/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "log", "name": "myLoggerInfo", "level": "info"}' 

curl --location --request POST "localhost:9000/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "http", "url": "https://sqreen.murdix.com/callback", "method": "post", "shared_key": "key"}'

curl --location --request POST "localhost:9000/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "slack", "url": "https://hooks.slack.com/services/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"}'

```

- [List target](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/controllers/TargetController.scala#L26): `GET /target` It lits all current 
targets.

Example of a request
```bash
curl "localhost:9000/target"
```

- [Clean targets](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/controllers/TargetController.scala#L21): `GET /clean` It removes all 
targets added by Add target

Example of a request
```bash
curl "localhost:9000/clean"
```

## Target Request
To add a new request, a POST request must be done to the Add target endpoint defined before. Each target has it own payload:

### Log
A log target will output the message in the configured logger (`./logs/application.log`) and has three fields:
- `type`: The fixed string `log`
- `name`: The name of the logger. The name will be printed in the log file.
- `level`: The level of the logger. It can be one of the following: "error", "warn", "info", "debug"
Example
```json
{
    "type": "log", 
    "name": "mySuperLogger", 
    "level": "error"
}
```

### Email
An email target will forward the message to an email address. It has two fields: the type specifying the email target and the email of the recipient.
- `type`: The fixed string `email`
- `to`: The email address where to forward the message
Example:
```json
{
    "type": "email", 
    "name": "steve@apple.com"
}
```

### http
An HTTP target will send an http request to the address indicated in the payload. It has four fields:
- `type`: The fixed string `http`.
- `url`: The URL where to forward the request.
- `method`: It specifies the method how to send the request. It can be only `get` or `post`.
- `shared_key`: A secret shared key that will be used to calculate the HMAC of the message (only for method `post`). The HMAC will be sent in the 
header as `X-Integrity-Murdix` and it will allow the receiver to authenticate the message.
Example:
```json
{
    "type": "http", 
    "url": "https://sqreen.murdix.com/callback", 
    "method": "post", 
    "shared_key": "secret_key"
}
```

### Slack
A Slack message will send a Slack [Incoming Webhook](https://api.slack.com/messaging/webhooks) to a predefined channel in a Slack workspace. It has 
two fields:
- `type`: The fixed string `slack`.
- `url`: The URL of the webhook.
Example:
```json
{
    "type": "slack", 
    "url": "https://hooks.slack.com/services/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"
}
```

## Structure
The webapp is built on Scala using Play Framework as web framework. 
In the `app` folder it is possible to find all the code. Inside the folder `app` there is:

- `controllers`: It contains two controllers, 
[`ApplicationController.scala`](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/controllers/ApplicationController.scala) that handles the 
incoming webhook from Sqreen.io and 
[`TargetController.scala`](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/controllers/TargetController.scala) that handles the addition 
and removal of targets.

- `filters`: It contains the helper [`WebhookRequest.scala`](https://github.com/mcornejo/sq-tech/blob/main/sq-back/app/filters/WebhookRequest.scala) 
and it is used to transform the raw request into a WebhookRequest object and verify the message is authentic from Sqreen.io using the HMAC. If the 
HMAC of the request is invalid, the filter will respond with `Forbidden` and the message will not be pass to the controller.

- `services`: It contains every target logic on how to operate and perform the forwarding.

- `targets`: It contains data classes that capture the configuration of each target. 

- `utils`: A mini cryptographic library that calculates the HMAC using SHA-256 as hash function.

In the folder `test` there are the tests for the controllers and units tests.

## Build
As the project is written using Scala, the source code must be compiled in order to be executed by the JVM.
```bash
sbt compile
```

Run locally
As the project is written using Scala, the source code must be compiled in order to be executed by the JVM.
```bash
sbt run
```

Create docker image
```bash
sbt docker:stage
cd target/docker/stage
docker build -t murdix/sq-back:latest .
```

## Tests
We provided tests for the parser from textFile. To execute the tests, just type in a terminal:
```bash
$ sbt test
```

## Considerations
The current implementation does not persist the targets stored. It relies on an in-memory cache that is not persistent nor distributed. For a heavy 
load, the app can be easily distributed, but the cache needs to be replaced for a unique data source (like a database or distributed cache).
