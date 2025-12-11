#!/bin/sh
# Entrypoint for websockify on Render.com
# Usage: PORT=<public> TARGET_HOST=<host> TARGET_PORT=<port> ./start-websockify.sh

set -e

# Use Render.com provided PORT or default to 10000
PUBLIC_PORT="${PORT:-10000}"
# Target host and port (Java server)
TARGET_HOST="${TARGET_HOST:-localhost}"
TARGET_PORT="${TARGET_PORT:-8887}"

# Print config
echo "Starting websockify: public :$PUBLIC_PORT -> $TARGET_HOST:$TARGET_PORT"

# Start websockify
exec websockify --web=./ --verbose "$PUBLIC_PORT" "$TARGET_HOST:$TARGET_PORT"

