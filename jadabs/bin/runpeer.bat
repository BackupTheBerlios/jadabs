@echo off

set XARGS=init.xargs

if not "%1" == "" set XARGS=%1

java -Dch.ethz.jadabs.jxme.peeralias=%COMPUTERNAME% -jar framework-1.3.0-aop.jar -xargs %XARGS%