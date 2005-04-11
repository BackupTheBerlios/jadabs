@echo off

set XARGS=init

if not "%1" == "" set XARGS=%1

java -Dorg.knopflerfish.gosg.jars="file:%MAVEN_HOME%\.maven\\repository\\" -jar "%MAVEN_HOME%\.maven\repository\osgi\jars\framework-aop-1.3.3.jar" -xargs %XARGS%.xargs
