#!/bin/bash

sbt "project phantom-cassandra-unit" "assembly"
nohup java -jar ${PWD}/cassandra.jar &
