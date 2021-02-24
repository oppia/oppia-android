#!/bin/bash

jar_file_path=$?
config_file_path=$?
os_type=$?
github_actions_path=$1

check_protobuf() {
  echo "********************************"
  echo "Checking Protobuf file formatting"
  echo "********************************"

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

create_jar_file_path() {
  buf_file_name=$?

  if [ "$1" = "Linux" ]; then
    buf_file_name="buf-Linux-x86_64"
  else
    buf_file_name="buf-Darwin-x86_64"
  fi

  if [ $github_actions_path ]; then
    jar_file_path="$github_actions_path/oppia-android-tools/$buf_file_name"
    config_file_path="$github_actions_path/oppia-android/buf.yaml"
  else
    jar_file_path="../oppia-android-tools/$buf_file_name"
    config_file_path="buf.yaml"
  fi
}

check_github_action_path_exists() {
  if [ $github_actions_path ]; then
    create_jar_file_path $os_type $github_actions_path
  else
    create_jar_file_path $os_type
  fi
}

check_os_type() {
  if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    os_type="Linux"
  elif [[ "$OSTYPE" == "darwin"* ]]; then
    os_type="Darwin"
  elif [[ "$OSTYPE" == "freebsd"* ]]; then
    os_type="Linux"
  else
    windows_warning
  fi
}

check_os_type

check_github_action_path_exists $github_actions_path

check_protobuf
