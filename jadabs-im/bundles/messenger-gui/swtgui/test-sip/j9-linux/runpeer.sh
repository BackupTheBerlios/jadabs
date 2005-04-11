#!/bin/bash

XARGS=init-linux.xargs

if [ -n "$1" ]; then XARGS=$1; fi

/home/andfrei/opt/j9-cdc/bin/j9 -Dorg.knopflerfish.gosg.jars=file:$HOME/.maven/repository/ -classpath $HOME/.maven/repository/osgi/jars/framework-aop-1.3.3.jar org.knopflerfish.framework.Main -xargs $XARGS