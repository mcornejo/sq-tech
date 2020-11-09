#!/bin/sh
set -e

HOST="https://webhook.murdix.com"

echo 'Adding a debug logger'
curl --location --request POST "$HOST/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "log", "name": "myLoggerDebug", "level": "debug"}' 

echo 'Adding an info logger'
curl --location --request POST "$HOST/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "log", "name": "myLoggerInfo", "level": "info"}' 

echo 'Adding an API (POST) call'
curl --location --request POST "$HOST/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "http", "url": "https://sqreen.murdix.com/callback", "method": "post", "shared_key": "key"}'

echo 'Adding an Slack message'
curl --location --request POST "$HOST/target" \
--header 'Content-Type: application/json' \
--data-raw '{"type": "slack", "url": "https://hooks.slack.com/services/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"}'

echo 'Simulating a HMACd message from Sqreen'
curl --location --request POST "$HOST" \
--header 'X-Sqreen-Integrity: 2069da86a9c3067d10eb8d2d14aea1cc48b9db624dba7f2264ef2f32d450a22e' \
--header 'Content-Type: application/json' \
--data-raw '[{"message_id": null, "api_version": "2", "date_created": "2020-11-06T13:55:09.503282+00:00", "message_type": "security_event", "retry_count": 0, "message": {"risk_coefficient": 25, "event_category": "http_error", "event_kind": "waf", "application_id": "5fa41cbdb87595001cb7fd03", "application_name": "my-express-app", "environment": "development", "date_occurred": "2020-11-06T13:35:47.487000+00:00", "event_id": "5fa551335922d9000f88ecbf", "event_url": "https://my.sqreen.com/application/5fa41cbdb87595001cb7fd03/events/5fa551335922d9000f88ecbf", "humanized_description": "Attack tentative from 127.0.0.1", "ips": [{"address": "127.0.0.1", "is_tor": false, "geo": {}, "date_resolved": "2020-11-06T13:35:47.514000+00:00"}]}}]'

echo 'Reviewing the logs'
curl "$HOST/logs"

echo 'Cleaning'
curl "$HOST/clean"
