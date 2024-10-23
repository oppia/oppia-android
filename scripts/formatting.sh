#!/bin/bash

#Defines color codes for output formatting

# Red color for error messages
RED='\033[0;31m'

# Green color for success messages
GREEN='\033[0;32m'

# Yellow color for warnings messages
YELLOW='\033[0;33m'

# No color, used to reset the color after each message
NC='\033[0m'

# Function to print an error message in red
function echo_error() {
    echo -e "${RED}$1${NC}"
}

# Function to print a success message in green
function echo_success() {
    echo -e "${GREEN}$1${NC}"
}

# Function to print a warning message in yellow
function echo_warning() {
    echo -e "${YELLOW}$1${NC}"
}
