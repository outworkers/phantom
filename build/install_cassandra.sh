#!/usr/bin/env bash
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

if [ "$jdk_version_8_or_more" > 1 ];
  then
    cassandra_version="3.7"
  else
    cassandra_version="2.2.9"
fi

pip install --user 'requests[security]'
pip install --user ccm
ccm create test -v ${cassandra_version} -n 1 -s
exit $?