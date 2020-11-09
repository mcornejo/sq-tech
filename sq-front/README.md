## sq-front
Node.js App to showcase an application Sqreened. It is a **very** simple application that only responds `Hello world!` at the `GET /` endpoint.
It also has a debug endpoint `POST /callback` that logs the request and responds with a `200` status. This app is basic example of using
the sqreen module installed in the index.js `require('sqreen')` first line.

## Build
```bash
npm install
```

Docker image
```bash
docker build -t murdix/sq-front .
```

## Run locally
```bash
npm run start
```
