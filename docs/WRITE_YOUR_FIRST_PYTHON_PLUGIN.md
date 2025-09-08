# Write Your First Python Plugin

Right now the Python part is pretend. But you can practice structure.

## Folder Shape
```
plugins/python/example_plugin/
  main.py
  __init__.py
  plugin_meta.json
  requirements.txt
```

## What main.py Has
```
PLUGIN_NAME = "ExamplePythonPlugin"
PLUGIN_VERSION = "0.1.0"

def on_enable():
    print("I am starting!")

```

## Try Running It Alone
```
python plugins/python/example_plugin/main.py
```

You will see fake start/stop messages.

Later a bridge can call `on_enable()` and `on_disable()` for real.
