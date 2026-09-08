#!/bin/bash
set -e

SPEC_FILE="contract/openapi.json"

# Capture hash of current spec before generation
BEFORE=$(sha256sum "$SPEC_FILE" 2>/dev/null | cut -d' ' -f1 || echo "")

# Run Quarkus build to trigger OpenAPI spec generation. The hook runs at the repository root and
# the build lives under api/, so only the build call moves; the paths above stay repository-relative.
(cd api && ./gradlew :api-application:quarkusBuild -q)

# Check if spec changed
AFTER=$(sha256sum "$SPEC_FILE" 2>/dev/null | cut -d' ' -f1 || echo "")

if [ "$BEFORE" != "$AFTER" ]; then
    git add "$SPEC_FILE"
    echo "OpenAPI spec was updated. Please re-run 'git commit' to include it."
    exit 1
fi
