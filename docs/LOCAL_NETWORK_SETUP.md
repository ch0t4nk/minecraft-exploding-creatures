# Let Friends On Your Wi-Fi Join

## Find Your Local IP
Run:
```
python ./scripts/detect_local_ip.py
```
It prints something like `192.168.1.25`.

## Put It In server.properties
Open `server/server.properties` and set:
```
server-ip=YOUR_NUMBER_HERE
```
Example:
```
server-ip=192.168.1.25
```

## Port
Leave `server-port=25565`. Your friends on the **same Wi-Fi** can join using:
```
192.168.1.25:25565
```
(Use your number instead.)

## Do NOT Port Forward Yet
That is for the public internet and needs adult help.
