<#
.SYNOPSIS
  Runs Maven with a JDK (ensures javac) and optional auto-copy of built plugin jar.
.DESCRIPTION
  Order of Java resolution:
    1. -JavaHome param
    2. JAVA_HOME env var (must contain javac)
    3. Path in config/java_path.txt (java.exe path -> derive parent)
    4. check_jdk.ps1 search
  Supports args separated by spaces or commas. Adds -AutoCopy to copy final jar to server/plugins.
.PARAMETER JavaHome
  Explicit JDK home directory.
.PARAMETER MavenHome
  Directory containing bin/mvn.cmd (default C:\Tools\apache-maven-3.9.11).
.PARAMETER MavenArgs
  Maven goals/phases. If empty defaults to clean package.
.PARAMETER Project
  Path to project (folder containing pom.xml). Default: current directory.
.PARAMETER AutoCopy
  If set, copies resulting *.jar (non -sources/-javadoc) to ../../server/plugins.
.EXAMPLE
  ./scripts/mvn_local.ps1 -Project plugins/java/BoomBarnyard -AutoCopy -MavenArgs clean package
#>
param(
  [string]$JavaHome,
  [string]$MavenHome = 'C:\Tools\apache-maven-3.9.11',
  [string[]]$MavenArgs,
  [string]$Project = '.',
  [switch]$AutoCopy
)
$ErrorActionPreference = 'Stop'
function Fail($m){ Write-Error $m; exit 1 }
function HasJavac($jdkHome){ Test-Path (Join-Path $jdkHome 'bin/javac.exe') }

# Normalize MavenArgs (handle comma separated inside single token)
if (-not $MavenArgs -or $MavenArgs.Count -eq 0) { $MavenArgs = @('clean','package') }
else {
  $expanded = @()
  foreach ($a in $MavenArgs) { $expanded += ($a -split ',') }
  $MavenArgs = $expanded | Where-Object { $_ -ne '' }
}

# Resolve JavaHome
if (-not $JavaHome) {
  if ($env:JAVA_HOME -and (HasJavac $env:JAVA_HOME)) { $JavaHome = $env:JAVA_HOME }
  else {
    $savedPathFile = Join-Path (Split-Path $PSScriptRoot -Parent) 'config/java_path.txt'
    if (Test-Path $savedPathFile) {
      $lines = Get-Content $savedPathFile | Where-Object { $_ -and -not $_.StartsWith('#') }
      $javaExe = $lines | Select-Object -First 1
      if ($javaExe -and (Test-Path $javaExe)) {
        $candidate = Split-Path (Split-Path $javaExe -Parent) -Parent
        if (HasJavac $candidate) { $JavaHome = $candidate }
      }
    }
    if (-not $JavaHome) {
      $check = Join-Path $PSScriptRoot 'check_jdk.ps1'
      if (Test-Path $check) { $JavaHome = & $check 2>$null }
    }
  }
}
if (-not $JavaHome -or -not (HasJavac $JavaHome)) { Fail 'Could not resolve a JDK with javac. Install a JDK 17+.' }
$env:JAVA_HOME = $JavaHome
Write-Host "Using JAVA_HOME=$JavaHome" -ForegroundColor Cyan

# Resolve mvn
$mvn = 'mvn'
if ($MavenHome -and (Test-Path (Join-Path $MavenHome 'bin/mvn.cmd'))) { $mvn = (Join-Path $MavenHome 'bin/mvn.cmd') }

# Move to project dir
Push-Location $Project
if (-not (Test-Path 'pom.xml')) { Pop-Location; Fail "No pom.xml in project path '$Project'" }

Write-Host "Running: $mvn $($MavenArgs -join ' ')" -ForegroundColor Yellow
& $mvn @MavenArgs
$mvnExit = $LASTEXITCODE
if ($mvnExit -ne 0) { Pop-Location; Fail "Maven exited with code $mvnExit" }

if ($AutoCopy) {
  $target = Join-Path (Get-Location) 'target'
  if (Test-Path $target) {
    $jar = Get-ChildItem $target -Filter *.jar | Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1
    if ($jar) {
      $serverPlugins = Resolve-Path (Join-Path $PSScriptRoot '..\..\server\plugins')
      if (-not (Test-Path $serverPlugins)) { New-Item -ItemType Directory -Path $serverPlugins | Out-Null }
      $dest = Join-Path $serverPlugins $jar.Name
      Copy-Item $jar.FullName $dest -Force
      Write-Host "Copied $($jar.Name) -> $dest" -ForegroundColor Green
    } else { Write-Warning 'No jar found to copy.' }
  } else { Write-Warning 'No target directory.' }
}
Pop-Location
exit 0
