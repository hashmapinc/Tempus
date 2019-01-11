#!/bin/sh
# Path needs to be mounted from host to container.
UPDATE_PATH=/srv/test/test.maccyber.io/

cd ${UPDATE_PATH} && docker-compose pull && docker-compose up -d
