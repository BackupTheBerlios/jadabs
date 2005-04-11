@echo off

echo.
echo Setting Environment Variables...

set CATALINA_HOME=D:\tomcat
set AXIS_HOME=%CATALINA_HOME%\webapps\axis
set AXIS_LIB=%AXIS_HOME%\lib
set AXISCLASSPATH=%AXIS_LIB%\axis.jar;%AXIS_LIB%\commons-discovery.jar;%AXIS_LIB%\commons-logging.jar;%AXIS_LIB%\jaxrpc.jar;%AXIS_LIB%\saaj.jar;%AXIS_LIB%\log4j-1.2.8.jar;%AXIS_LIB%\xml-apis.jar;%AXIS_LIB%\xercesImpl.jar
set CLASSPATH=.;%AXISCLASSPATH%

set JADABS=D:\jadabs-0.6.8-minimal
set XARGS="%CD%\init-helloWorld.xargs"

echo Done.
echo.

java -cp "%AXISCLASSPATH%" org.apache.axis.client.AdminClient -lhttp://localhost:8080/axis/services/AdminService undeploy.wsdd