#!/bin/bash
# Usage: ./scripts/run_tests.sh

sbt "update" "compile" "project phantom" "coveralls"
