param([ValidateSet('demo', 'cloud')][string]$Mode = 'demo')
# Compile un APK de debogage pour la variante demandee. La demo est selectionnee par defaut.
. "$PSScriptRoot\Environment.ps1"
# Gradle est lance depuis la racine du projet. finally restaure le dossier d'origine.
Push-Location $ProjectDirectory
try {
    $variant = (Get-Culture).TextInfo.ToTitleCase($Mode) + 'Debug'
    & .\gradlew.bat "assemble$variant" "test${variant}UnitTest" --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'La compilation ou les tests ont échoué.' }
} finally { Pop-Location }
