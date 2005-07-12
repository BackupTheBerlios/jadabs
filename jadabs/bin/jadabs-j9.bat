rem
rem Jadabs-J9 starts the Jadabs framework with the J9 (J2ME/CDC) from IBM.
rem

set XARGS=init.xargs
set JAVA_HOME=D:\TestCD\java\j9-cdc-win32

D:\TestCD\java\j9-cdc-win32\bin\j9 -Dorg.knopflerfish.gosg.jars=file:./repository/ -Dch.ethz.jadabs.jxme.peeralias=%COMPUTERNAME% -classpath framework-aop-1.3.3.jar org.knopflerfish.framework.Main -xargs %XARGS%