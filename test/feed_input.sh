#!/bin/bash
# Helper: feeds input file line-by-line into a running process via a named pipe,
# echoing each input line to stderr so it appears inline with prompts.
# Usage: feed_input.sh <input_file> <command...>

INPUT_FILE="$1"
shift

FIFO=$(mktemp -u /tmp/splendor_input.XXXXXX)
mkfifo "$FIFO"

# Feed input lines with tiny delay so prompts print first
(
    while IFS= read -r line; do
        sleep 0.05
        echo "$line"
    done < "$INPUT_FILE"
    # Keep pipe open briefly then close
    sleep 0.5
) > "$FIFO" &
FEEDER_PID=$!

# Run the game with input from pipe, capture all output
# Use script to get a pseudo-terminal so input is echoed
if command -v script > /dev/null 2>&1; then
    # macOS script syntax
    OUTPUT=$(script -q /dev/null bash -c "$* < $FIFO" 2>&1)
else
    OUTPUT=$("$@" < "$FIFO" 2>&1)
fi

wait $FEEDER_PID 2>/dev/null
rm -f "$FIFO"

echo "$OUTPUT"
