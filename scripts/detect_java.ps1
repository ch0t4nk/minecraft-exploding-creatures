# Finds a Java 17 or higher installation under C:\Program Files\Java or C:\Program Files\Microsoft\jdk
# Usage: ./scripts/detect_java.ps1
# Prints the full path to java.exe if found.

$ErrorActionPreference = 'SilentlyContinue'

$SearchRoots = @(
    'C:\Program Files\Java',
    'C:\Program Files\Microsoft\jdk',
    'C:\Program Files\Eclipse Adoptium'
)

$Best = $null
foreach ($root in $SearchRoots) {
    if (-not (Test-Path $root)) { continue }
    Get-ChildItem -Path $root -Directory | ForEach-Object {
        $javaPath = Join-Path $_.FullName 'bin' | Join-Path -ChildPath 'java.exe'
        if (Test-Path $javaPath) {
            $verOut = & $javaPath -version 2>&1
            if ($verOut -match 'version "(\d+)(?:\.(\d+))?') {
                $major = [int]$matches[1]
                if ($major -ge 17) {
                    $Best = $javaPath
                }
            }
        }
    }
}

if ($Best) {
    Write-Output $Best
    exit 0
} else {
    Write-Error 'No Java 17+ found under standard folders.'
    exit 1
}
