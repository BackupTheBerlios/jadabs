#!/bin/sh -x
#
# Copyright 2003 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

# You should have the following variables set:
#
#JAVA_HOME=<path to J2SDK installation root>
#TOMCAT_HOME=<path to jwsdp-1.2 installation root>

PATH=$TOMCAT_HOME/bin:$TOMCAT_HOME/jaxrpc/bin:$JAVA_HOME/bin:$PATH
export PATH TOMCAT_HOME JAVA_HOME

if [ "$TOMCAT_HOME" = "" ]
then
  echo TOMCAT_HOME variable is not set
  echo TOMCAT_HOME should point to JWSDP-1.2 installation
  echo You can download it from:
  echo http://java.sun.com/webservices/downloads/webservicespack.html
  exit
fi

if [ ! -x $TOMCAT_HOME/jaxrpc/bin/wscompile.sh ]
then
  echo TOMCAT_HOME should point to JWSDP-1.2 installation
  echo You can download it from:
  echo http://java.sun.com/webservices/downloads/webservicespack.html
  exit
fi

if [ "$JAVA_HOME" = "" ]
then
  echo JAVA_HOME variable is not set
  echo JAVA_HOME should point to J2SDK installation
  exit
fi

if [ ! -x $JAVA_HOME/bin/javac ]
then
  echo JAVA_HOME should point to J2SDK installation
  exit
fi
mkdir -p WEB-INF/classes
echo running javac
javac -d WEB-INF/classes src/serverscript/* -classpath \
$TOMCAT_HOME/jaxrpc/lib/jaxrpc-api.jar:$TOMCAT_HOME/common/lib/servlet-api.jar || exit

mkdir tmp_src
echo running wscompile.sh
wscompile.sh -gen:server -d WEB-INF/classes -keep -model WEB-INF/model.gz \
        -s tmp_src/ -f:wsi,documentliteral -classpath WEB-INF/classes \
	src/config.xml || exit

cp src/web.xml src/jaxrpc-ri.xml WEB-INF/
echo running jar
jar -cf serverscript.jar WEB-INF/
echo running wsdeploy.sh
wsdeploy.sh serverscript.jar -o serverscript.war




