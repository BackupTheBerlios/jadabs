@echo off

set XARGS=init

if not "%1" == "" set XARGS=%1

java -Dorg.knopflerfish.gosg.jars="file:%HOMEDRIVE%%HOMEPATH%\.maven\\repository\\" -jar "%HOMEDRIVE%%HOMEPATH%\.maven\repository\osgi\jars\framework-1.3.3-aop.jar" -xargs %XARGS%.xargs
