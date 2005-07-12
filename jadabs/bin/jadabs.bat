
set XARGS=init.xargs

rem Test with j2sdk-1.5.0, local repository
rem D:\TestCD\java\j2sdk-1.5.0\bin\java -Dorg.knopflerfish.gosg.jars=file:"C:\\Documents and Settings\\andfrei.IKNLAB8\\.maven\\repository\\" -Dch.ethz.jadabs.jxme.peeralias=%COMPUTERNAME% -jar framework-aop-1.3.3.jar -xargs %XARGS%

rem default startup from remote repository
java -Dorg.knopflerfish.gosg.jars=file:./repository/ -Dch.ethz.jadabs.jxme.peeralias=%COMPUTERNAME% -jar framework-aop-1.3.3.jar -xargs %XARGS%