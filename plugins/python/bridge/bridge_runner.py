"""Simple Python side of the bridge.
Reads JSON lines from stdin. For now it just prints them back with a tag.
Kid friendly: we keep it easy.
"""
import sys
import json

print("[PY BRIDGE] Hello from Python! Waiting for events...", flush=True)

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue
    try:
        data = json.loads(line)
    except json.JSONDecodeError:
        print(f"[PY BRIDGE] Bad JSON: {line}", flush=True)
        continue
    if data.get("type") == "player_join":
        player = data.get("player", "?")
        print(f"[PY BRIDGE] Player joined: {player}", flush=True)
    else:
        print(f"[PY BRIDGE] Unknown event: {data}", flush=True)
