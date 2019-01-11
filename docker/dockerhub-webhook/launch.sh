#!/bin/bash

docker run -d \
  --restart always \
  -p 3000:3000 \
  -e SERVER_PORT=3000 \
  -e TOKEN=$TOKEN \
  -e ROUTE=/api \
  -v ${PWD}/scripts:/src/scripts \
  -v /usr/bin/kubectl:/usr/bin/kubectl \
  -v /var/log/webhook_log:/src/webhook_log \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name dockerhub-webhook \
  maccyber/dockerhub-webhook 
