#!/usr/bin/env bash
function run_test_suite {
    if [ "${TRAVIS_SCALA_VERSION}" == "${TARGET_SCALA_VERSION}" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
    then
        echo "Running tests with coverage and report submission"
        sbt ++$TRAVIS_SCALA_VERSION coverage test coverageReport coverageAggregate coveralls
        test_exit_code=$?

        if [ ${test_exit_code} -eq "0" ];
        then
            echo "Running tut compilation"
            sbt ++$TRAVIS_SCALA_VERSION "project readme" "tut"
            local tut_exit_code=$?
            echo "Tut compilation exited with status $tut_exit_code"
            exit ${tut_exit_code}
        else
            echo "Unable to run tut compilation, test suite failed"
            exit ${test_exit_code}
        fi
    else
        echo "Running tests without attempting to submit coverage reports or run tut"
        sbt "plz $TRAVIS_SCALA_VERSION test"
        exit $?
    fi
}

run_test_suite