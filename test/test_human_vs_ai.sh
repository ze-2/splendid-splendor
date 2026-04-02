#!/bin/bash
# Test: 1 Human + 1 AI — scripted human cycles through options 1,2,3
cd "$(dirname "$0")/.."
source test/check_output.sh

echo "=== Test: Human vs AI (scripted) ==="

bash compile.sh > /dev/null 2>&1

{
    echo "1"
    echo "2"
    echo "TestHuman"
    echo "human"
    echo "TestAI"
    echo "ai"
    for i in $(seq 1 500); do
        echo "1"
        echo "2"
        echo "3"
    done
} > test/input_human_vs_ai.txt

OUTPUT=$(bash run.sh < test/input_human_vs_ai.txt 2>&1)
EXIT_CODE=$?

echo "$OUTPUT" > test/output_human_vs_ai_raw.txt
python3 test/merge_output.py test/output_human_vs_ai_raw.txt test/input_human_vs_ai.txt test/output_human_vs_ai.txt

if [ $EXIT_CODE -eq 0 ] && check_game_complete "$OUTPUT"; then
    echo "[PASS] Human vs AI — game completed successfully"
    if check_winner_declared "$OUTPUT"; then
        echo "[PASS] Winner declared correctly"
    else
        echo "[FAIL] No winner declaration found"
    fi
    if check_noble_visit "$OUTPUT"; then
        echo "[INFO] Noble visit occurred during game"
    else
        echo "[INFO] No noble visits this game (may be normal)"
    fi
else
    echo "[FAIL] Human vs AI — exit code: $EXIT_CODE"
    echo "       Check test/output_human_vs_ai.txt for details"
    tail -20 test/output_human_vs_ai_raw.txt
fi
