@echo off

set XARGS=init-win

if not "%1" == "" set XARGS=%1

java -Dorg.knopflerfish.gosg.jars="file:%HOMEDRIVE%%HOMEPATH%\.maven\\repository\\" -jar "%HOMEDRIVE%%HOMEPATH%\.maven\repository\osgi\jars\framework-1.3.0-aop.jar" -xargs %XARGS%.xargs
