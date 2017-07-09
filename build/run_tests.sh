#!/usr/bin/env bash
if [ "${TRAVIS_SCALA_VERSION}" == "2.12.1" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
then
    echo "Running tests with coverage and report submission"
    sbt ";++$TRAVIS_SCALA_VERSION testsWithCoverage; project readme; tut"
    exit $?
else
    echo "Running tests without attempting to submit coverage reports or run tut"
    sbt "++$TRAVIS_SCALA_VERSION test"
    exit $?
fi
q
