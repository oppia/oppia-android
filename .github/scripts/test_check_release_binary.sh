#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEMP_DIR=$(mktemp -d)
trap 'rm -rf "$TEMP_DIR"' EXIT

cat > "$TEMP_DIR/gsutil" <<'EOF'
#!/usr/bin/env bash

case "$GSUTIL_MODE" in
  existing)
    echo "Bucket object exists"
    exit 0
    ;;
  missing)
    echo "CommandException: No URLs matched"
    exit 1
    ;;
  auth_error)
    echo "AccessDeniedException: Permission denied"
    exit 1
    ;;
  other_error)
    echo "Internal server error"
    exit 2
    ;;
esac
EOF

chmod +x "$TEMP_DIR/gsutil"
export PATH="$TEMP_DIR:$PATH"

GCS_PATH="gs://test-bucket/test.aab"

run_test() {
  local mode="$1"
  local expected="$2"
  local output_file="$TEMP_DIR/output"

  : > "$output_file"

  if GSUTIL_MODE="$mode" bash "$SCRIPT_DIR/check_release_binary.sh" \
    "$GCS_PATH" "$output_file"; then
    status=0
  else
    status=$?
  fi

  if [[ "$expected" == "failure" ]]; then
    [[ "$status" -ne 0 ]] || {
      echo "FAIL: $mode should fail"
      exit 1
    }
  else
    [[ "$status" -eq 0 ]] || {
      echo "FAIL: $mode unexpectedly failed"
      exit 1
    }

    grep -q "should_build=$expected" "$output_file" || {
      echo "FAIL: $mode produced the wrong decision"
      exit 1
    }
  fi

  echo "PASS: $mode"
}

run_test existing false
run_test missing true
run_test auth_error failure
run_test other_error failure

echo "All GCS pre-check tests passed."