#!/bin/bash
echo "Java Home:"
echo $JAVA_HOME
echo "Android Home:"
echo $ANDROID_HOME
echo "Android SDK Home:"
echo $ANDROID_SDK_HOME
export ANDROID_HOME=/tmp
./gradlew --info --full-stacktrace connectedCheck
