export classpath=../lib/djunit.jar:../lib/junit-3.8.1.jar:../lib/log4j-1.2.8.jar:../lib/nanoxml-lite-2.2.3.jar
java -cp $classpath djunit.framework.TestCoordinator $*
