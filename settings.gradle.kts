// Gradle cherche les plugins de compilation dans ces dépôts.
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
// Les bibliothèques utilisent les dépôts communs définis ici.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Grappe"
// Déclare app comme module du projet.
include(":app")
