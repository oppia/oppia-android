#!/bin/bash

# Download buildifier
BUILDIFIER="3.4.0"
echo Using Buildifier version $BUILDIFIER
if [[ "$OSTYPE" == "darwin"* ]]; 
then
    curl -sSLO https://github.com/bazelbuild/buildtools/releases/download/$BUILDIFIER/buildifier.mac > buildifier.mac
    chmod a+x buildifier.mac
    mv buildifier.mac buildifier
else
    curl -sSLO https://github.com/bazelbuild/buildtools/releases/download/$BUILDIFIER/buildifier > buildifier
    chmod a+x buildifier
fi

echo Buildifier file downloaded
