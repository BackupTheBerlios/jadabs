#!/bin/bash

java -Dorg.knopflerfish.gosg.jars=file:$HOME/.maven/repository/ -jar $HOME/.maven/repository/osgi/jars/framework-1.3.0.jar -xargs init-linux.xargs