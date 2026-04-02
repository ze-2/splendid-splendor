#!/usr/bin/env python3
"""
Merges game output with the input that was fed, so the log reads
like a real console session where the user typed responses.

Scans for prompt patterns (ending with ': ') and inserts the corresponding
input value right after the prompt, followed by a newline.
"""
import sys
import re

def merge(output_file, input_file, result_file):
    with open(output_file, 'r') as f:
        raw = f.read()
    with open(input_file, 'r') as f:
        inputs = [line.rstrip('\n') for line in f.readlines()]

    # Prompt patterns that expect user input
    # Covers both old ConsoleUI and new ConsoleSetupUI/ConsoleActionUI/ConsoleTerminal
    prompt_pattern = re.compile(
        r'(Menu select:\s*'
        r'|Enter number of players \(2-4\):\s*'
        r'|Enter name for Player \d+:\s*'
        r'|Is Player \d+ human or AI\?[^:]*:\s*'
        r'|Choose action:\s*'
        r'|Choose gem \d+ of \d+:\s*'
        r'|Choose gem colour:\s*'
        r'|Choose card to reserve:\s*'
        r'|Choose card to buy:\s*'
        r'|Choose gem:\s*'
        r'|Choose noble:\s*'
        r'|Name cannot be empty[^:]*:\s*'
        r'|Invalid input[^:]*:\s*'
        r'|Please enter a number between \d+ and \d+\.\s*'
        r'|Press Enter to continue\.\.\.)'
    )

    input_idx = 0
    result = []
    i = 0

    while i < len(raw):
        m = prompt_pattern.search(raw, i)
        if m is None:
            result.append(raw[i:])
            break

        result.append(raw[i:m.end()])

        if input_idx < len(inputs):
            result.append(inputs[input_idx] + '\n')
            input_idx += 1

        i = m.end()

    with open(result_file, 'w') as f:
        f.write(''.join(result))

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print(f"Usage: {sys.argv[0]} <output_file> <input_file> <result_file>")
        sys.exit(1)
    merge(sys.argv[1], sys.argv[2], sys.argv[3])
