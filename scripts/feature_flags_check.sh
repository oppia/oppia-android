#!/bin/bash

source scripts/formatting.sh

echo "********************************"
echo "Running feature flag checks"
echo "********************************"

failed_checks=0

function item_in_array() {
  local item="$1"
  shift
  local array=("$@")

  for element in "${array[@]}"; do
    if [[ "$element" == "$item" ]]; then
      echo 1
      return
    fi
  done

  echo 0
}

function get_classes_from_constants_file() {
  # Get the file path of the FeatureFlagConstants File
  file_path=$(find . -name FeatureFlagConstants.kt)

  # Get a list of feature flag annotation classes
  annotation_classes=$(grep -E '[\s\w]*annotation\s*class' "$file_path")

  # Convert the string into an array, splitting by newline
  IFS=$'\n' read -rd '' -a array <<<"$annotation_classes"

  # Create an empty array to hold individual class names
  class_names=()

  # Iterate through each line and take the last word of the list
  for line in "${array[@]}"; do
      # Convert each line into an array of words, splitting by space
      IFS=' ' read -ra words <<<"$line"

      # Get last element of the list
      last_element="${words[${#words[@]}-1]}"

      # Add the element into class_names
      class_names+=("$last_element")
  done

  echo "${class_names[@]}"
}

function get_imported_classes_in_logger() {
  # File path containing the block of text
  file_path=$(find . -name FeatureFlagsLogger.kt)

  # Use sed to extract the block based on a starting pattern and an ending pattern
  extracted_block=$(sed -n '/class FeatureFlagsLogger @Inject constructor(/,/^)/p' "$file_path")

  # Get annotation classes
  logged_classes=$(grep -E '@Enable[A-Za-z0-9_]+' "$file_path")

  # Replace the @ symbol and spaces within each element of the list
  for i in "${!logged_classes[@]}"; do
    logged_classes[$i]=$(echo "${logged_classes[$i]}" | tr -d '@' | tr -d ' ')
  done

  # Print the logged classes
  echo "$logged_classes"
}

function get_flags_added_into_the_logging_map() {
  # File path containing the block of text
  file_path=$(find . -name FeatureFlagsLogger.kt)

  # Use sed to extract the block based on a starting pattern and an ending pattern
  extracted_block=$(sed -n '/Map<String, PlatformParameterValue<Boolean>> = mapOf(/,/)$/p' "$file_path")

  # Get any word beginning with enable from the extracted block
  enable_flags=$(grep -E 'enable[A-Za-z0-9_]+' <<<"$extracted_block")

  added_flags=()

  # Convert the string into an array, splitting by newline
  IFS=$'\n' read -rd '' -a added_flags <<<"$enable_flags"

  # Create an empty array to hold individual class names
  class_names=()

  # Iterate through each line and take the last word of the list
  for line in "${added_flags[@]}"; do
      # Remove the comma at the end of the line
      line=$(echo "$line" | awk '{sub(/,$/, ""); print}')

      # Convert each line into an array of words, splitting by space
      IFS=' ' read -ra words <<<"$line"

      # Get last element of the list
      last_element="${words[${#words[@]}-1]}"

      # Capitalize last element using sed
      last_element=$(echo "$last_element" | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)}1')

      # Add the element into class_names
      class_names+=("$last_element")
  done

  echo "${class_names[@]}"
}

function perform_checks_on_feature_flags() {
  feature_flags=($(get_classes_from_constants_file))
  imported_classes=($(get_imported_classes_in_logger))
  flags_added_to_map=($(get_flags_added_into_the_logging_map))

  file_path=$(find . -name FeatureFlagsLogger.kt)
  imports_line_number=$(grep -n 'class FeatureFlagsLogger @Inject constructor(' "$file_path" | head -n 1 | awk -F: '{print $1}')
  flags_map_line_number=$(grep -n 'Map<String, PlatformParameterValue<Boolean>> = mapOf(' "$file_path" | head -n 1 | awk -F: '{print $1}')

  for element in "${feature_flags[@]}"; do
    in_array=$(item_in_array "$element" "${imported_classes[@]}")
    if [[ $in_array -ne 1 ]]; then
      failed_checks=$((failed_checks + 1))
      echo_error "$element is not imported in the constructor argument in $file_path at line $imports_line_number"
    fi
  done

  for element in "${feature_flags[@]}"; do
    in_array=$(item_in_array "$element" "${flags_added_to_map[@]}")
    if [[ $in_array -ne 1 ]]; then
      failed_checks=$((failed_checks + 1))
      echo_error "$element is not added to the logging map in $file_path at line $flags_map_line_number"
    fi
  done

  if [[ $failed_checks -eq 0 ]]; then
    echo_success "Feature flag checks completed successfully"
    exit 0
  else
    echo "********************************"
    echo_error "Feature flag issues found."
    echo "Please fix the above issues."
    echo "********************************"
    exit 1
  fi
}

perform_checks_on_feature_flags
