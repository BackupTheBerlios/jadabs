#!/bin/bash

#
# Jadabs-CVM starts the Jadabs framework with the CVM (J2ME/CDC) from Sun.
# It has been tested on CVM for Linux on Fedora and iPAQ/Familiar 0.7.1.
#

XARGS=init.xargs

PWD=$(pwd)
CVM_PATH=/home/andfrei/opt/j2me-cdc-1.0.1-i686/bin/cvm

REPOSITORY=-Dorg.knopflerfish.gosg.jars=file:./repository/
PEERALIAS=-Dch.ethz.jadabs.jxme.peeralias=$HOSTNAME

$CVM_PATH $REPOSITORY $PEERALIAS -Djava.class.path=framework-aop-1.3.3.jar org.knopflerfish.framework.Main -xargs $XARGS

# CVM with iPAQ Bluetooth path
#$CVM_PATH $REPOSITORY $PEERALIAS -Dsun.boot.library.path=/usr/lib -Xbootclasspath/a:/usr/share/idev_bluez.jar -Djava.class.path=.:LinuxLicense.txt:framework-aop-1.3.3.jar org.knopflerfish.framework.Main -xargs $XARGS