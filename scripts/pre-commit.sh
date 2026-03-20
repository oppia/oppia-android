#!/bin/bash

# Pre-commit hook for running local checks before committing.
# The binary file check is now handled by a Kotlin script in CI
# (see .github/workflows/static_checks.yml).
#
# To install this hook, symlink or copy this file to .git/hooks/pre-commit:
#   ln -sf ../../scripts/pre-commit.sh .git/hooks/pre-commit
#   chmod +x .git/hooks/pre-commit

echo "Running pre-commit checks..."
echo "Note: Binary file checks are handled by CI (scripts:binary_file_check)."
echo "Pre-commit hook completed."
