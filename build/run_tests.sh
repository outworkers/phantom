#!/usr/bin/env bash
function run_test_suite {
    if [ "${TRAVIS_SCALA_VERSION}" == "${TARGET_SCALA_VERSION}" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
    then
        echo "Running tests with coverage and report submission"
        sbt ++$TRAVIS_SCALA_VERSION coverage test coverageReport coverageAggregate coveralls
        testExitCode=$?

        if [ ${testExitCode} == "0" ];
        then
            echo "Running tut compilation"
            sbt ++$TRAVIS_SCALA_VERSION "project readme" "tut"
            exit $?
        else
            echo "Unable to run tut compilation, test suite failed"
            exit ${testExitCode}
        fi
    else
        echo "Running tests without attempting to submit coverage reports or run tut"
        sbt "plz $TRAVIS_SCALA_VERSION test"
        exit $?
    fi
}

run_test_suite