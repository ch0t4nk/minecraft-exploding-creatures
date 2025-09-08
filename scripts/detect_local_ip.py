"""Print the machine's local network IP address.

This prints a likely LAN address (for example 192.168.x.x). It tries a
lightweight UDP socket connection to a public IP to determine the local
interface without sending any data.
"""
import socket

def get_local_ip():
    """Return the local IPv4 address as a string, or '127.0.0.1' on failure."""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            s.connect(('8.8.8.8', 80))
            addr = s.getsockname()[0]
        finally:
            s.close()
        return addr
    except OSError:
        return '127.0.0.1'


if __name__ == '__main__':
    print(get_local_ip())
