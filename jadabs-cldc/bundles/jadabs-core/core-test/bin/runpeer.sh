#!/bin/bash

XARGS=init.xargs

PWD=$(pwd)

if [ -n "$1" ]; then XARGS=$1; fi

if [ -z $2 ]; then
	java -Dorg.knopflerfish.gosg.jars=file:$HOME/.maven/repository/ -Dch.ethz.jadabs.jxme.peeralias=peer1 -jar $HOME/.maven/repository/osgi/jars/framework-1.3.0.jar -xargs $XARGS
else
	java -Xdebug -Xnoclassgc -Xbootclasspath/a:$PROSE_DIR/lib/jdk-jvmai-loc.jar -Dprose.library.path=$PWD/$PROSE_DIR/lib/i386 -Xrunprosevm -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -Dch.ethz.inf.project.home=$PWD/$PROSE_DIR -Dprose.port:5000 -Dch.ethz.jadabs.jxme.peeralias=$HOSTNAME -cp $PROSE_DIR/lib/jdk-prose-loc.jar:framework-aop-1.3.3.jar org.knopflerfish.framework.Main -xargs $XARGS
fi
