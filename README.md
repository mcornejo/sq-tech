# sq-tech
SQ-Tech is a (double) webapp to showcase an application Sqreened and the usage of a webhook. It contains two modules (a backend and a frontend):
- sq-front: Is a mini Node.js app using Express to showcase how to use Sqreen. It contains two endpoints, the root `/` (GET) and `/callback` (POST)
- sq-back: A webapp that receives a webhook from Sqreen.io and forwards it to different configurable targets via API. It contains multiple endpoints 
for receiving the webhook and configure the targets.

The idea of thsee applications is to receive a webhook from Sqreen.io and dispatch it to differents sources (backend) when our main application is being attacked, in this case our frontend. That is why we installed Sqreen only in the sq-front app.

Sq-back, currently can forward the webhook to 4 different targets: Email, Http query, Logging and Slack Message. While all the targets are available, we will only showcase two of them: HTTP and Logging. In order to send an Email a SMTP server is needed (not configured). Any of these targets can have as many as entries to forward.

## Usage 
The easiest way is to test the whole app is loading two targets: Http query and a logging. In this example we will forward the webhook to three 
targets: one as HTTP (POST) and two as Logging using different levels: one INFO and one DEBUG. 

For details on the deployment of each app and details on the APIs, cf. the [README](https://github.com/mcornejo/sq-tech/tree/main/sq-back) of sq-back 
and the [README](https://github.com/mcornejo/sq-tech/tree/main/sq-front) of sq-front.

Append a new Log Target with level debug:
```bash
curl --location --request POST "localhost:9000/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "log", "name": "myLoggerDebug", "level": "debug"}'
```

Append a new Log Target with level Info:
```bash
curl --location --request POST "localhost:9000/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "log", "name": "myLoggerInfo", "level": "info"}'
```

Append a new HTTP (POST):
```bash
curl --location --request POST "localhost:9000/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "http", "url": "http://localhost:8080/callback", "method": "post", "shared_key": "key"}'
```

Append a new Slack Message:
```bash
curl --location --request POST "localhost:9000/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "slack", "url": "https://hooks.slack.com/services/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"}'
```

Simulating a webhook from Sqreen.io: This message will trigger the forward of all the targets appended below, in this example, it will forward that message to a log in debug and info, it will re-posted `HMAC'd` to `localhost:8080/callback` and it be will posted as a slack message.
```bash
curl --location --request POST "localhost:9000/target" \
--header 'X-Sqreen-Integrity: 2069da86a9c3067d10eb8d2d14aea1cc48b9db624dba7f2264ef2f32d450a22e' \
--header 'Content-Type: application/json' \
--data-raw '[{"message_id": null, "api_version": "2", "date_created": "2020-11-06T13:55:09.503282+00:00", "message_type": "security_event", "retry_count": 0, "message": {"risk_coefficient": 25, "event_category": "http_error", "event_kind": "waf", "application_id": "5fa41cbdb87595001cb7fd03", "application_name": "my-express-app", "environment": "development", "date_occurred": "2020-11-06T13:35:47.487000+00:00", "event_id": "5fa551335922d9000f88ecbf", "event_url": "https://my.sqreen.com/application/5fa41cbdb87595001cb7fd03/events/5fa551335922d9000f88ecbf", "humanized_description": "Attack tentative from 127.0.0.1", "ips": [{"address": "127.0.0.1", "is_tor": false, "geo": {}, "date_resolved": "2020-11-06T13:35:47.514000+00:00"}]}}]'
```

There are two auxiliary API endpoints to make the showcase more interesting:
the `/logs` endpoint prints the content of the logs in the sq-back application.
```bash
curl "localhost:9000/logs"
```

The `/clean` endpoint remove all appended targets for a refresh start.
```bash
curl "localhost:9000/clean"
```

## Live Demo
Both applications are currently running a staging server with TLS activated.
The backend is hosted in `https://webhook.murdix.com` and the frontend is hosted in `https://sqreen.murdix.com`.

We can test the app currenly running in a server. first we will append some targets, 
```bash
curl --location --request POST "https://webhook.murdix.com/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "log", "name": "myLoggerInfo", "level": "info"}' 
```

Then we will attack our front:
```bash
curl -A Arachni/v1.2.x https://sqreen.murdix.com/
```

Finally we can see the logs of the app as soon as the webhook is received (it can take some minutes):
```bash
curl "https://webhook.murdix.com/logs"
```

## Automated tests
A small test suite is provided in the `test.sh` script. This script is very similar to the commands herein presented. It adds four targets: two Log, 
one HTTP and one Slack message. Then it simulates a webhook message from Sqreen.io and it shows in the screen the content of the logs in the server. 
Finally it cleans everything for a fresh start (except the logs, logs are persisted for the live of the container in the server). 

## Requirements
### sq-Front
- [Node.js](https://nodejs.org/): JavaScript runtime to build the project
- [npm](https://www.npmjs.com/): Node package manager to install dependencies.
- [Docker](https://www.docker.com/): Package software into standardized units for development, shipment and deployment.

### sq-back
- [JDK 11](https://adoptopenjdk.net/): The Java runtime to execute the code.
- [SBT](https://www.scala-sbt.org/download.html): The interactive build tool to compile and execute the project.
- [Docker](https://www.docker.com/): Package software into standardized units for development, shipment and deployment.

## Deployment
The deployment is done via Docker. Both webapps are dockerised and everything can be automatised using the `create_images.sh` script.
This bash script builds the docker image for `murdix/sq-front` from `Node:15` and `murdix/sq-back` from `openjdk:13`. 
There is a `docker-compose.yml` that helps for the deployment on the live demo server.

***

All details of each webapp are contained in their own README.md.
