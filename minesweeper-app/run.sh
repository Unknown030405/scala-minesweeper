#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$DIR/jre/bin/java" -jar "$DIR/minesweeper.jar" &
echo "Runs with PID: $!"