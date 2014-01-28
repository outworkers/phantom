#!/bin/bash

sbt "project phantom-util" "assembly"
nohup java -jar ${PWD}/cassandra.jar &