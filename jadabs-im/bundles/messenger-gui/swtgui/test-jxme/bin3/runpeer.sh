#!/bin/bash

java -Dorg.knopflerfish.gosg.jars=file:$HOME/.maven/repository/ -jar $HOME/.maven/repository/osgi/jars/framework-aop-1.3.3.jar -xargs init-linux.xargs