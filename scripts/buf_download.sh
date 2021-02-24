#!/bin/bash

# Download buf
BUFVERSION="v0.37.1"
echo Using Bufbuild version $BUFVERSION
curl -sSLOC - https://github.com/bufbuild/buf/releases/download/$BUFVERSION/buf-$(uname -s)-$(uname -m)
chmod a+x buf-$(uname -s)-$(uname -m)
echo Buf file downloaded
