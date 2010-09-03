#!/bin/sh
#
#
# --------------------------------------------------------

SPATH="$_"
DPATH=`/usr/bin/dirname "$SPATH"`

JAVA_BIN=""
JAVA_TOOLS=""

if [ -e $JAVA_HOME/bin/java ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"    
    JAVA_TOOLS="$JAVA_HOME/lib/tools.jar"    
elif [ -e $JRE_HOME/bin/java ]; then
    JAVA_BIN="$JRE_HOME/bin/java"
    JAVA_TOOLS="$JRE_HOME/lib/tools.jar"    
else
    JAVA_BIN=`/usr/bin/whereis java`
fi

if [ ! -e $JAVA_BIN ]; then
    echo "Couldn't find java, set JAVA_HOME or JRE_HOME"
    exit 1
fi

if [ ! -e "$DPATH/@appname.jar" ]; then
    echo "Couldn't find jar file, '$DPATH/@appname.jar'"
    exit 1
fi

$JAVA_BIN -cp "$JAVA_TOOLS:$DPATH/@appname.jar" org.pidster.tomcat.util.cli.Console $@
