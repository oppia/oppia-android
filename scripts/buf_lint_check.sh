#!/bin/bash

check_protobuf() {
  echo "********************************"
  echo "Checking Protobuf file formatting"
  echo "********************************"

  github_actions_path=$1

  os_type=$2

  buf_file_name=$?

  if [ "$os_type" = "Linux" ]; then
    buf_file_name="buf-Linux-x86_64"
  else
    buf_file_name="buf-Darwin-x86_64"
  fi

  jar_file_path=$?

  config_file_path=$?

  # $0 means the script runs without any params
  if [ $0 ]; then
      jar_file_path="../oppia-android-tools/$buf_file_name"
      config_file_path="buf.yaml"
  else
      jar_file_path="$github_actions_path/oppia-android-tools/$buf_file_name"
      config_file_path="$github_actions_path/oppia-android/buf.yml"
  fi

  $jar_file_path lint --config $config_file_path

  status=$?

  if [ "$status" = 0 ] ; then
    echo "Protobuf lint check completed successfully"
    exit 0
  else
    echo "********************************"
    echo "Protobuf lint check issues found"
    echo "Please fix the above issues."
    echo "********************************"
    exit 1
fi
}

windows_warning() {
  echo "********************************"
  echo "Currently Buf check doesn't support on Windows"
  echo "********************************"
}

if [[ "$OSTYPE" == "linux-gnu"* ]]
then
  check_protobuf $1 Linux
elif [[ "$OSTYPE" == "darwin"* ]]
then
  check_protobuf $1 Darwin
elif [[ "$OSTYPE" == "freebsd"* ]]
then
  check_protobuf $1 Linux
else
  windows_warning
fi
