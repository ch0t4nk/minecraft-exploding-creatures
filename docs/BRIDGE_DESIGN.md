# Bridge Design (Simple Version)

## Goal
Let Java (the real server plugin) talk to Python (your fun code) using plain text JSON lines.

## How It Works
1. Java starts a Python process: `python plugins/python/bridge/bridge_runner.py`
2. When a player joins, Java builds a small JSON object like:
   `{ "type": "player_join", "player": "Alex" }`
3. Java sends that line to Python's stdin (standard input).
4. Python reads the line, turns it into a dict, and prints a friendly message.
5. Anything Python prints shows up in the server console with the tag `[PY BRIDGE]`.

## Why JSON?
- Easy to read
- Works across languages
- Good practice for real APIs later

## Future Ideas
| Idea | What It Means |
|------|---------------|
| Send chat events | Let Python react to player messages |
| Run server commands | Python prints a special JSON back that Java catches |
| Safe sandbox | Limit what Python can import |
| Reload scripts | Type a command to restart Python bridge |

## Safety Tips
- Never run random Python you found online.
- Keep your Python code simple and trusted.

## You Can Try Next
Add more event types: block break, chat, quit.
