export bluez_device=hci4
export properties="-Djava.library.path=/usr/lib -DallowedBTAddr=$2 -DconfigPath=$1"
export classpath=/usr/share/java/idev_bluez.jar:../lib:../lib/djunit.jar:../lib/junit-3.8.1.jar:../lib/log4j-1.2.8.jar:../lib/nanoxml-lite-2.2.3.jar:../lib/scatter.jar
java $properties -classpath $classpath djunit.simple.runner.SimpleTestRunner ch.ethz.iks.jxme.bluetooth.test.scatternet.IntermediateChainLink
