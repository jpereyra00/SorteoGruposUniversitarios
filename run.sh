#!/usr/bin/env bash
set -e
./mvnw spring-boot:run 2>/dev/null || mvn spring-boot:run
