# How Plugins Work (Simple)

## Java Plugins
- Real Minecraft server plugins are written in Java.
- They listen for events like: player joins, block breaks, chat messages.
- The server calls their code.

## Python Plan
To use Python we need a "bridge" Java plugin:
1. Java hears an event.
2. Java sends info to Python (maybe over sockets or by starting a Python process).
3. Python runs your code.
4. Python maybe sends a message back.

We did NOT build that bridge fully here. This is a starter kit.
