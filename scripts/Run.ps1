param(
    [ValidateSet('demo', 'cloud')][string]$Mode = 'demo',
    [switch]$Tablet,
    [switch]$SkipBuild
)
. "$PSScriptRoot\Environment.ps1"
if (-not $SkipBuild) {
    & "$PSScriptRoot\Build.ps1" -Mode $Mode
    # Libérer la mémoire de Gradle avant de démarrer Android sur un PC de 16 Go.
    Push-Location $ProjectDirectory
    try { & .\gradlew.bat --stop } finally { Pop-Location }
}
$deviceName = if ($Tablet) { 'Grappe_Tablet' } else { 'Grappe_Phone' }
$port = if ($Tablet) { 5556 } else { 5554 }
$serial = "emulator-$port"
$adb = Join-Path $SdkDirectory 'platform-tools\adb.exe'
$emulator = Join-Path $SdkDirectory 'emulator\emulator.exe'
& $adb start-server
if ((& $adb devices) -notmatch "$serial\s+device") {
    & "$PSScriptRoot\Configure-EmulatorInput.ps1" -DeviceName $deviceName
    Start-Process -FilePath $emulator -ArgumentList @('-avd', $deviceName, '-port', $port, '-gpu', 'auto', '-memory', '1536', '-cores', '2', '-no-snapshot-load') -WindowStyle Hidden
}
$deadline = (Get-Date).AddMinutes(4)
do {
    Start-Sleep -Seconds 2
    $booted = & $adb -s $serial shell getprop sys.boot_completed 2>$null
    if ((Get-Date) -gt $deadline) { throw "L'émulateur ne répond pas. Ouvre Android Studio > Device Manager." }
} until ($booted -match '1')
# Le clavier du PC suffit ; éviter le tutoriel de stylet et le clavier tactile superposé.
& $adb -s $serial shell settings put secure stylus_handwriting_enabled 0
& $adb -s $serial shell settings put secure show_ime_with_hard_keyboard 0
$apk = Join-Path $ProjectDirectory "app\build\outputs\apk\$Mode\debug\app-$Mode-debug.apk"
& $adb -s $serial install -r $apk
if ($LASTEXITCODE -ne 0) { throw "Installation de l'APK impossible." }
$applicationId = if ($Mode -eq 'demo') { 'fr.grappe.app.demo' } else { 'fr.grappe.app' }
& $adb -s $serial shell am start -n "$applicationId/fr.grappe.app.MainActivity"
if ($LASTEXITCODE -ne 0) { throw "Lancement de l'application impossible." }
