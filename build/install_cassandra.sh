#!/usr/bin/env bash

CASSANDRA_VERSION="3.10"
DEFAULT_CASSANDRA_VERSION="2.2.9"

function check_java_version {
  if type -p java; then
    echo found java executable in PATH
    _java=java
  elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo found java executable in JAVA_HOME
    _java="$JAVA_HOME/bin/java"
  else
    echo "no java"
    return -1
  fi

  if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo version "$version"
    if [[ ("$version" > "1.8") || ("$version" = "1.8") ]]; then
      echo "version is more than 1.8"
      return 1
    else
      echo "version is less than 1.8"
      return -1
    fi
  fi
}

jdk_version_8_or_more=$(check_java_version)

if [ ${jdk_version_8_or_more} -gt 1 ];
  then
    cassandra_version="$CASSANDRA_VERSION"
  else
    cassandra_version="$DEFAULT_CASSANDRA_VERSION"
fi

pip install --user 'requests[security]'
pip install --user ccm
ccm create test -v ${CASSANDRA_VERSION} -n 1 -s
