#!/bin/bash
# Usage: ./scripts/run_tests.sh

sbt "project phantom-dsl" "test"
sbt "project phantom-scalatra-test" "test"
sbt "project phantom-thrift" "test"
