#!/bin/bash
# Helper: strips ANSI codes from raw output and checks for game completion patterns
# Usage: source test/check_output.sh
# Then call: check_game_complete "$OUTPUT"

strip_ansi() {
    sed 's/\x1b\[[0-9;]*m//g' | sed 's/\x1b\[[0-9;]*[A-Za-z]//g'
}

check_game_complete() {
    local RAW="$1"
    local CLEAN
    CLEAN=$(echo "$RAW" | strip_ansi)

    if echo "$CLEAN" | grep -q "G A M E   O V E R"; then
        return 0
    fi
    return 1
}

check_winner_declared() {
    local RAW="$1"
    local CLEAN
    CLEAN=$(echo "$RAW" | strip_ansi)

    if echo "$CLEAN" | grep -q "wins!\|Shared victory\|wins with"; then
        return 0
    fi
    return 1
}

check_noble_visit() {
    local RAW="$1"
    local CLEAN
    CLEAN=$(echo "$RAW" | strip_ansi)

    if echo "$CLEAN" | grep -qi "noble\|visited"; then
        return 0
    fi
    return 1
}

check_reprompt() {
    local RAW="$1"
    local CLEAN
    CLEAN=$(echo "$RAW" | strip_ansi)

    if echo "$CLEAN" | grep -q "Please enter a number\|Invalid input\|cannot be empty"; then
        return 0
    fi
    return 1
}
