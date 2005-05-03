@echo off

set XARGS=init.xargs

if not "%1" == "" set XARGS=%1

java -Dorg.knopflerfish.gosg.jars=file:./repository/ -Dch.ethz.jadabs.jxme.peeralias=%COMPUTERNAME% -jar framework-aop-1.3.3.jar -xargs %XARGS%