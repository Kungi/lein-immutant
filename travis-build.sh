#!/bin/bash

curl -f -L https://raw.github.com/technomancy/leiningen/2.4.2/bin/lein > ./lein
chmod +x ./lein
./lein version
export LEIN2_CMD=`pwd`/lein
export PRINT_OUT=1
./lein midje
