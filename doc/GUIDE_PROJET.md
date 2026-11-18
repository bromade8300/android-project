# Guide du projet Grappe

Version simplifiée du 17 novembre 2026 : une bibliothèque personnelle des vins bus.

Maquettes actuelles : [galerie des neuf vues](maquettes-simplifiees/index.html), [PDF pour l'oral](../output/pdf/Grappe-maquettes-simplifiees.pdf). L'ancienne image de maquettes est conservée comme référence historique.

## Démarrer

`Lancer-Grappe.cmd` compile et ouvre la démo locale. `Lancer-Grappe-Cloud.cmd` nécessite un fichier `local.properties` configuré avec l'URL et la clé publique Supabase. Les accès sont exclus de Git.

Les scripts attendent Temurin 17 sous `C:\Program Files\Eclipse Adoptium` et le SDK Android sous `%LOCALAPPDATA%\Android\Sdk`. Le téléphone virtuel s'appelle `Grappe_Phone`.

```powershell
.\scripts\Run.ps1
.\scripts\Build.ps1
.\scripts\SmokeTest.ps1
```

Ne pas manipuler l'émulateur pendant le test CRUD.

## Application

Trois entrées : **Mes vins**, **Ajouter**, **Profil**. La bibliothèque affiche les vins du compte connecté, avec une recherche par nom ou région. Le même formulaire sert à ajouter et modifier. La suppression nécessite confirmation.

Le profil affiche trois statistiques : nombre de vins, nombre de régions, note moyenne. Il donne aussi accès à la modification du compte, à la déconnexion et à **À propos**. Le bouton API externe se trouve dans À propos.

`demoDebug` conserve les données sur l'appareil sans créer de compte. `cloudDebug` utilise Supabase Auth, PostgreSQL et Storage pour la photo de profil, avec cache de lecture hors ligne. Les anciens champs de photos de vins restent compatibles mais ne sont plus édités ni affichés.

## Pour l'oral

1. Lire [ORAL.md](ORAL.md) : pitch, démonstration et questions de code.
2. Lire [ARCHITECTURE.md](ARCHITECTURE.md) : chemin d'une sauvegarde et responsabilités.
3. Vérifier les limites dans [VERIFICATION.md](VERIFICATION.md).
4. Comparer avec le [sujet original](CAHIER_DES_CHARGES.md), conservé sans modification.

Les exigences d'authentification, CRUD cloud, API Ktor, À propos, gestion du compte et adaptation aux écrans restent présentes. Les intégrations Firebase Analytics, Crashlytics et App Distribution étaient absentes et restent à traiter si exigées. L'alternative Supabase à Auth/Firestore doit être acceptée par l'enseignant.

Le [journal](JOURNAL_REALISATION.md) et les anciennes vérifications décrivent aussi la version antérieure avec fil partagé. L'architecture et ce guide décrivent la version actuelle. Le filtrage de la bibliothèque n'est pas une migration des permissions de lecture serveur.
