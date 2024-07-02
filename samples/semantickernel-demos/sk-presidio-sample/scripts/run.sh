#!/bin/bash

# It takes some time for presidio to start, wait for it
sleep 10

set -a
. /run/secrets/ai-config
set +a

java -jar sk-presidio-sample.jar