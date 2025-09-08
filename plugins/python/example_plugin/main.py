"""Example Python plugin.

This is a pretend plugin script. In a real setup a bridge would let Python
listen to Minecraft events. This file demonstrates the minimal structure.
"""

PLUGIN_NAME = "ExamplePythonPlugin"
PLUGIN_VERSION = "0.1.0"


def on_enable():
    """Called when the plugin is enabled (simulated here)."""
    print(f"[PYTHON PLUGIN] {PLUGIN_NAME} v{PLUGIN_VERSION} is starting up!")


def on_disable():
    """Called when the plugin is disabled (simulated here)."""
    print(f"[PYTHON PLUGIN] {PLUGIN_NAME} is shutting down. Bye!")


if __name__ == "__main__":
    # Simulate enable/disable cycle
    on_enable()
    on_disable()
