# Journal de réalisation

Toutes les étapes ci-dessous ont été réalisées le 13 novembre 2026. Le journal décrit les résultats, sans attribuer d'horaires précis non vérifiés.

## Cadrage

1. Lecture des maquettes fournies : accueil, publication d'une dégustation, profil et connexion, sur mobile et tablette.
2. Lecture du cahier des charges déjà présent dans `doc/CAHIER_DES_CHARGES.md` ; document original conservé.
3. Choix d'une application Kotlin native avec Compose, d'un code organisé pour une soutenance et d'une architecture MVVM légère.
4. Supabase retenu à la demande explicite d'Antoine. Les dépendances Firebase initialement envisagées ont été retirées avant la livraison ; la différence avec le sujet est documentée.

## Environnement Windows

1. Installation d'Android Studio avec Winget.
2. Installation des Android Command-line Tools, Platform Tools, SDK 35, Build Tools 35.0.0, Android Emulator et image système Google APIs x86_64.
3. Installation de Temurin JDK 17 pour la compatibilité Gradle, car le JBR fourni par Android Studio est plus récent.
4. Ajout du Gradle Wrapper 8.11.1 au projet.
5. Création des AVD `Grappe_Phone` et `Grappe_Tablet` ; vérification de l'accélération WHPX.
6. Configuration de `ANDROID_HOME`, du SDK et des scripts PowerShell de compilation et de lancement.
7. Limitation à deux workers Gradle et compilation Kotlin dans le processus Gradle pour mieux gérer la mémoire du PC.
8. Ajout de lanceurs `.cmd` pour la démo, le cloud et Android Studio.

## Application Android

1. Création du module Android, du manifeste, du thème et de l'icône.
2. Création du modèle de dégustation et de ses règles de validation.
3. Création du dépôt local et du cache, du contrat de stockage et du dépôt Supabase.
4. Implémentation de Supabase Auth, PostgREST et Storage avec Ktor.
5. Chiffrement de la session avec le Keystore Android et renouvellement des jetons.
6. Implémentation des écrans Compose, des composants réutilisables et des dispositions tablette.
7. Ajout du choix des photos, de la notation, des filtres, de la recherche et de la gestion du profil.
8. Ajout d'un catalogue de vins externe via SampleAPIs et Ktor.
9. Création des variantes `demoDebug` et `cloudDebug`.

## Vérifications et corrections

1. Correction d'un import Gradle Kotlin DSL lors de la première compilation.
2. Correction d'un attribut de thème nécessitant Android 8.1 alors que le minimum annoncé est Android 8.0.
3. Ajustement des couleurs de sélection pour conserver le bordeaux des maquettes.
4. Compilation des deux APK et exécution des cinq tests métier.
5. Analyse Android Lint sans erreur bloquante.
6. Vérification visuelle du téléphone et de la tablette.
7. Test du CRUD local, de la conservation de l'identifiant lors d'une modification et de la persistance après redémarrage.
8. Vérification de la réponse de l'API externe depuis le PC.
9. Génération et mise à jour du graphe de code Graphify, en extraction locale sans modèle externe.

## Mise en place Supabase

1. Récupération de l'identifiant du projet et de la clé publique fournis par Antoine.
2. Ajout de l'URL et de la clé publique dans `local.properties`, exclu de Git.
3. Connexion du plugin MCP Supabase par Antoine. Aucune modification de la base n'a finalement été effectuée par le navigateur.
4. Inspection du projet et confirmation de l'absence de tables métier.
5. Application de la migration créant `public.tastings`, ses index, ses contraintes, quatre politiques RLS et le bucket `photos` avec trois politiques.
6. Retrait des privilèges automatiques TRUNCATE, TRIGGER et REFERENCES du rôle client.
7. Exécution du test SQL avec deux identités temporaires, puis ROLLBACK : CRUD auteur autorisé et écritures étrangères refusées.
8. Analyse de sécurité Supabase sans problème signalé.
9. Création, à la demande d'Antoine, d'un compte cloud de démonstration Thomas Martin et de deux avis : Château des Rives et Les Hauts de Loire.
10. Vérification de la connexion par l'API et de la lecture des deux avis ; fermeture de la session utilisée pour cette vérification.

## Clavier et presse-papiers

1. Diagnostic du téléphone virtuel : clavier physique désactivé dans sa configuration.
2. Activation de `hw.keyboard=yes`, désactivation du capot clavier avec `hw.keyboard.lid=no`, puis redémarrage de l'émulateur.
3. Vérification de la présence d'un clavier matériel dans Android.
4. Diagnostic des raccourcis : ils étaient dirigés vers les contrôles de l'émulateur.
5. Passage de **Send keyboard shortcuts to** à **Virtual device** dans les réglages de l'émulateur.
6. Vérification visuelle que **Enable clipboard sharing** est activé.

## Documentation et accès

Les documents d'architecture et de vérification ont été créés puis actualisés après la mise en place du cloud. Le guide complet et ce journal regroupent le travail effectué. Le compte, son mot de passe, la clé publique et les paramètres de connexion ont été regroupés à la demande d'Antoine dans `doc/ACCES_LOCAL.md`, exclu de Git.

Deux compétences Supabase ont été installées localement à partir du dépôt officiel `supabase/agent-skills`. Le plugin Supabase connecté fournit également ses propres compétences et les outils MCP. Aucun mot de passe administrateur PostgreSQL ni clé secrète Supabase n'a été fourni ou stocké dans le projet.
