
set XARGS=init.xargs
set NAME=%COMPUTERNAME%
set REPOSITORY=./repository/
rem set REPOSITORY="C:/Documents and Settings/andfrei/.maven/repository/"

rem Test with j2sdk-1.5.0, local repository
rem D:\TestCD\java\j2sdk-1.5.0\bin\java -Dorg.knopflerfish.gosg.jars=file: -Dch.ethz.jadabs.jxme.peeralias=%NAME% -jar framework-aop-1.3.3.jar -xargs %XARGS%

rem default startup from remote repository
java -Dorg.knopflerfish.gosg.jars=file:%REPOSITORY% -Dch.ethz.jadabs.jxme.peeralias=%NAME% -jar framework-aop-1.3.3.jar -xargs %XARGS%
