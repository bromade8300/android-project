# Test du parcours CRUD sur l'émulateur démo. Il crée puis supprime sa propre dégustation.
. "$PSScriptRoot\Environment.ps1"
$adb = Join-Path $SdkDirectory 'platform-tools\adb.exe'
$serial = 'emulator-5554'
$testName = 'Verification_' + (Get-Date -Format 'HHmmss')

function Read-Ui {
    for ($attempt = 0; $attempt -lt 5; $attempt++) {
        $result = & $adb -s $serial shell uiautomator dump /sdcard/grappe-test.xml 2>&1
        if ($result -match 'dumped to') {
            return [xml](& $adb -s $serial shell cat /sdcard/grappe-test.xml)
        }
        Start-Sleep -Seconds 1
    }
    throw "L'interface Android ne répond pas."
}

function Tap-Node($node) {
    if (-not $node) { throw "Élément attendu absent de l'écran." }
    $coordinates = [regex]::Matches($node.bounds, '\d+') | ForEach-Object { [int]$_.Value }
    $x = [int](($coordinates[0] + $coordinates[2]) / 2)
    $y = [int](($coordinates[1] + $coordinates[3]) / 2)
    & $adb -s $serial shell input tap $x $y
    Start-Sleep -Milliseconds 900
}

function Tap-Text([string]$text) {
    $tree = Read-Ui
    $node = $tree.SelectNodes('//node') | Where-Object { $_.text -eq $text } | Select-Object -First 1
    if (-not $node) { throw "Texte absent : $text" }
    Tap-Node $node
}

function Read-Tastings {
    [xml]$stored = & $adb -s $serial shell run-as fr.grappe.app.demo cat shared_prefs/tastings.xml
    $stored.SelectSingleNode('/map/string[@name="demo"]').InnerText | ConvertFrom-Json
}

function Save-Editor([string]$label) {
    # Faire défiler le formulaire sans envoyer de retour Android, qui quitterait l'écran.
    for ($attempt = 0; $attempt -lt 6; $attempt++) {
        $tree = Read-Ui
        $node = $tree.SelectNodes('//node') | Where-Object { $_.text -eq $label } | Select-Object -First 1
        if ($node) { Tap-Node $node; return }
        & $adb -s $serial shell input swipe 540 1200 540 350 700
        Start-Sleep -Milliseconds 700
    }
    throw "Bouton absent : $label"
}

Tap-Text 'Publier'
$tree = Read-Ui
Tap-Node ($tree.SelectNodes('//node[@class="android.widget.EditText"]') | Select-Object -First 1)
& $adb -s $serial shell input text $testName
Save-Editor 'Partager ma dégustation'
$created = Read-Tastings | Where-Object wine -eq $testName
if (-not $created) { throw 'Création non persistée.' }
Write-Output 'CREATE : OK'

$tree = Read-Ui
Tap-Node ($tree.SelectNodes('//node') | Where-Object { $_.'content-desc' -eq "Modifier $testName" } | Select-Object -First 1)
$tree = Read-Ui
Tap-Node ($tree.SelectNodes('//node[@class="android.widget.EditText"]') | Select-Object -First 1)
& $adb -s $serial shell input keyevent 123
& $adb -s $serial shell input text '_modifie'
Save-Editor 'Enregistrer les modifications'
$updated = Read-Tastings | Where-Object wine -eq "${testName}_modifie"
if ($updated.id -ne $created.id) { throw 'Modification non persistée avec le même identifiant.' }
Write-Output 'UPDATE : OK'

& $adb -s $serial shell am force-stop fr.grappe.app.demo
& $adb -s $serial shell am start -n fr.grappe.app.demo/fr.grappe.app.MainActivity | Out-Null
Start-Sleep -Seconds 2
$tree = Read-Ui
if (-not ($tree.SelectNodes('//node') | Where-Object text -eq "${testName}_modifie")) { throw 'La donnée ne survit pas au redémarrage.' }
Write-Output 'PERSISTENCE : OK'

Tap-Node ($tree.SelectNodes('//node') | Where-Object { $_.'content-desc' -eq "Supprimer ${testName}_modifie" } | Select-Object -First 1)
Tap-Text 'Supprimer'
if (Read-Tastings | Where-Object id -eq $created.id) { throw 'Suppression non persistée.' }
Write-Output 'DELETE : OK'
