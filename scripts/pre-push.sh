#!/bin/sh

# This script will run the pre-push checks in the given order
#  - ktlint
#  - (others in the future)

bash scripts/kotlin-lint-check.sh
bash scripts/checkstyle_lint_check.sh

# TODO(#1736): Add Bazel Linter to the project
# TODO(#970): Add XML Linter to the project
# TODO(#2097): Add Buf Linter to the project
