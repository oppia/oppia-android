#!/bin/bash
# Workspace status script to generate stable timestamp.
# This stable variable changes on every build to force isolated post-build targets
# (like signed production AABs) to re-run and output final console paths.
echo "STABLE_BUILD_TIMESTAMP $(date +%s)"
echo "STABLE_BUILD_GIT_COMMIT $(git rev-parse HEAD)"
