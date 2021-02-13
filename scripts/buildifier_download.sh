#!/bin/bash

# Download buildifier
BUILDIFIER="3.4.0"
echo Using Buildifier version $BUILDIFIER
curl -sSLO https://github.com/bazelbuild/buildtools/releases/download/$BUILDIFIER/buildifier > buildifier
chmod a+x buildifier
echo Buildifier file downloaded
