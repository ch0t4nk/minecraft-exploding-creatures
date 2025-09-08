# Setup Guide (Easy Mode)

## 1. Install Stuff You Need
- Install Java 17 (very important). You can get it from Adoptium.
- Install Python 3.10 or newer.
- Install Git (optional but helpful).

## 2. Get The Paper Server Jar
1. Go to https://papermc.io/downloads
2. Pick the latest version that matches your Minecraft.
3. Download the file (like `paper-1.21.1.jar`).
4. Put it in the `server` folder and rename it to `paper.jar`.

## 3. Agree To The EULA
Open `server/eula.txt` and change `eula=false` to `eula=true` (or run the start script with the AgreeEula flag later).

## 4. Make The Python Environment
In PowerShell run:
```
./scripts/setup_python_env.ps1
```
This makes a `venv` (a safe mini Python world) and installs packages.

## 5. Start The Server
```
./scripts/start_server.ps1 -AgreeEula
```
If it works you will see lots of messages.

## 6. Stop The Server
Type `stop` in the console and press Enter.

Now read `LOCAL_NETWORK_SETUP.md` if you want friends on your Wi-Fi to join.
