#!/bin/bash

# This script will run the pre-push checks in the given order
#  - ktlint
#  - checkstyle
#  - (others in the future)

bash scripts/ktlint_lint_check.sh
bash scripts/checkstyle_lint_check.sh

# TODO(#1736): Add Bazel Linter to the project
# TODO(#970): Add XML Linter to the project
# TODO(#2097): Add Buf Linter to the project
