#!/bin/bash

PWD=$(pwd)
PROSE_DIR=prose-1.2.1

export LD_LIBRARY_PATH=$PWD/$PROSE_DIR/lib/i386

java -Xdebug -Xnoclassgc -Xbootclasspath/a:$PROSE_DIR/lib/jdk-jvmai-loc.jar -Dprose.library.path=$PWD/$PROSE_DIR/lib/i386 -Xrunprosevm -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -Dch.ethz.inf.project.home=$PWD/$PROSE_DIR -Dprose.port:5000 -Djxme.peername=peer1 -cp $PROSE_DIR/lib/jdk-prose-loc.jar:framework-1.3.0-aop.jar org.knopflerfish.framework.Main -xargs prose.xargs