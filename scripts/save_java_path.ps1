param(
    [Parameter(Mandatory=$true)][string]$JavaPath
)
# Saves the given Java executable path into config/java_path.txt
# Usage: ./scripts/save_java_path.ps1 -JavaPath "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot\bin\java.exe"

$ErrorActionPreference = 'Stop'

if (-not (Test-Path $JavaPath)) {
    Write-Host "Path does not exist: $JavaPath" -ForegroundColor Red
    exit 1
}

if (-not ($JavaPath.ToLower().EndsWith('java.exe'))) {
    Write-Host 'Please point directly to java.exe' -ForegroundColor Yellow
}

$ConfigDir = Join-Path $PSScriptRoot '..' | Join-Path -ChildPath 'config'
if (-not (Test-Path $ConfigDir)) { New-Item -ItemType Directory -Path $ConfigDir | Out-Null }
$OutFile = Join-Path $ConfigDir 'java_path.txt'
Set-Content -Path $OutFile -Value $JavaPath -Encoding UTF8
Write-Host "Saved Java path to: $OutFile" -ForegroundColor Green
