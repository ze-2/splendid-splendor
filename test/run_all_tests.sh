#!/bin/bash
# Runs all test scripts and prints a summary
cd "$(dirname "$0")/.."

echo "========================================"
echo "    Splendor — Full Test Suite"
echo "========================================"
echo ""

PASS=0
FAIL=0

run_test() {
    echo "----------------------------------------"
    RESULT=$(bash "test/$1" 2>&1)
    echo "$RESULT"
    PASSES=$(echo "$RESULT" | grep -c "\[PASS\]")
    FAILS=$(echo "$RESULT" | grep -c "\[FAIL\]")
    PASS=$((PASS + PASSES))
    FAIL=$((FAIL + FAILS))
    echo ""
}

run_test "test_ai_vs_ai.sh"
run_test "test_3player_ai.sh"
run_test "test_4player_ai.sh"
run_test "test_human_vs_ai.sh"
run_test "test_invalid_input.sh"

echo "========================================"
echo "    SUMMARY: $PASS passed, $FAIL failed"
echo "========================================"

if [ $FAIL -gt 0 ]; then
    echo "    Output logs saved in test/"
    exit 1
else
    echo "    All tests passed!"
    exit 0
fi
