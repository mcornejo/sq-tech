#!/bin/sh
set -e

pwd=$(pwd)

echo 'Creating backend images'
cd $pwd/sq-back
sbt clean
sbt docker:stage
cd target/docker/stage
docker build -t murdix/sq-back:latest .

echo 'Creating frontend images'
cd $pwd/sq-front
npm install
docker build -t murdix/sq-front .
cd $pwd
echo 'Built two images'
