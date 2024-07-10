#!/bin/bash

set -eux

./mvnw package

docker-compose build && docker-compose up