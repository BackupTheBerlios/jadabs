#! /bin/bash
#
#
# (history at the end)
#
# $Id: prose_sh_functions.sh,v 1.2 2003/07/29 16:13:12 anicoara Exp $
#

# Summary:

#---------------------------------------------
# Utility Functions Common to PROSE scripts
#---------------------------------------------


prose_msg()
{
    if [ -z "$ARG_QUIET" ]
    then
       echo "prose: *Message* $1"
       shift;
       for i in "$@"; do
          echo "                 $i";
       done
    fi
}

prose_err()
{
    echo "prose: *Error*   $1"
    shift;
    for i in "$@"; do
       echo "                 $i";
    done
    exit 1;
}





#---------------------------------------------
# PATH & LDPATH FOR PROSE
#
# sets PROSE_TOP PROSE_LIB PROSE_CPATH PROSE_BOOTCPATH PROSE_DLPATH



prose_arch_specific()
{
# first figure out on what system we are runing
if type -path uname 2>&1 >/dev/null
then
  OS_ARCH=`uname -m` || prose_err "uname does not support the -m option;"

  case $OS_ARCH in
  i*86)
      OS_ARCH=i386
      EGREP="grep -qE";
      ;;
 "Power Macintosh")
      OS_ARCH=ppc
      EGREP="grep -qE";
      ;;
  sun4u)
      OS_ARCH=sparc;
      EGREP="egrep -s";
      ;;
  arm*)
      OS_ARCH=armv4l;
      EGREP="grep -qE";
      ;;
  *)
      OS_ARCH=x86;
      EGREP="grep -qE";
      ;;
  esac
else
    OS_ARCH="x86"
    EGREP="grep -qE"
fi

if [ $OS_ARCH = "i386" ]
then
case `uname -s` in
*NT*)
    OS_ARCH="x86"
    EGREP="grep -qE"
    ;;
Linux)
    ;;
*)
    prose_err "cannot proceed (operating system unknown)"
    ;;
esac
fi



CPS=":"
if [ $OS_ARCH = "x86" ]
then
    CPS=";"
    PROSE_FIX_OUTPUT="TRUE"
else
    CPS=":"
fi

}

prose_set_prosepath()
{

#1. In the development tree:
# <project>/lib/pvm.sh
#2. In the common repository (iks)
# <project>/site/lib/pvm.sh
#3. In the binary release
# <usr-local>/bin/pvm.sh


if  type cygpath >/dev/null 2>&1
then
CUREXEC=$(cygpath "$0")
else
CUREXEC="$0"
fi

PROSEDIRNAME=`dirname "$CUREXEC"`
PROSEBASENAME=`basename "$CUREXEC"`
if [ -f "$PROSEDIRNAME/../programs/$PROSEBASENAME" ]
then
    #we are in the prose development tree
    PROSE_TOP=$(cd "$PROSEDIRNAME/..";pwd)
    PROSE_BOOTCPATH=$PROSE_TOP/bootclasses:$PROSE_TOP/lib/bcel.jar
    PROSE_CPATH=$PROSE_TOP/src:$PROSE_TOP/site/lib/iks-util-loc.jar
    PROSE_TOOLS=$PROSE_TOP/src:$PROSE_TOP/lib/jlfgr-1_0.jar:$PROSE_TOP/site/lib/iks-util-loc.jar
    PROSE_LDPATH="$PROSE_TOP/lib/$OS_ARCH"
else

    if [ -f "$PROSEDIRNAME/../../site/lib/$PROSEBASENAME" ]
    then
        #we are at iks but not in the prose tree
        PROSE_TOP=$(cd "$PROSEDIRNAME/../..";pwd)
	PROSE_LIB=$PROSE_TOP/site/lib
	PROSE_CPATH=$PROSE_LIB/
        PROSE_BOOTCPATH=$PROSE_TOP/bootclasses
    else
	#we are in the release tree
	PROSE_TOP=$(cd "$PROSEDIRNAME/..";pwd)
	PROSE_LIB=$PROSE_TOP/lib
	PROSE_CPATH=$PROSE_LIB/jdk-prose-loc.jar
	PROSE_BOOTCPATH=$PROSE_LIB/jdk-jvmai-loc.jar
	PROSE_LDPATH="$PROSE_LIB/$OS_ARCH"
	PROSE_TOOLS="$PROSE_LIB/prose-compile-loc.jar"
    fi
fi

#transform classpathes if ncessary
if [ $OS_ARCH = "x86" ]
then
    PROSE_TOP=`cygpath -w "$PROSE_TOP"`
    [ -n "$PROSE_LIB" ] &&     PROSE_LIB=`cygpath -w "$PROSE_LIB"`
    PROSE_CPATH=`cygpath -w -p "$PROSE_CPATH"`
    PROSE_BOOTCPATH=`cygpath -w -p "$PROSE_BOOTCPATH"`
    PROSE_LDPATH=`cygpath -w -p "$PROSE_LDPATH"`
    PROSE_TOOLS=`cygpath -w -p "$PROSE_TOOLS"`
fi

}

#---------------------------------------------
#  JAVA
#
#  sets PROSE_JAVA_INT  (the interpreter)
#       PROSE_JAVA_HOME (jdk home)

prose_find_jvm()
{

# check type of vm:
PROSE_JAVA_INT=${PROSE_JAVA_INT:-"java"};
PROSE_JAVA_HOME=${PROSE_JAVA_HOME:-"$(dirname $(which java))/.."}

if  type cygpath >/dev/null 2>&1 
then
echo "not"
else
[ -x "$PROSE_JAVA_HOME/bin/$PROSE_JAVA_INT" ]  || prose_err "Your 'java' script is not executable. Please" \
					     "Check your 'PROSE_JAVA_HOME' 'PROSE_JAVA_INT' values"

fi
}

#---------------------------------------------
#  CHECK JAVA
#
#  checks whether prose works with this java

prose_check_jvm()
{
JAVA_WORKS=0;
UNSUPPORTED="unsup";

"$PROSE_JAVA_HOME/bin/$PROSE_JAVA_INT" -version > /tmp/prose-tmp-1$$ 2>&1 && JAVA_WORKS=1;

if [ $JAVA_WORKS != 1 ]
then
    prose_msg "WARNING: your java may crash!" \
              "when trying to run $PROSE_JAVA_HOME/bin/$PROSE_JAVA_INT," \
              "the error message was:"  ""
    echo      "--------------------ERROR MESSAGE BEGIN--------------------------------------"
    cat /tmp/prose-tmp-1$$;
    echo      "--------------------ERROR MESSAGE END----------------------------------------"
    echo      ""
fi


JAVA_VERSION=$UNSUPPORTED;
$EGREP  'java version "1.2.*' /tmp/prose-tmp-1$$  && JAVA_VERSION="1.2.*";
$EGREP  'java version "1.3.*' /tmp/prose-tmp-1$$  && JAVA_VERSION="1.3.*";
$EGREP  'java version "1.4.0' /tmp/prose-tmp-1$$  && JAVA_VERSION="1.4.0";
$EGREP  'java version "1.4.1' /tmp/prose-tmp-1$$  && JAVA_VERSION="1.4.1";
$EGREP  'java version "1.4.2' /tmp/prose-tmp-1$$  && JAVA_VERSION="1.4.2";



JAVA_VENDOR=$UNSUPPORTED;
case $JAVA_VERSION in
    1.2*)
         $EGREP 'Classic VM|Solaris VM'  /tmp/prose-tmp-1$$ && JAVA_VENDOR="sun";
    ;;

    1.3*|1.4*)
         $EGREP 'HotSpot'  /tmp/prose-tmp-1$$  && JAVA_VENDOR="sun";
	 $EGREP 'Blackdown'  /tmp/prose-tmp-1$$ && JAVA_VENDOR="blackdown";
    ;;
esac

if [ $JAVA_VENDOR = $UNSUPPORTED -o $JAVA_VERSION = $UNSUPPORTED ]
then
    prose_err "sorry, the current vm ($JAVA_VERSION,$JAVA_VENDOR) is not supported";
fi

rm /tmp/prose-tmp-1$$
}



##------------------------------------
# OUTPUT A codebase formatted classpath

prose_codebasepath()
{



# if it does not contain whitespace, go on anywhay
local CODEBASE=$(echo "$1" | perl -pe "s/$CPS/ /g");
local RESULT=""
for i in $CODEBASE;
do

   if [ -d "$i" ]
   then
      CRTDIR=$(cd "$i"; pwd)/
   else
      if [ -d $(dirname "$i") ]
      then
	  # this directory actuallly exits
          CRTDIR=$(cd $(dirname "$i"); pwd)/$(basename "$i")
      else
      	  # this directory DOES NOT exit
	  CRTDIR=$(dirname "$i")/$(basemame "$i")
      fi
   fi

    # as ususual we format for windows if cygpath exists
   if type cygpath >/dev/null 2>&1
   then
    CRTDIR=$(cygpath -w "$CRTDIR");
   fi

   RESULT=${RESULT:+"$RESULT file://$CRTDIR"}
   RESULT=${RESULT:-"file://$CRTDIR"}
done

echo "$RESULT"
return
}





