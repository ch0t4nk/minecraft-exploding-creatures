param(
    [switch]$AgreeEula,
    [switch]$AutoFindJava,
    [string]$JavaPath
)

# This script starts the Minecraft Paper server.
# It also makes sure the Python virtual environment is ready.
# Run in PowerShell:  ./scripts/start_server.ps1 -AgreeEula

$ErrorActionPreference = 'Stop'

$ServerDir = Join-Path $PSScriptRoot '..' | Join-Path -ChildPath 'server'
$JarPath = Join-Path $ServerDir 'paper.jar'
if (-not (Test-Path $JarPath)) {
    Write-Host 'Missing paper.jar. Download Paper from https://papermc.io/downloads and place it in the server folder.' -ForegroundColor Red
    exit 1
}

$EulaFile = Join-Path $ServerDir 'eula.txt'
if ($AgreeEula) {
    if (Test-Path $EulaFile) {
        (Get-Content $EulaFile) -replace 'eula=false','eula=true' | Set-Content $EulaFile
        Write-Host 'EULA set to true.' -ForegroundColor Yellow
    }
}

# Decide which java to use
$JavaCmd = 'java'
if ($JavaPath) {
    if (Test-Path $JavaPath) {
        Write-Host "Using provided Java path: $JavaPath" -ForegroundColor Cyan
        $JavaCmd = $JavaPath
    } else {
        Write-Host "Provided -JavaPath not found: $JavaPath" -ForegroundColor Red
    }
}
elseif (-not $JavaPath) {
    # Try loading saved path
    $SavedPathFile = Join-Path $PSScriptRoot '..' | Join-Path -ChildPath 'config' | Join-Path -ChildPath 'java_path.txt'
    if (Test-Path $SavedPathFile) {
        $saved = (Get-Content $SavedPathFile | Where-Object { $_ -and -not $_.StartsWith('#') } | Select-Object -First 1)
        if ($saved -and (Test-Path $saved)) {
            Write-Host "Using saved Java path: $saved" -ForegroundColor Cyan
            $JavaCmd = $saved
        }
    }
}
if ($AutoFindJava) {
    # Check current java version first
    $currentVer = (& $JavaCmd -version 2>&1) | Select-String 'version "(\d+)' | ForEach-Object { $_.Matches[0].Groups[1].Value } | Select-Object -First 1
    $needFind = $true
    if ($currentVer) {
        if ([int]$currentVer -ge 17) { $needFind = $false }
    }
    if ($needFind) {
        $DetectScript = Join-Path $PSScriptRoot 'detect_java.ps1'
        if (Test-Path $DetectScript) {
            $found = & $DetectScript 2>$null
            if ($LASTEXITCODE -eq 0 -and $found) {
                Write-Host "Using detected Java: $found" -ForegroundColor Cyan
                $JavaCmd = $found
            } else {
                Write-Host 'Could not auto-find Java 17+. Still trying system java...' -ForegroundColor Yellow
            }
        } else {
            Write-Host 'detect_java.ps1 not found.' -ForegroundColor Yellow
        }
    }
}

Push-Location $ServerDir
Write-Host 'Starting Minecraft server...' -ForegroundColor Cyan
# Auto-clean older BoomBarnyard plugin jars (keep highest version)
try {
    $pluginDir = Join-Path $ServerDir 'plugins'
    if (Test-Path $pluginDir) {
        $bbJars = Get-ChildItem $pluginDir -Filter 'boombarnyard-*.jar' -ErrorAction SilentlyContinue | Sort-Object Name
        if ($bbJars.Count -gt 1) {
            $keep = $bbJars[-1]
            foreach ($j in $bbJars[0..($bbJars.Count-2)]) { Remove-Item $j.FullName -ErrorAction SilentlyContinue }
            Write-Host "BoomBarnyard cleanup: kept $($keep.Name), removed $($bbJars.Count-1) older jar(s)." -ForegroundColor Yellow
        }
        $remapDir = Join-Path $pluginDir '.paper-remapped'
        if (Test-Path $remapDir) {
            $remapJars = Get-ChildItem $remapDir -Filter 'boombarnyard-*.jar' -ErrorAction SilentlyContinue | Sort-Object Name
            if ($remapJars.Count -gt 1) {
                $keepR = $remapJars[-1]
                foreach ($r in $remapJars[0..($remapJars.Count-2)]) { Remove-Item $r.FullName -ErrorAction SilentlyContinue }
                Write-Host "BoomBarnyard remap cleanup: kept $($keepR.Name), removed $($remapJars.Count-1) remapped older jar(s)." -ForegroundColor Yellow
            }
        }
    }
} catch { Write-Host "BoomBarnyard cleanup warning: $($_.Exception.Message)" -ForegroundColor DarkYellow }
# Tweak memory sizes if you want (1G = 1024M)
& $JavaCmd -Xms1G -Xmx2G -jar $JarPath nogui
Pop-Location
