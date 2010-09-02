#!/bin/sh
#
#
# --------------------------------------------------------

SPATH="$_"
DPATH=`/usr/bin/dirname "$SPATH"`

echo "$DPATH"

#/usr/bin/java -cp "$DPATH/tomcat-cli.jar" org.pidster.tomcat.util.cli.TerminalImpl $@

/usr/bin/java -jar "$DPATH/@appname.jar" $@