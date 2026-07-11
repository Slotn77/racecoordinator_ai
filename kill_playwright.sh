#!/bin/bash

echo "Searching for Playwright processes..."

# Find processes matching 'playwright', excluding the current script's PID
pids=$(pgrep -f "playwright" | grep -v $$)

if [ -z "$pids" ]; then
    echo "No Playwright processes found."
else
    # Replace newlines with spaces for a cleaner output string
    pid_list=$(echo $pids | tr '\n' ' ')
    echo "Killing Playwright processes: $pid_list"
    
    # Kill the processes
    echo "$pids" | xargs kill -9 2>/dev/null
    echo "Done."
fi
