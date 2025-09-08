# PythonBridge Plugin (Placeholder)

This little Java plugin is just a shell. Its job in the future would be to:
1. Load or watch Python scripts.
2. Pass Minecraft events to Python.
3. Let Python send messages or run commands.

Right now it only prints messages when it turns on or off.

## Build Steps (for later)
You need Java 17 and Maven installed.

```
mvn package
```
Your jar will appear in `target/`. Copy it into the `server/plugins` folder.
