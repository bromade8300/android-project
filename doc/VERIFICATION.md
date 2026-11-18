# Vérification de la version simplifiée — 17 novembre 2026

- Compilation démo et cloud réussie ; sept tests unitaires réussis dans chaque variante (cinq validations et deux tests de statistiques).
- Dernière version démo recompilée après limitation de l'exemple API à un seul vin.
- Parcours `SmokeTest.ps1` réussi sur `emulator-5554` : création, modification avec même identifiant, persistance après arrêt/redémarrage, suppression confirmée.
- Inspection visuelle de la bibliothèque, du profil et d'À propos sur téléphone, puis du formulaire avec une taille logique de tablette sur le même émulateur. Taille et densité d'origine rétablies ensuite. Ce contrôle ne remplace pas un essai sur une tablette physique.
- Les trois libellés de statistiques sont présents. Le profil des exemples affiche trois vins, deux régions et une moyenne de 4,3.
- Le bouton Ktor d'À propos a affiché « Emporda 2012 · Maselva » depuis SampleAPIs. Les exemples de l'API ne sont pas ajoutés au carnet.
- Le test attend le chargement après redémarrage et envoie les caractères progressivement sur l'émulateur lent.
- Android 35, Temurin 17, WHPX opérationnel. Le tutoriel de saisie au stylet de Gboard a été désactivé dans cet émulateur car il interceptait la saisie automatisée.
- L'accès cloud réel n'a pas été testé dans cette session : le clone ne contient pas les paramètres locaux Supabase. Compiler `cloudDebug` ne valide ni les identifiants ni les permissions distantes.
- Aucune migration serveur appliquée. La requête et le cache filtrent le propriétaire, mais la politique SELECT historique autorise toujours les lectures par les autres comptes connectés.
- Graphify actualisé pour le code Kotlin ; extraction SQL indisponible (`tree_sitter_sql` manquant). La documentation est contrôlée directement.

Les vérifications ci-dessous sont historiques et ne décrivent pas le nouvel accueil ni le nouveau formulaire.

---

# Vérifications effectuées le 13 novembre 2026

## Compilation et tests

- `assembleDemoDebug` : succès.
- `assembleCloudDebug` : succès, avec l'URL et la clé publique du projet Supabase fournies par Antoine.
- `testDemoDebugUnitTest` : 5 tests, 0 échec.
- `lintDemoDebug` : aucune erreur bloquante. Des avertissements proposent notamment des dépendances plus récentes et des extensions KTX facultatives.

## Exécution Android

Android Studio, Eclipse Temurin 17, Android SDK 35, Platform Tools et Android Emulator sont installés. L'accélération WHPX est disponible. Deux appareils virtuels sont configurés : `Grappe_Phone` et `Grappe_Tablet`.

Le script `scripts/SmokeTest.ps1` a terminé le parcours CRUD sur le téléphone : création, modification avec conservation de l'identifiant, lecture après arrêt et redémarrage de l'application, puis suppression confirmée et vérification du stockage. Les données générées par ce parcours sont supprimées à la fin du test. Ne pas manipuler le téléphone virtuel pendant l'exécution du script.

L'écran de bienvenue, l'accueil et le profil ont été inspectés sur le téléphone et la tablette. Les dispositions tablette utilisent la largeur disponible ; l'accueil et le formulaire prévoient deux colonnes.

L'API externe SampleAPIs a répondu avec un catalogue de vins depuis le PC. Son affichage dans l'application et les erreurs réseau restent à tester dans un parcours dédié.

## État de Supabase

Le projet `androidlagrappe` (`jnvncplvifouzqppwuhp`, région Paris) est configuré via le MCP Supabase. Deux migrations sont enregistrées : `create_grappe_tastings_and_photo_storage` et `restrict_tastings_client_privileges`.

La table `public.tastings`, ses contraintes, sa clé étrangère vers `auth.users` et ses index sont en place. Quatre politiques RLS permettent la lecture du fil aux utilisateurs connectés et réservent les écritures à l'auteur. Les rôles clients ne disposent pas de TRUNCATE, TRIGGER ou REFERENCES.

Le bucket public `photos` accepte uniquement les JPEG de 5 Mio maximum. Trois politiques limitent le dépôt, la lecture des métadonnées et la suppression au dossier de l'utilisateur. Les photos restent téléchargeables publiquement, conformément au fonctionnement du fil partagé.

Le test `supabase/tests/rls.sql`, exécuté via le MCP, a réussi : création, lecture, modification et suppression par l'auteur ; lecture partagée ; refus des écritures d'un autre utilisateur ; refus de l'usurpation du propriétaire ; validation des contraintes et refus de l'accès anonyme. Il utilise deux identités temporaires dans une transaction annulée par ROLLBACK. Aucun compte ni avis de test n'est conservé.

L'analyse de sécurité Supabase ne signale aucun problème. Les deux index nouvellement créés sont signalés comme encore inutilisés dans l'analyse de performance ; ils sont conservés pour le tri du fil et la recherche par auteur.

Un compte cloud de démonstration Thomas Martin a ensuite été créé via le MCP, avec une identité e-mail confirmée et deux dégustations. La connexion par l'API Supabase a réussi et les deux publications ont été lues avec son jeton utilisateur. La session de vérification a été fermée. Les identifiants sont conservés dans `doc/ACCES_LOCAL.md`, exclu de Git.

L'inscription depuis Android avec réception d'un e-mail et le téléversement réel d'une photo restent à tester. Les tests SQL et API ne remplacent pas ce parcours complet.

## Clavier de l'émulateur

Le clavier matériel du téléphone a été activé dans sa configuration AVD et l'appareil redémarré. Android signale la présence d'un clavier alphabétique. Les raccourcis ont été dirigés vers **Virtual device** au lieu des contrôles de l'émulateur. Le partage du presse-papiers était déjà activé. Ces réglages ont été vérifiés ; la confirmation du fonctionnement au clavier physique par Antoine reste à recueillir.
