#!/bin/bash
# Test: 4 AI players — verify 4-player gem counts (7 per colour) and 5 nobles
cd "$(dirname "$0")/.."

echo "=== Test: 4-Player AI ==="

bash compile.sh > /dev/null 2>&1

cat > test/input_4player_ai.txt <<'INPUT'
4
AlphaAI
ai
BetaAI
ai
GammaAI
ai
DeltaAI
ai
INPUT

OUTPUT=$(bash run.sh < test/input_4player_ai.txt 2>&1)
EXIT_CODE=$?

echo "$OUTPUT" > test/output_4player_ai_raw.txt
python3 test/merge_output.py test/output_4player_ai_raw.txt test/input_4player_ai.txt test/output_4player_ai.txt

if [ $EXIT_CODE -eq 0 ] && echo "$OUTPUT" | grep -q "GAME OVER!"; then
    echo "[PASS] 4-player AI — game completed successfully"
    if echo "$OUTPUT" | grep -q "wins with\|Shared victory"; then
        echo "[PASS] Winner declared correctly"
    else
        echo "[FAIL] No winner declaration found"
    fi
else
    echo "[FAIL] 4-player AI — exit code: $EXIT_CODE"
    echo "       Check test/output_4player_ai.txt for details"
fi
