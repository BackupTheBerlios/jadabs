#! /usr/bin/perl


if (@ARGV > 0) {
    if ($ARGV[0] eq "transparent") {

	system("java -Xmx400M -classpath bin/lib/jadabs.jar:bin/lib/common.jar:libs/junit-3.8.1.jar:libs/javassist-2.6.jar:libs/log4j-1.2.8.jar:libs/prose.jar:libs/tools.jar ch.ethz.iks.jadabs.Jadabs -pcoprep bin/pcoprep -name MyPeer -proxy ifc -adapt hash");
    }
    elsif ($ARGV[0] eq "dynamic") {
        system("java -Xmx400M -classpath bin/lib/jadabs.jar:bin/lib/common.jar:libs/junit-3.8.1.jar:libs/javassist-2.6.jar:libs/log4j-1.2.8.jar:libs/prose.jar:libs/tools.jar ch.ethz.iks.jadabs.Jadabs -pcoprep bin/pcoprep -name MyPeer -adapt reflect");
    }
    elsif ($ARGV[0] eq "eventsystem") {

        system("java -Xmx400M -classpath bin/lib/jadabs.jar:bin/lib/common.jar:libs/junit-3.8.1.jar:libs/javassist-2.6.jar:libs/log4j-1.2.8.jar:libs/prose.jar:libs/tools.jar:bin/ext/esEvolutionTest.jar ch.ethz.iks.evolution.test.EventSystemReplaceTest -pcoprep bin/pcoprep -proxy ifc -adapt hash");
    }
    elsif ($ARGV[0] eq "testcop") {
        system("java -Xmx400M -classpath bin/lib/jadabs.jar:bin/lib/common.jar:libs/junit-3.8.1.jar:libs/javassist-2.6.jar:libs/log4j-1.2.8.jar:libs/prose.jar:libs/tools.jar -jar bin/ext/tcEvolutionTest.jar -pcoprep bin/pcoprep -adapt reflect");
    } else {
    	print "usage: perl jadabs.pl [transparent | dynamic | eventsystem | testcop]";
	exit;
    }

}
else {
    print ' no args found, defaulting...\r\n';
    system("java -Xmx400M -classpath bin/lib/jadabs.jar:bin/lib/common.jar:libs/junit-3.8.1.jar:libs/javassist-2.6.jar:libs/log4j-1.2.8.jar:libs/prose.jar:libs/tools.jar ch.ethz.iks.jadabs.Jadabs -pcoprep bin/pcoprep -name MyPeer");
}
