#!/bin/sh

# Checking for JAVA_HOME is required on *nix due
# to some distributions stupidly including kaffe in /usr/bin
if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit 1
fi

# Checking for JAVA_HOME is required on *nix due
# to some distributions stupidly including kaffe in /usr/bin
if [ "$JADABS_HOME" = "" ] ; then
  echo JADABS_HOME was not set, set to local folder
  JADABS_HOME=.
  exit 1
fi

JAVA=$JAVA_HOME/bin/java

JADABS_BOOTSTRAP_JAR=$JADABS_HOME/lib/commons-logging.jar:$JADABS_HOME/lib/jadabs.jar
JADABS_MAINCLASS=ch.ethz.iks.jadabs.Jadabs

echo "Starting JADABS."
echo "============"
#echo "      Security policy: $MERLIN_HOME/bin/security.policy"
#echo "          JVM Options: $MERLIN_JVM_OPTS"
#echo "        Bootstrap JAR: $MERLIN_BOOTSTRAP_JAR"
#echo ""

# "$JAVA" $MERLIN_JVM_OPTS "-Djava.security.policy=$MERLIN_HOME/bin/security.policy" "-Djava.ext.dirs=$MERLIN_HOME/ext" -jar "$MERLIN_BOOTSTRAP_JAR" "$@"
"$JAVA" -cp "$JADABS_BOOTSTRAP_JAR" $JADABS_MAINCLASS -pcoprep $JADABS_HOME/pcoprep "$@"
