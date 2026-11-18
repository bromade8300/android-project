param([string]$DeviceName = 'Grappe_Phone')
$ErrorActionPreference = 'Stop'

# À appeler avant le démarrage : l'émulateur lit le matériel au boot.
$avdRoot = if ($env:ANDROID_AVD_HOME) { $env:ANDROID_AVD_HOME } else { Join-Path $env:USERPROFILE '.android\avd' }
$config = Join-Path $avdRoot "$DeviceName.avd\config.ini"
if (-not (Test-Path -LiteralPath $config)) { throw "Configuration AVD introuvable : $config" }
$text = [IO.File]::ReadAllText($config)
if (-not (Test-Path -LiteralPath "$config.before-keyboard.bak")) {
    Copy-Item -LiteralPath $config -Destination "$config.before-keyboard.bak"
}
foreach ($setting in @(@('hw.keyboard', 'yes'), @('hw.keyboard.lid', 'no'))) {
    $pattern = '(?m)^' + [regex]::Escape($setting[0]) + '=.*$'
    $line = $setting[0] + '=' + $setting[1]
    if ($text -match $pattern) { $text = [regex]::Replace($text, $pattern, $line) }
    else { $text += "`r`n$line`r`n" }
}
[IO.File]::WriteAllText($config, $text, [Text.UTF8Encoding]::new($false))

# Préférences Qt de l'émulateur Windows, limitées à l'utilisateur courant.
# Les raccourcis Ctrl+A/C/V/X sont transmis au téléphone, pas à ses contrôles.
$key = 'HKCU:\Software\Android Open Source Project\Emulator\set'
New-Item -Path $key -Force | Out-Null
New-ItemProperty -Path $key -Name 'forwardShortcutsToDevice' -Value 'true' -PropertyType String -Force | Out-Null
New-ItemProperty -Path $key -Name 'clipboardSharing' -Value 'true' -PropertyType String -Force | Out-Null
Write-Output "Clavier matériel et raccourcis Android activés pour $DeviceName ; partage du presse-papiers activé."
