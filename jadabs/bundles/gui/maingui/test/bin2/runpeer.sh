#!/bin/bash

java -Dch.ethz.jadabs.jxme.peeralias=peer1 -Dorg.knopflerfish.gosg.jars=file:$HOME/.maven/repository/ -jar $HOME/.maven/repository/osgi/jars/framework-1.3.3.jar -xargs init.xargs
