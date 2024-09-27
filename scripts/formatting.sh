#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

function echo_error() {
    echo -e "${RED}$1${NC}"
}

function echo_success() {
    echo -e "${GREEN}$1${NC}"
}

function echo_warning() {
    echo -e "${YELLOW}$1${NC}"
}

