#!/bin/bash
# Test: 2 AI players — full game to completion
cd "$(dirname "$0")/.."
source test/check_output.sh

echo "=== Test: 2-Player AI vs AI ==="

bash compile.sh > /dev/null 2>&1

cat > test/input_ai_vs_ai.txt <<'INPUT'
1
2
AlphaAI
ai
BetaAI
ai
INPUT

OUTPUT=$(bash run.sh < test/input_ai_vs_ai.txt 2>&1)
EXIT_CODE=$?

echo "$OUTPUT" > test/output_ai_vs_ai_raw.txt
python3 test/merge_output.py test/output_ai_vs_ai_raw.txt test/input_ai_vs_ai.txt test/output_ai_vs_ai.txt

if [ $EXIT_CODE -eq 0 ] && check_game_complete "$OUTPUT"; then
    echo "[PASS] 2-player AI vs AI — game completed successfully"
    if check_winner_declared "$OUTPUT"; then
        echo "[PASS] Winner declared correctly"
    else
        echo "[FAIL] No winner declaration found"
    fi
else
    echo "[FAIL] 2-player AI vs AI — exit code: $EXIT_CODE"
    echo "       Check test/output_ai_vs_ai.txt for details"
fi
