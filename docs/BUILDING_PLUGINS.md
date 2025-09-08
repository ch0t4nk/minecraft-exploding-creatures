# Building Java Plugins

This guide shows how to build the example plugins with Maven.

## Requirements
- A JDK (Java 17 or higher) installed (needs `javac`).
- Maven installed (or the local path you configured in `mvn_local.ps1`).

## Quick Build (BoomBarnyard)
From the project root:
```
pwsh -File .\scripts\mvn_local.ps1 -Project plugins/java/BoomBarnyard -AutoCopy -MavenArgs clean package
```
This will:
1. Find a JDK (uses `JAVA_HOME`, `config/java_path.txt`, or searches common folders).
2. Run `mvn clean package`.
3. Copy the built jar into `server/plugins` if `-AutoCopy` is used.

## If Maven Not on PATH
Edit `scripts/mvn_local.ps1` and adjust the default `-MavenHome` parameter to match where you unpacked Maven.

You can also pass it explicitly:
```
pwsh -File .\scripts\mvn_local.ps1 -MavenHome C:\Tools\apache-maven-3.9.11 -Project plugins/java/BoomBarnyard -AutoCopy
```

## Common Issues
| Problem | Fix |
|---------|-----|
| "No compiler is provided" | You used a JRE not a JDK. Install a JDK (Temurin 21) and re-run. |
| Maven can't find Paper API | Ensure the `papermc` repository is in the `pom.xml` (already added). |
| Jar not copied | Use `-AutoCopy` or manually copy from `target/` to `server/plugins`. |

## Adding New Plugins
1. Create a new folder under `plugins/java/YourPlugin`.
2. Copy a `pom.xml` from an existing plugin and change `artifactId` & `name`.
3. Add your `plugin.yml` in `src/main/resources`.
4. Put Java sources in `src/main/java/...`.
5. Build using the wrapper command above.

Happy coding!
