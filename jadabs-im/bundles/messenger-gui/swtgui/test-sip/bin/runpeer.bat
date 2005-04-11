@echo off

set XARGS=init-win


if not "%1" == "" set XARGS=%1

java -Dorg.knopflerfish.gosg.jars="file:c:\documents and settings\franz terrier\.maven\\repository\\" -jar "c:\documents and settings\franz terrier\.maven\repository\osgi\jars\framework-aop-1.3.3.jar" -xargs %XARGS%.xargs
