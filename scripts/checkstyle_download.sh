#!/bin/bash

# Download checkstyle
CHECKSTYLE="8.37"
echo Using Checkstyle version $CHECKSTYLE
curl -sSLOC - https://github.com/checkstyle/checkstyle/releases/download/checkstyle-$CHECKSTYLE/checkstyle-$CHECKSTYLE-all.jar
chmod a+x checkstyle-$CHECKSTYLE-all.jar
echo Checkstyle file downloaded
