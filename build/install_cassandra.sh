#!/usr/bin/env bash

python -V
pip install --user 'pyOpenSSL'
pip install --user 'ndg-httpsclient'

pip install --user 'pyasn1'
pip install --user 'urllib3'
pip install --user 'requests[security]'
pip install --user ccm

ccm create test -v "3.11" -n 1 -s timeout 60
ccm node1 showlog

if [ -e "/var/log/cassandra/" ]; then
    cat /var/log/cassandra/system.log
else
   echo "No lib directory found"
fi

