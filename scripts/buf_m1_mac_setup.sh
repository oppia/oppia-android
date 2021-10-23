#!/bin/bash

# Add protobuf platform for M1 Mac
arch_name="$(uname -m)"
if [ "${arch_name}" = "x86_64" ] && [[ "$OSTYPE" == "darwin"* ]]; then
    if [ "$(sysctl -in sysctl.proc_translated)" = "1" ]; then
        echo 'protobuf_platform=osx-x86_64' >> ../oppia-android/local.properties
        echo 'Added protobuf platform to local properties.'
    fi
fi