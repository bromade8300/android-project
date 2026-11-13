# Grappe

Carnet de dégustation Android en **Kotlin + Jetpack Compose**, inspiré des maquettes mobile et tablette : bordeaux, crème, titres sérif et illustrations de bouteilles.

Documentation complète : [guide du projet](doc/GUIDE_PROJET.md), [journal de réalisation](doc/JOURNAL_REALISATION.md), [architecture](doc/ARCHITECTURE.md) et [vérifications](doc/VERIFICATION.md). Les comptes et clés demandés sont regroupés dans [les accès locaux](doc/ACCES_LOCAL.md), fichier exclu de Git.

## Lancer sur ce PC

Double-cliquer sur **Lancer-Grappe.cmd**, puis choisir **Explorer la démo**. Le premier lancement compile l'application et ouvre un téléphone Android virtuel.

Pour ouvrir le code : **Ouvrir-Android-Studio.cmd**. Choisir le JDK **Temurin 17** dans Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK. Sélectionner `demoDebug` dans Build Variants, puis Run.

```powershell
# Compiler et lancer la démo sur téléphone
.\scripts\Run.ps1

# Lancer sur tablette
.\scripts\Run.ps1 -Tablet

# Compiler et exécuter les tests métier
.\scripts\Build.ps1

# Vérifier le CRUD sur le téléphone virtuel ouvert, sans le manipuler pendant le test
.\scripts\SmokeTest.ps1
```

Les scripts utilisent le SDK dans `%LOCALAPPDATA%\Android\Sdk` et un JDK Temurin 17 installé dans `C:\Program Files\Eclipse Adoptium`.

## Fonctionnalités

- Accueil avec recherche et filtre personnel.
- CRUD des dégustations, notes de 1 à 5, couleur, millésime, région, ressenti et photo.
- Profil modifiable, photo de profil, carnet personnel, écran À propos.
- Interface adaptée aux téléphones et tablettes.
- Suggestions de vins via SampleAPIs avec Ktor, chargées à la demande.
- Variante `demo` : données persistantes sur l'appareil, sans compte.
- Variante `cloud` : Supabase Auth, PostgreSQL, Storage, renouvellement de session et cache de lecture hors ligne.

## Configurer Supabase

Sur ce PC, l'URL et la clé publique sont déjà renseignées et le schéma a été appliqué au projet `androidlagrappe` via le MCP Supabase. Double-cliquer sur **Lancer-Grappe-Cloud.cmd**, puis créer son compte depuis l'application. Les étapes ci-dessous permettent de configurer un autre projet.

1. Créer un projet dans la console Supabase et activer l'authentification e-mail/mot de passe.
2. Exécuter [supabase/schema.sql](supabase/schema.sql) dans SQL Editor.
3. Ajouter dans `local.properties`, à côté de `sdk.dir` :

```properties
supabase.url=https://VOTRE-PROJET.supabase.co
supabase.key=VOTRE_CLE_PUBLIQUE_PUBLISHABLE_OU_ANON
```

Ne jamais placer une clé `service_role` ou secrète dans l'application. La clé publique est protégée par les politiques RLS du schéma.

4. Compiler et lancer `cloudDebug` :

```powershell
.\scripts\Run.ps1 -Mode cloud
```

Si la confirmation e-mail est activée, confirmer le compte depuis l'e-mail avant la première connexion. La version cloud ne charge aucune donnée de démonstration.

## Pour la soutenance

Lire [doc/ARCHITECTURE.md](doc/ARCHITECTURE.md) pour les responsabilités des fichiers et le chemin d'une action. Le document distingue les fonctions disponibles, les limites et les exigences Firebase remplacées par Supabase à la demande d'Antoine.

Le cahier des charges original reste dans [doc/CAHIER_DES_CHARGES.md](doc/CAHIER_DES_CHARGES.md).

Les résultats et les limites des tests sont dans [doc/VERIFICATION.md](doc/VERIFICATION.md). Le graphe de code local est disponible dans `graphify-out/graph.html` (extraction AST locale sans fournisseur externe).

## Vérification manuelle

1. Explorer la démo et créer une dégustation avec une note et un ressenti.
2. La modifier depuis l'icône crayon, vérifier la conservation de l'identifiant.
3. Relancer l'application et vérifier la persistance.
4. Supprimer la dégustation après confirmation.
5. Modifier le profil, ouvrir À propos, puis se déconnecter.
6. Refaire le parcours sur tablette et tester le bouton Découvrir des vins.
7. Après configuration Supabase : inscription, confirmation e-mail, connexion, CRUD, deuxième compte sans droits d'écriture sur les avis du premier, consultation hors ligne et reconnexion.

Les cinq tests unitaires vérifient les règles métier. La compilation seule ne valide pas le serveur Supabase.