# Guide complet du projet Grappe

État documenté le 13 novembre 2026. Projet situé dans `C:\Users\Antoine\Desktop\Android`.

## Documents à consulter

| Document | Contenu |
| --- | --- |
| [README](../README.md) | Démarrage rapide, commandes et fonctionnalités |
| [Architecture](ARCHITECTURE.md) | Responsabilités, schéma des couches et lecture du code |
| [Vérifications](VERIFICATION.md) | Résultats des tests et parcours restant à vérifier |
| [Accès locaux](ACCES_LOCAL.md) | Compte de démonstration, mot de passe, clé publique et paramètres du projet ; fichier exclu de Git |
| [Journal de réalisation](JOURNAL_REALISATION.md) | Actions effectuées et décisions prises |
| [Cahier des charges](CAHIER_DES_CHARGES.md) | Sujet original de l'enseignant, conservé |
| [Maquettes](maquettes.png) | Image de référence fournie par Antoine |

## Application réalisée

Grappe est un carnet de dégustation Android natif, écrit en Kotlin avec Jetpack Compose. L'interface reprend les couleurs bordeaux et crème, les titres sérif, la navigation basse et les illustrations de bouteilles des maquettes.

Les écrans permettent de se connecter, de s'inscrire, de consulter et rechercher des dégustations, d'ajouter un avis, de le modifier ou le supprimer, de modifier son profil et sa photo, de changer son mot de passe et de consulter la page À propos.

Le fil cloud est partagé entre utilisateurs connectés. Le filtre **Mes avis** sélectionne ceux de l'utilisateur courant. Un utilisateur ne peut modifier ou supprimer que ses propres avis.

Deux variantes sont disponibles :

| Variante | Identifiant Android | Stockage et connexion |
| --- | --- | --- |
| `demoDebug` | `fr.grappe.app.demo` | Stockage local persistant, bouton Explorer la démo, aucun compte requis |
| `cloudDebug` | `fr.grappe.app` | Supabase Auth, PostgreSQL et Storage ; compte requis |

Les deux variantes peuvent être installées simultanément. Leurs données Android sont séparées.

## Outils installés et emplacement

| Outil | Version ou composant utilisé | Emplacement |
| --- | --- | --- |
| Android Studio | Installation Winget `Google.AndroidStudio`, version annoncée 2026.2.2.8 | `C:\Program Files\Android\Android Studio` |
| Java | Eclipse Temurin JDK 17, installation 17.0.21.9 | `C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot` |
| Android SDK | Plateforme Android 35 et Build Tools 35.0.0 | `%LOCALAPPDATA%\Android\Sdk` |
| Command-line Tools | Gestion du SDK et des appareils virtuels | `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest` |
| ADB | Platform Tools | `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe` |
| Android Emulator | Image Android 35, Google APIs, x86_64 | `%LOCALAPPDATA%\Android\Sdk\emulator` |
| Gradle | Wrapper 8.11.1 fourni dans le projet | `gradlew.bat`, `gradlew`, `gradle/wrapper` |

Android Studio a fourni un JBR 25. Un JDK 17 séparé a été installé pour utiliser la version de Gradle choisie. Les scripts définissent `JAVA_HOME` pour leur propre exécution ; ils ne nécessitent pas de changer le Java global du PC.

La variable utilisateur Windows `ANDROID_HOME` pointe vers `%LOCALAPPDATA%\Android\Sdk`. Le chemin SDK est également enregistré dans `local.properties`.

Les licences SDK ont été acceptées lors de l'installation. L'accélération matérielle WHPX a été détectée comme disponible. Les paramètres BIOS n'ont pas été modifiés.

## Lancer et compiler

Depuis le dossier du projet :

| Fichier | Action |
| --- | --- |
| `Lancer-Grappe.cmd` | Compiler, démarrer le téléphone virtuel et ouvrir la démo locale |
| `Lancer-Grappe-Cloud.cmd` | Même parcours pour la version Supabase |
| `Ouvrir-Android-Studio.cmd` | Ouvrir le projet dans Android Studio |

Commandes PowerShell équivalentes :

```powershell
.\scripts\Run.ps1
.\scripts\Run.ps1 -Mode cloud
.\scripts\Run.ps1 -Tablet
.\scripts\Run.ps1 -Mode cloud -SkipBuild
.\scripts\Build.ps1
.\scripts\Build.ps1 -Mode cloud
```

`-SkipBuild` réutilise l'APK existant. Les scripts de lancement compilent sinon l'application et exécutent les tests métier, puis arrêtent les processus Gradle avant de démarrer Android pour libérer la mémoire.

Dans Android Studio, choisir **Temurin 17** comme Gradle JDK et sélectionner `demoDebug` ou `cloudDebug` dans Build Variants. Une configuration locale `.gradle/config.properties` et `.idea/gradle.xml` a aussi été préparée pour le JDK.

### APK générés

```text
app/build/outputs/apk/demo/debug/app-demo-debug.apk
app/build/outputs/apk/cloud/debug/app-cloud-debug.apk
```

Ce sont des APK de développement signés avec la clé de debug Android. Ils ne constituent pas une publication sur le Play Store ni une distribution Firebase.

## Appareils virtuels et clavier

| Appareil | Port ADB | Utilisation |
| --- | --- | --- |
| `Grappe_Phone` | `emulator-5554` | Téléphone Pixel 6, Android 35 |
| `Grappe_Tablet` | `emulator-5556` | Tablette Pixel Tablet, Android 35 |

Les configurations se trouvent dans `%USERPROFILE%\.android\avd`. Le lancement prévoit 1 536 Mio de mémoire et deux cœurs par émulateur. La tablette a été arrêtée après vérification pour libérer les ressources du PC.

### Correction du clavier physique

Le téléphone avait initialement `hw.keyboard=no`. Son fichier `%USERPROFILE%\.android\avd\Grappe_Phone.avd\config.ini` a été modifié :

```ini
hw.keyboard=yes
hw.keyboard.lid=no
```

Le téléphone virtuel a ensuite été redémarré. Android détecte un clavier alphabétique matériel.

### Correction des raccourcis Ctrl+C / Ctrl+V

Dans la barre de l'émulateur, ouvrir **… → Settings → General** :

- **Send keyboard shortcuts to** a été changé de **Emulator controls (default)** à **Virtual device**.
- **Enable clipboard sharing** était déjà activé et a été conservé.

Ces réglages ont été vérifiés dans l'interface des contrôles avancés. Cliquer dans un champ Android avant de coller. Pour copier, sélectionner d'abord le texte. La copie d'un mot de passe peut être volontairement limitée par le champ sécurisé.

Le routage est documenté dans la [documentation Android Emulator](https://developer.android.com/studio/run/emulator-extended-controls). La validation finale au clavier physique par Antoine reste distincte du contrôle de ces réglages.

## Organisation du code Kotlin

Les chemins ci-dessous sont relatifs à `app/src/main/java/fr/grappe/app`.

| Fichier | Responsabilité |
| --- | --- |
| `MainActivity.kt` | Point d'entrée Android, installation du contenu Compose |
| `model/Tasting.kt` | Dégustation, profil utilisateur et validation métier |
| `ui/GrappeApp.kt` | Navigation entre accueil, formulaire, profil et À propos |
| `ui/GrappeViewModel.kt` | État observable, actions utilisateur et coordination des dépôts |
| `ui/theme/GrappeTheme.kt` | Couleurs, typographies et thème Material 3 |
| `ui/components/GrappeComponents.kt` | Boutons, en-tête, avatar, étoiles et bouteille dessinée en Canvas |
| `ui/screens/AuthScreen.kt` | Connexion, inscription et entrée dans la démo locale |
| `ui/screens/HomeScreen.kt` | Fil, recherche, filtre personnel et catalogue externe |
| `ui/screens/EditorScreen.kt` | Ajout et modification, note et choix d'une photo |
| `ui/screens/ProfileScreen.kt` | Profil, changement de mot de passe, déconnexion et écran À propos |
| `data/TastingRepository.kt` | Contrat commun du stockage des dégustations |
| `data/LocalTastingRepository.kt` | Démo persistante et cache cloud, sérialisation JSON en SharedPreferences |
| `data/SupabaseTastingRepository.kt` | CRUD PostgREST, DTO SQL et téléversement des photos |
| `data/SupabaseService.kt` | Client Ktor, Auth, renouvellement des jetons, compte et erreurs HTTP |
| `data/SecureSessionStore.kt` | Chiffrement de la session avec AES-GCM et le Keystore Android |
| `data/WineApi.kt` | Chargement des suggestions depuis SampleAPIs |

Les ressources Android comprennent le manifeste, le thème système et l'icône vectorielle. Les illustrations de bouteilles de l'interface sont produites par le code Compose ; aucun service de génération d'images n'a été utilisé.

Les tests métier se trouvent dans `app/src/test/java/fr/grappe/app/model/TastingTest.kt`. Le projet comporte un seul module Android `app`, sans framework d'injection de dépendances.

## Configuration technique

| Élément | Valeur |
| --- | --- |
| Android Gradle Plugin | 8.9.2 |
| Kotlin et plugin Compose | 2.1.20 |
| Gradle | 8.11.1 |
| Java cible | 17 |
| Android minimum | API 26 — Android 8.0 |
| Android compilation et cible | API 35 |
| Compose BOM | 2025.04.01 |
| Ktor | 3.1.2 |
| Coil | 2.7.0 |

Toutes les versions de dépendances déclarées sont visibles dans `app/build.gradle.kts`. Le téléchargement initial des dépendances peut être long ; les compilations suivantes utilisent le cache Gradle.

## Base Supabase mise en place

Le projet `androidlagrappe` est hébergé à Paris. Le MCP Supabase a été utilisé pour les opérations administratives. Les paramètres de connexion et les accès sont dans [ACCES_LOCAL.md](ACCES_LOCAL.md).

### Données métier

La table `public.tastings` contient `id`, `owner_id`, `author`, `wine`, `vintage`, `color`, `region`, `rating`, `notes`, `photo_uri` et `created_at`.

L'identifiant est un UUID et `owner_id` référence `auth.users`. La suppression d'un utilisateur supprime ses dégustations par cascade. `created_at` représente un instant en millisecondes depuis l'époque Unix, comme dans le modèle Kotlin.

Les contraintes refusent notamment un nom de vin vide, un millésime hors de 1900 à 2100, une note hors de 1 à 5, une couleur inconnue et des notes de plus de 2 000 caractères. Deux index couvrent l'auteur et la date.

### Autorisations

RLS est activé. Les quatre politiques permettent de lire le fil aux utilisateurs connectés et réservent la création, la modification et la suppression à l'auteur. La modification ne permet pas de transférer un avis à un autre compte. Le rôle `anon` n'a pas accès à la table.

Les privilèges automatiques inutiles de `authenticated` ont été retirés : seuls SELECT, INSERT, UPDATE et DELETE sont accordés. RLS reste appliqué à chacune de ces opérations.

### Photos

Le bucket `photos` est public, limité aux JPEG de 5 Mio maximum. L'application compresse les photos avant le téléversement et utilise un chemin de la forme `<identifiant utilisateur>/<UUID>.jpg`.

Trois politiques limitent le dépôt, la lecture des métadonnées et la suppression au dossier du compte. Le caractère public du bucket permet le téléchargement d'une photo à partir de son URL.

### Migrations et fichiers

| Élément | Rôle |
| --- | --- |
| `supabase/schema.sql` | État SQL réutilisable de la table, du bucket et des politiques |
| `create_grappe_tastings_and_photo_storage` | Première migration enregistrée via le MCP |
| `restrict_tastings_client_privileges` | Retrait des privilèges clients automatiques inutiles |
| `supabase/tests/rls.sql` | Tests d'intégration des autorisations avec annulation des données temporaires |

## API externe, session et hors ligne

L'API externe est `https://api.sampleapis.com/wines/reds`, appelée avec Ktor depuis le bouton **Découvrir des vins**. Cette API n'utilise pas Supabase et ne nécessite pas de clé.

La session Supabase est conservée chiffrée sur l'appareil. Le jeton d'accès est renouvelé avant son expiration. La déconnexion efface la session locale.

Le cache cloud permet de relire les dernières dégustations sans réseau pour l'utilisateur déjà connecté. Les écritures cloud exigent une connexion ; il n'existe pas de file d'attente des écritures à synchroniser plus tard.

## Tests, journaux et artefacts

Les résultats détaillés sont dans [VERIFICATION.md](VERIFICATION.md). Les tests comprennent les cinq validations métier, le parcours CRUD Android local et les contrôles SQL de propriété des avis.

`scripts/SmokeTest.ps1` pilote le téléphone virtuel pour créer, modifier, relire après redémarrage et supprimer son propre avis temporaire. Ne pas utiliser l'émulateur simultanément. Les tests SQL se lancent avec le MCP Supabase en utilisant le contenu de `supabase/tests/rls.sql`.

Les rapports Gradle sont sous `app/build/reports`. Les journaux de cette installation et des vérifications sont dans `%LOCALAPPDATA%\GrappeTools` : compilation, validation, démarrage, émulateur, test CRUD et redémarrage du clavier. Ce sont des artefacts locaux, pas une infrastructure de journalisation de production.

La capture de l'accueil mobile est dans `doc/verification/accueil-mobile.png`. Les trois illustrations d'architecture initiales du sujet sont conservées dans `doc/assets`.

Graphify a généré un graphe local dans `graphify-out` : `graph.json`, `graph.html` et `GRAPH_REPORT.md`. L'extraction a porté sur le code du projet, sans fournisseur de modèle externe. Le parseur SQL optionnel n'est pas installé : le graphe ne représente pas exhaustivement le schéma SQL. Ce dossier est exclu de Git.

## Limites et travail restant

- Le compte de démonstration cloud est créé, confirmé et testé par l'API ; le parcours complet Android avec inscription réelle et réception d'e-mail reste à vérifier.
- Le téléversement et la suppression d'un fichier photo réel restent à tester depuis Android.
- Les anciennes photos remplacées ne sont pas nettoyées automatiquement dans Storage.
- Les abonnements, commentaires, likes et notifications des maquettes ne sont pas implémentés.
- Le catalogue externe répond depuis le PC ; son parcours complet dans l'application et les erreurs réseau restent à vérifier.
- Aucun APK de production, publication Play Store ou bonus iOS/web/desktop n'a été réalisé.
- Firebase Analytics, Crashlytics et App Distribution ne sont pas intégrés. Antoine a demandé Supabase à la place de Firebase ; cette substitution doit être validée avec l'enseignant.

Pour la soutenance, présenter les fonctionnalités réellement disponibles et les tests réalisés, puis utiliser le schéma d'architecture existant pour expliquer les responsabilités du code.
