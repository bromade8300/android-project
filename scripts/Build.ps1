param([ValidateSet('demo', 'cloud')][string]$Mode = 'demo')
. "$PSScriptRoot\Environment.ps1"
Push-Location $ProjectDirectory
try {
    $variant = (Get-Culture).TextInfo.ToTitleCase($Mode) + 'Debug'
    & .\gradlew.bat "assemble$variant" "test${variant}UnitTest" --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'La compilation ou les tests ont échoué.' }
} finally { Pop-Location }
