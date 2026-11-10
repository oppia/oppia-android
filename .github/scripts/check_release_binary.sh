#!/usr/bin/env bash

set -euo pipefail

GCS_PATH="$1"
OUTPUT_FILE="$2"

GCS_STATUS=0
GCS_OUTPUT=$(gsutil stat "$GCS_PATH" 2>&1) || GCS_STATUS=$?

if [[ "$GCS_STATUS" -eq 0 ]]; then
  echo "should_build=false" >> "$OUTPUT_FILE"
  echo "AAB already exists. Skipping build."
elif grep -q "No URLs matched" <<< "$GCS_OUTPUT"; then
  echo "should_build=true" >> "$OUTPUT_FILE"
  echo "AAB does not exist. Proceeding with build."
else
  echo "$GCS_OUTPUT"
  echo "::error::Unexpected GCS error while checking $GCS_PATH."
  exit "$GCS_STATUS"
fi