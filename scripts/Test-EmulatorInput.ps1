# Essai sur un brouillon non enregistré. Le presse-papiers Windows est restauré.
. "$PSScriptRoot\Environment.ps1"
Add-Type -AssemblyName System.Windows.Forms
$adb = Join-Path $SdkDirectory 'platform-tools\adb.exe'
$serial = 'emulator-5554'
function Ui {
    & $adb -s $serial shell uiautomator dump /sdcard/grappe-input.xml | Out-Null
    return [xml](& $adb -s $serial shell cat /sdcard/grappe-input.xml)
}
function Tap($node) {
    if (-not $node) { throw 'Contrôle absent.' }
    $bounds = [regex]::Matches($node.bounds, '\d+') | ForEach-Object { [int]$_.Value }
    & $adb -s $serial shell input tap ([int](($bounds[0]+$bounds[2])/2)) ([int](($bounds[1]+$bounds[3])/2))
    Start-Sleep -Seconds 1
}
function FieldText {
    $tree = Ui
    return ($tree.SelectNodes('//node[@class="android.widget.EditText"]') | Select-Object -First 1).text
}
$previous = [Windows.Forms.Clipboard]::GetDataObject()
$backup = [Windows.Forms.DataObject]::new()
if ($previous) {
    foreach ($format in $previous.GetFormats($false)) {
        try { $backup.SetData($format, $previous.GetData($format, $false)) } catch { throw "Impossible de sauvegarder le format du presse-papiers : $format" }
    }
}
try {
    $tree = Ui
    Tap ($tree.SelectNodes('//node') | Where-Object text -eq 'Ajouter' | Select-Object -First 1)
    $tree = Ui
    Tap ($tree.SelectNodes('//node[@class="android.widget.EditText"]') | Select-Object -First 1)
    [Windows.Forms.Clipboard]::SetText('Copie PC : été 2026')
    Start-Sleep -Seconds 2
    & $adb -s $serial shell input keycombination 113 50
    Start-Sleep -Seconds 1
    $actual = FieldText
    if ($actual -ne 'Copie PC : été 2026') { throw "Collage PC vers Android non confirmé (longueur obtenue : $($actual.Length))." }
    Write-Output 'PC -> Android : OK (espaces, accents et chiffres)'
    & $adb -s $serial shell input keycombination 113 29
    & $adb -s $serial shell input text 'RetourAndroid42'
    & $adb -s $serial shell input keycombination 113 29
    & $adb -s $serial shell input keycombination 113 31
    Start-Sleep -Seconds 2
    if ([Windows.Forms.Clipboard]::GetText() -ne 'RetourAndroid42') { throw 'Copie Android vers PC non confirmée.' }
    Write-Output 'Android -> PC : OK'
    & $adb -s $serial shell input keyevent 61
    $tree = Ui
    if (-not ($tree.SelectNodes('//node[@class="android.widget.EditText" and @focused="true"]') | Where-Object text -eq (Get-Date).Year.ToString())) {
        throw 'Navigation Tab vers le millésime non confirmée.'
    }
    Write-Output 'Tab vers le champ suivant : OK'
} finally {
    if ($previous) { [Windows.Forms.Clipboard]::SetDataObject($backup, $true) }
    else { [Windows.Forms.Clipboard]::Clear() }
    $tree = Ui
    $cancel = $tree.SelectNodes('//node') | Where-Object text -eq 'Annuler' | Select-Object -First 1
    if ($cancel) { Tap $cancel }
}
