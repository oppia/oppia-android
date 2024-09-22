#!/bin/bash

source scripts/formatting.sh

jar_file_path=$?
config_file_path=$?
os_type=$?
github_actions_path=$1

lint_protobuf_files() {
  echo "********************************"
  echo "Checking Protobuf file formatting"
  echo "********************************"

  $jar_file_path lint --config $config_file_path

  status=$?

  if [ "$status" = 0 ] ; then
    echo_success "Protobuf lint check completed successfully"
    exit 0
  else
    echo "********************************"
    echo_error "Protobuf lint check issues found. Please fix them before pushing your code."
    echo "********************************"
    exit 1
  fi
}

populate_jar_config_file_paths() {
  buf_file_name=$?

  if [ "$1" = "Linux" ]; then
    buf_file_name="buf-Linux-x86_64"
  else
    buf_file_name="buf-Darwin-x86_64"
  fi

  if [ $github_actions_path ]; then
    jar_file_path="$github_actions_path/oppia-android-tools/$buf_file_name"
    config_file_path="/home/runner/work/oppia-android/oppia-android/buf.yaml"
  else
    jar_file_path="../oppia-android-tools/$buf_file_name"
    config_file_path="buf.yaml"
  fi
}

ensure_jar_config_file_paths_are_set() {
  if [ $github_actions_path ]; then
    populate_jar_config_file_paths $os_type $github_actions_path
  else
    populate_jar_config_file_paths $os_type
  fi
}

check_os_type() {
  if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    os_type="Linux"
  elif [[ "$OSTYPE" == "darwin"* ]]; then
    os_type="Darwin"
  else
    echo_error "Protobuf lint check not available on $OSTYPE"
    exit 0
  fi
}

check_os_type

ensure_jar_config_file_paths_are_set $github_actions_path

lint_protobuf_files
