#!/usr/bin/env bash
if [ "${TRAVIS_SCALA_VERSION}" == "2.11.8" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
then
    echo "Running tests with coverage and report submission"
    sbt "; plz $TRAVIS_SCALA_VERSION coverage; plz $TRAVIS_SCALA_VERSION test; plz $TRAVIS_SCALA_VERSION coverageReport; plz $TRAVIS_SCALA_VERSION coverageAggregate; plz $TRAVIS_SCALA_VERSION coveralls"
    exit $?
else
    echo "Running tests without attempting to submit coverage reports"
    sbt "plz $TRAVIS_SCALA_VERSION test"
    exit $?
fi
q