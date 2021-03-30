#!/bin/bash

# Compute the list of tests that are affected by changes on this branch (including in the working
# directory). This script is useful to verify that only the tests that could break due to a change
# are actually verified as passing. Note that this script does not actually run any of the tests.
#
# Usage:
#   bash scripts/compute_affected_tests.sh <bazel_path>
#
# This script is based on https://github.com/bazelbuild/bazel/blob/d96e1cd/scripts/ci/ci.sh and the
# documentation at https://docs.bazel.build/versions/master/query.html. Note that this script will
# automatically list all tests if it's run on the develop branch (the idea being that test
# considerations on the develop branch should always consider all targets).

DEST_FILE=$1

# Reference: https://stackoverflow.com/a/6245587.
current_branch=$(git rev-parse --abbrev-ref HEAD)

printf "Current branch: $current_branch\n"

if [[ "$current_branch" != "develop" ]]; then
  # Compute all files that have been changed on this branch. https://stackoverflow.com/a/9294015 for
  # constructing the arrays.
  commit_range=HEAD..$(git merge-base origin/develop HEAD)
  printf "Commit range: $commit_range\n\n"
  changed_committed_files=($(git diff --name-only $commit_range))
  # See https://stackoverflow.com/a/26304373 for reference on printing Bash arrays.
  (IFS=",$IFS"; printf "Committed changed files to consider: %s\n\n" "${changed_committed_files[*]}"; IFS="${IFS:1}")
  changed_staged_files=($(git diff --name-only --cached))
  (IFS=",$IFS"; printf "Staged changes files to consider: %s\n\n" "${changed_staged_files[*]}"; IFS="${IFS:1}")
  changed_unstaged_files=($(git diff --name-only))
  (IFS=",$IFS"; printf "Changed unstaged files to consider: %s\n\n" "${changed_unstaged_files[*]}"; IFS="${IFS:1}")
  # See https://stackoverflow.com/a/35484355 for how this works.
  changed_untracked_files=($(git ls-files --others --exclude-standard))
  (IFS=",$IFS"; printf "Changed untracked files to consider: %s\n\n" "${changed_untracked_files[*]}"; IFS="${IFS:1}")

  changed_files_with_potential_duplicates=(
    "${changed_committed_files[@]}"
    "${changed_staged_files[@]}"
    "${changed_unstaged_files[@]}"
    "${changed_untracked_files[@]}"
  )

  # De-duplicate files: https://unix.stackexchange.com/q/377812.
  changed_files=($(printf "%s\n" "${changed_files_with_potential_duplicates[@]}" | sort -u))
  (IFS=",$IFS"; printf "All files to consider: %s\n\n" "${changed_files[*]}"; IFS="${IFS:1}")

  # Filter all of the source files among those that are actually included in Bazel builds.
  changed_bazel_files=()
  for changed_file in "${changed_files[@]}"; do
    changed_bazel_files+=($(bazel query --noshow_progress $changed_file 2> /dev/null))
  done

  (IFS=",$IFS"; printf "Changed Bazel files: %s\n\n" "${changed_bazel_files[*]}"; IFS="${IFS:1}")

  # Compute the list of affected tests based on source files.
  source_affected_targets="$(bazel query --noshow_progress --universe_scope=//... --order_output=no "kind(test, allrdeps(set(${changed_bazel_files[@]})))" 2>/dev/null)"
  (IFS=",$IFS"; printf "Affected targets: %s\n\n" "${source_affected_targets[*]}"; IFS="${IFS:1}")

  # Compute the list of files to consider for BUILD-level changes (this uses the base file list as a
  # reference since Bazel's query won't find matching targets for utility bzl files that can still
  # affect the build). https://stackoverflow.com/a/44107086 for reference on changing case matching.
  shopt -s nocasematch
  changed_bazel_support_files=()
  for changed_file in "${changed_files[@]}"; do
    if [[ "$changed_file" =~ ^.+?\.bazel$ ]] || [[ "$changed_file" =~ ^.+?\.bzl$ ]] || [[ "$changed_file" == "WORKSPACE" ]]; then
      changed_bazel_support_files+=("$changed_file")
    fi
  done
  shopt -u nocasematch
  (IFS=",$IFS"; printf "Changed Bazel support files: %s\n\n" "${changed_bazel_support_files[*]}"; IFS="${IFS:1}")

  # Compute the list of affected tests based on BUILD/Bazel/WORKSPACE files. These are generally
  # framed as: if a BUILD file changes, run all tests transitively connected to it.
  # Reference for joining an array to string: https://stackoverflow.com/a/53839433.
  printf -v changed_bazel_support_files_list '%s,' "${changed_bazel_support_files[@]}"
  build_affected_targets=$(bazel query --noshow_progress --universe_scope=//... --order_output=no "filter('^[^@]', kind(test, allrdeps(siblings(rbuildfiles(${changed_bazel_support_files_list%,})))))" 2>/dev/null)
  (IFS=",$IFS"; printf "Affected Bazel support targets: %s\n\n" "${build_affected_targets[*]}"; IFS="${IFS:1}")

  all_affected_targets_with_potential_duplicated=(
    "${source_affected_targets[@]}"
    "${build_affected_targets[@]}"
  )

  # Print all of the affected targets without duplicates.
  printf "%s" "${all_affected_targets_with_potential_duplicated[@]}" | sort -u
  printf "%s" "${all_affected_targets_with_potential_duplicated[@]}" | sort -u > $DEST_FILE
else
  # Print all test targets.
  bazel query --noshow_progress "kind(test, //...)" 2>/dev/null
  bazel query --noshow_progress "kind(test, //...)" 1>$DEST_FILE
fi
