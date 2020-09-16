#!/bin/bash
echo "Using Android Home: $ANDROID_HOME"
./gradlew --info --full-stacktrace connectedCheck
