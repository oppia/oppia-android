#!/bin/bash

# Download ktlint
KTLINT="0.37.1"
echo Using Ktlint $KTLINT
curl -sSLOC - https://github.com/pinterest/ktlint/releases/download/$KTLINT/ktlint
chmod a+x ktlint
echo Ktlint file downloaded
