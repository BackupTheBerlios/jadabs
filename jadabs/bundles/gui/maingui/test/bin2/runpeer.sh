#!/bin/bash

java -Dch.ethz.jadabs.jxme.peeralias=peer2 -Dorg.knopflerfish.gosg.jars=file:$HOME/.maven/repository/ -cp . -jar $HOME/.maven/repository/osgi/jars/framework-1.3.3.jar -xargs init.xargs
