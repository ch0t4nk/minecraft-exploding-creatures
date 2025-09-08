# Downloads the latest Paper server jar and saves it as server/paper.jar
# Run: ./scripts/download_paper.ps1

$ErrorActionPreference = 'Stop'

$ApiBase = 'https://api.papermc.io/v2/projects/paper'
Write-Host 'Getting list of versions...' -ForegroundColor Cyan
try {
    $versions = Invoke-RestMethod "$ApiBase"
} catch {
    Write-Host 'Failed to reach PaperMC API.' -ForegroundColor Red
    exit 1
}
$latestVersion = $versions.versions[-1]
Write-Host "Latest version: $latestVersion" -ForegroundColor Green

Write-Host 'Getting builds...' -ForegroundColor Cyan
$builds = Invoke-RestMethod "$ApiBase/versions/$latestVersion"
$latestBuild = $builds.builds[-1]
Write-Host "Latest build: $latestBuild" -ForegroundColor Green

$jarName = "paper-$latestVersion-$latestBuild.jar"
$downloadUrl = "$ApiBase/versions/$latestVersion/builds/$latestBuild/downloads/$jarName"

$ServerDir = Join-Path $PSScriptRoot '..' | Join-Path -ChildPath 'server'
if (-not (Test-Path $ServerDir)) { New-Item -ItemType Directory -Path $ServerDir | Out-Null }
$OutPath = Join-Path $ServerDir 'paper.jar'

Write-Host "Downloading $jarName ..." -ForegroundColor Cyan
Invoke-WebRequest -Uri $downloadUrl -OutFile $OutPath

if (Test-Path $OutPath) {
    Write-Host 'Download complete: server/paper.jar' -ForegroundColor Green
} else {
    Write-Host 'Download failed.' -ForegroundColor Red
    exit 1
}
