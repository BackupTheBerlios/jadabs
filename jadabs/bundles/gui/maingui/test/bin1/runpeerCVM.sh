#!/bin/bash

/home/andfrei/opt/j2me-cdc-1.0.1-i686/bin/cvm -Dch.ethz.jadabs.jxme.peeralias=peer1 -Dorg.knopflerfish.gosg.jars=file:$HOME/.maven/repository/ -Djava.class.path=./:$HOME/.maven/repository/osgi/jars/framework-1.3.3.jar org.knopflerfish.framework.Main -xargs init.xargs
