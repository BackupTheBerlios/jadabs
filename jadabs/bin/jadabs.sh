#!/bin/bash

XARGS=init.xargs
XARGS_PROSE=init-prose.xargs

PWD=$(pwd)
PROSE_DIR=prose-1.2.1
NAME=$HOSTNAME
FRAMEWORK=framework-aop-1.3.3.jar

export LD_LIBRARY_PATH=$PWD/$PROSE_DIR/lib/i386

if [ -z $1 ]; then
	java -Dorg.knopflerfish.gosg.jars=file:repository/ -Dch.ethz.jadabs.jxme.peeralias=$NAME -jar $FRAMEWORK -xargs $XARGS
else
	java -Xdebug -Xnoclassgc -Xbootclasspath/a:$PROSE_DIR/lib/jdk-jvmai-loc.jar -Dprose.library.path=$PWD/$PROSE_DIR/lib/i386 -Xrunprosevm -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -Dch.ethz.inf.project.home=$PWD/$PROSE_DIR -Dprose.port:5000 -Dorg.knopflerfish.gosg.jars=file:repository/ -Dch.ethz.jadabs.jxme.peeralias=$HOSTNAME -cp $PROSE_DIR/lib/jdk-prose-loc.jar:$FRAMEWORK org.knopflerfish.framework.Main -xargs $XARGS_PROSE
fi
