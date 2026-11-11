#!/bin/bash

# It takes some time for database to start, wait for it
sleep 10

set -a
. /run/secrets/ai-config
set +a

java -jar sk-database-sample.jar