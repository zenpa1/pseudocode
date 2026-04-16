import re
import csv

try:
    with open('table_dump.txt', 'r', encoding='utf-8') as f:
        lines = f.readlines()
except FileNotFoundError:
    print("ERROR: Could not find 'table_dump.txt'.")
    exit()

action_goto_table = {}
current_state = None
transitions_found = 0

for line in lines:
    line = line.strip()
    
    # 1. Detect state blocks
    state_match = re.search(r"State\s+(\d+):", line, re.IGNORECASE)
    if state_match:
        current_state = state_match.group(1)
        if current_state not in action_goto_table:
            action_goto_table[current_state] = {}

    # 2. Detect GOTO shifts/transitions
    # Matches: GOTO (State 16, consider)
    goto_match = re.search(r"GOTO\s*\(\s*State\s+(\d+)\s*,\s*([^\)]+)\)", line, re.IGNORECASE)
    if goto_match and current_state:
        source_state = goto_match.group(1)
        clean_symbol = goto_match.group(2).strip()
        
        if source_state not in action_goto_table:
            action_goto_table[source_state] = {}
            
        action_goto_table[source_state][clean_symbol] = f"S/G {current_state}"
        transitions_found += 1

    # 3. Detect Reduces
    reduce_match = re.search(r"Reduce by rule\s+([\w\.]+)", line, re.IGNORECASE)
    if reduce_match and current_state:
        rule = reduce_match.group(1)
        action_goto_table[current_state]['REDUCE_DEFAULT'] = f"R {rule}"
        transitions_found += 1
        
    # 4. Detect Accepts
    if "Accept" in line and current_state:
        action_goto_table[current_state]['$'] = "Accept"
        transitions_found += 1

if transitions_found > 0:
    with open('parsing_table.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['State', 'Symbol', 'Action'])
        for state in sorted(action_goto_table.keys(), key=lambda x: int(x)):
            for symbol, action in action_goto_table[state].items():
                writer.writerow([state, symbol, action])
    print(f"SUCCESS: parsing_table.csv generated with {transitions_found} clean actions!")
else:
    print("ERROR: 0 transitions found. Check table_dump.txt.")