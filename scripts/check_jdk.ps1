<#
.SYNOPSIS
  Ensures a JDK (with javac) is available and prints JAVA_HOME.
.DESCRIPTION
  Searches common install roots for a JDK containing javac.exe (Java 17+).
  If found, prints the JAVA_HOME path. Optionally sets JAVA_HOME in the current session if -SetEnv is used.
.PARAMETER SetEnv
  If provided, sets $env:JAVA_HOME for the current shell session.
.EXAMPLE
  ./scripts/check_jdk.ps1 -SetEnv
#>
param(
    [switch]$SetEnv
)
$ErrorActionPreference = 'SilentlyContinue'
$roots = @(
  'C:\Program Files\Eclipse Adoptium',
  'C:\Program Files\Java',
  'C:\Program Files\Microsoft\jdk'
)
$best = $null
foreach ($root in $roots) {
  if (-not (Test-Path $root)) { continue }
  Get-ChildItem -Path $root -Directory | ForEach-Object {
    $home = $_.FullName
    $javac = Join-Path $home 'bin' | Join-Path -ChildPath 'javac.exe'
    $java = Join-Path $home 'bin' | Join-Path -ChildPath 'java.exe'
    if (Test-Path $javac -and Test-Path $java) {
      $verOut = & $java -version 2>&1
      if ($verOut -match 'version "(\d+)') {
        $major = [int]$matches[1]
        if ($major -ge 17) { $best = $home }
      }
    }
  }
}
if (-not $best) {
  Write-Error 'No suitable JDK (with javac) version 17+ found. Install a JDK (not just a JRE).'
  exit 1
}
if ($SetEnv) { $env:JAVA_HOME = $best }
Write-Output $best
exit 0
