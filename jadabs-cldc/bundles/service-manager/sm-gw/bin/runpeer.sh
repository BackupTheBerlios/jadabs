#!/bin/bash

XARGS=init.xargs

if [ -n "$1" ]; then XARGS=$1; fi

java -Dch.ethz.jadabs.jxme.peeralias=btgw -Dorg.knopflerfish.gosg.jars=file:$HOME/.maven/repository/ -jar $HOME/.maven/repository/osgi/jars/framework-1.3.3.jar -xargs $XARGS
