#!/bin/bash

if bazel run //scripts:pre_push_checks -- $(pwd) ; then
  echo "All checks passed successfully"
  exit 0
else
  exit 1
fi
