#!/bin/bash

source scripts/formatting.sh

# This script will run the pre-push checks in the given order
# - ktlint
# - checkstyle
# - buf
# - (others in the future)

if bash scripts/ktlint_lint_check.sh && bash scripts/checkstyle_lint_check.sh && bash scripts/buf_lint_check.sh ; then
  echo_success "All checks passed successfully"
  exit 0
else
  exit 1
fi

# TODO(#3000): Add Bazel Linter to the project
# TODO(#970): Add XML Linter to the project
