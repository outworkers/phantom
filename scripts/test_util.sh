#!/bin/bash

sbt "project phantom-test" "assembly"
nohup java -jar ${PWD}/cassandra.jar &
