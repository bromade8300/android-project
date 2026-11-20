# Chemins communs aux scripts de compilation et de lancement.
$ErrorActionPreference = 'Stop'
$ProjectDirectory = Split-Path -Parent $PSScriptRoot
$SdkDirectory = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
$JavaDirectory = Get-ChildItem 'C:\Program Files\Eclipse Adoptium\jdk-17*' -Directory -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $JavaDirectory) {
    throw 'JDK 17 introuvable. Installe Eclipse Temurin 17, puis relance ce script.'
}
# Ces variables permettent aux scripts appelants de trouver Java, le SDK et les outils Android.
$env:JAVA_HOME = $JavaDirectory
$env:ANDROID_HOME = $SdkDirectory
$env:Path = "$JavaDirectory\bin;$SdkDirectory\platform-tools;$SdkDirectory\emulator;$env:Path"
