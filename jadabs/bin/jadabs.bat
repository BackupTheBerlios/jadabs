@echo off

set XARGS=init.xargs

if not "%1" == "" set XARGS=%1

java -Dch.ethz.jadabs.jxme.peeralias=%COMPUTERNAME% -jar framework-aop-1.3.3.jar -xargs %XARGS%