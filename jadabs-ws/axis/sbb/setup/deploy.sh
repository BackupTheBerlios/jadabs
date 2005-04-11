#!/bin/sh

export oldDir=.
cd /home/jadabsws/tomcat/sbb/setup/

echo
echo Setting environment variables...
export JAVA_HOME=/usr/local/java/j2sdk1.4.2_06
export CATALINA_HOME=/home/jadabsws/tomcat/jakarta-tomcat-5.5.4
export AXIS_HOME=$CATALINA_HOME/webapps/axis
export AXIS_LIB=$AXIS_HOME/lib
export AXISCLASSPATH=$AXIS_LIB/axis.jar:$AXIS_LIB/commons-discovery.jar:$AXIS_LIB/commons-logging.jar:$AXIS_LIB/jaxrpc.jar:$AXIS_LIB/saaj.jar:$AXIS_LIB/log4j-1.2.8.jar:$AXIS_LIB/xml-apis.jar:$AXIS_LIB/xercesImpl.jar
export CLASSPATH=../src:$AXISCLASSPATH
echo Done.

echo
javac -verbose -d $AXIS_HOME/WEB-INF/classes/ ../src/ch/ethz/jadabs/webservices/sbb/SBBWebService.java
echo
java -cp $AXISCLASSPATH org.apache.axis.client.AdminClient -lhttp://localhost:8080/axis/services/AdminService deploy.wsdd
echo

cd $oldDir