#!/bin/bash
# Test: Invalid input handling — bad inputs followed by valid ones
cd "$(dirname "$0")/.."
source test/check_output.sh

echo "=== Test: Invalid Input Handling ==="

bash compile.sh > /dev/null 2>&1

cat > test/input_invalid.txt <<'INPUT'
0
5
abc
1
0
5
abc
2

TestPlayer
invalid
ai
AIBot
ai
INPUT

OUTPUT=$(bash run.sh < test/input_invalid.txt 2>&1)
EXIT_CODE=$?

echo "$OUTPUT" > test/output_invalid_input_raw.txt
python3 test/merge_output.py test/output_invalid_input_raw.txt test/input_invalid.txt test/output_invalid_input.txt

if [ $EXIT_CODE -eq 0 ] && check_game_complete "$OUTPUT"; then
    echo "[PASS] Invalid input handled — game completed after recovery"
else
    if check_reprompt "$OUTPUT"; then
        echo "[PASS] Invalid input re-prompted correctly"
    else
        echo "[FAIL] Unexpected behaviour with invalid input"
    fi
    echo "       Check test/output_invalid_input.txt for details"
fi
