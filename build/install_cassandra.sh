#!/usr/bin/env bash

pip install --user 'urllib3'
pip install --user 'requests'
pip install --user 'pyOpenSSL'
pip install --user 'ndg-httpsclient'
pip install --user 'pyasn1'
pip install --user ccm
ccm create test -v "3.10" -n 1 -s
