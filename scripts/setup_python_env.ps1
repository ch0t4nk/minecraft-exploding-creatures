# Creates a Python virtual environment for plugins.
# Run once (or after adding new packages): ./scripts/setup_python_env.ps1

$ErrorActionPreference = 'Stop'
$Root = Join-Path $PSScriptRoot '..'
$EnvDir = Join-Path $Root 'python-env'
$ReqFile = Join-Path $EnvDir 'requirements.txt'

if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Host 'Python is not installed or not on PATH.' -ForegroundColor Red
    exit 1
}

if (-not (Test-Path (Join-Path $EnvDir 'venv'))) {
    Write-Host 'Creating virtual environment...' -ForegroundColor Cyan
    python -m venv (Join-Path $EnvDir 'venv')
}

$VenvPython = Join-Path $EnvDir 'venv' | Join-Path -ChildPath 'Scripts' | Join-Path -ChildPath 'python.exe'

if (Test-Path $ReqFile) {
    Write-Host 'Installing Python packages...' -ForegroundColor Cyan
    & $VenvPython -m pip install --upgrade pip
    & $VenvPython -m pip install -r $ReqFile
} else {
    Write-Host 'No requirements.txt found yet.' -ForegroundColor Yellow
}

Write-Host 'Python environment ready.' -ForegroundColor Green
