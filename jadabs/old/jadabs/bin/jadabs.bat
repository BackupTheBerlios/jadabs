@echo off

rem set JAVA CMD
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME

set JAVA=java

goto SET_JADABS

:USE_JAVA_HOME
set JAVA=%JAVA_HOME%\bin\java

:SET_JADABS
if "%JADABS_HOME%" == "" set JADABS_HOME=.
rem set MERLIN_CMD_LINE_ARGS=%*
set JADABS_BOOTSTRAP_JAR=%JADABS_HOME%\lib\commons-logging.jar;%JADABS_HOME%\lib\jadabs.jar
rem set MERLIN_SECURITY_POLICY=-Djava.security.policy=%MERLIN_HOME%\bin\security.policy

rem %JAVA% %MERLIN_SECURITY_POLICY% %MERLIN_JVM_OPTS% -jar %MERLIN_BOOTSTRAP_JAR% %MERLIN_CMD_LINE_ARGS%
:RUN_JADABS
%JAVA% -cp %JADABS_BOOTSTRAP_JAR% ch.ethz.iks.jadabs.Jadabs -pcoprep %JADABS_HOME%\pcoprep %*
goto EndOfScript

:JadabsNotSet
echo Please, set the JADABS_HOME variable in your environment to match the 
echo location of Jadabs distribution.
goto EndOfScript

:EndOfScript
