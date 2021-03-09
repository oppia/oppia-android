#!/bin/bash

# Download buf
download_buf() {
  BUFVERSION="v0.37.1"
  echo Using Bufbuild version $BUFVERSION
  curl -sSLOC - https://github.com/bufbuild/buf/releases/download/$BUFVERSION/buf-$(uname -s)-$(uname -m)
  chmod a+x buf-$(uname -s)-$(uname -m)
  echo Buf downloaded
}

if [[ "$OSTYPE" == "linux-gnu"* ||  "$OSTYPE" == "darwin"* ]]; then
  download_buf
else
  echo "Protobuf lint check not available on $OSTYPE"
  exit 0
fi
