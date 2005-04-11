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

if [ ! -f serverscript.war ]
then
  echo You should run build.sh first
fi

echo Coping serverscript.war to webapps
cp serverscript.war $TOMCAT_HOME/webapps
echo Stopping Tomcat
$TOMCAT_HOME/bin/catalina.sh stop
echo Starting Tomcat
$TOMCAT_HOME/bin/catalina.sh start
