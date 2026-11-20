# Grappe · Notes d’oral à trois

Prévoir environ 3 minutes pour la démo finale et chronométrer le passage complet en répétition. La première personne introduit le projet. La deuxième présente le formulaire et le profil. La troisième présente l’architecture, l’enregistrement, les tests et la démo. Les passages ci-dessous servent de repères à l’oral.

[Ouvrir la présentation Grappe](presentation-grappe.html)

## Personne 1 — Le besoin et la bibliothèque

### Diapo 1 · Grappe

- Grappe est un carnet personnel pour garder une trace des vins dégustés.
- Chaque fiche conserve les informations du vin, une note sur 5 et un commentaire facultatif.
- Le parcours principal tient en trois écrans : **Mes vins**, **Ajouter** et **Profil**.

### Diapos 2 et 3 · L’équipe et le sommaire

- Présenter Théo pour le design de l’interface, Antoine pour le rôle de Product Owner et Louis pour le développement Android.
- Annoncer les trois parties du sommaire en une phrase.

### Diapo 4 · Besoin et priorités

- Prendre l’exemple d’un vin dont on a oublié le nom mais dont on se souvient de la région.
- Expliquer les trois priorités du carnet. La saisie doit être rapide, la recherche doit fonctionner par nom ou région, et une fiche doit rester modifiable.

### Diapo 5 · Mes vins

- Montrer le champ de recherche en haut. Il filtre les vins par nom ou par région.
- Pointer une fiche. Le millésime, la couleur et la note sont visibles directement dans la liste.
- Montrer les actions de modification et de suppression. La suppression demande une confirmation.

**Transition** — « La bibliothèque rassemble les vins déjà enregistrés. Voyons comment une fiche est ajoutée et comment elle alimente le profil. »

## Personne 2 — La saisie et le profil

### Diapo 6 · Ajouter un vin

- Montrer les champs principaux : nom, millésime, couleur et note. La région et le commentaire sont facultatifs.
- Prendre un exemple simple : un rouge de 2021, noté 4 sur 5.
- Le même formulaire sert à ajouter ou à modifier un vin. En modification, les champs sont préremplis et la fiche garde son identifiant.

### Diapo 7 · Mon profil

- Pointer les trois statistiques : nombre de vins, nombre de régions et moyenne des notes.
- Les chiffres se calculent à partir des vins du compte. Une région répétée ne compte qu’une fois.
- Le profil permet aussi de modifier le nom et la photo.

**Transition** — « Derrière ces écrans, le même parcours relie la saisie, la validation et l’enregistrement. »

## Personne 3 — L’architecture et les tests

### Diapo 8 · Architecture de l’application

- Suivre les trois blocs sur le schéma. **Jetpack Compose** affiche les écrans, le **ViewModel** gère l’état et les règles du carnet, et le **Repository** donne accès aux données.
- Montrer les deux destinations possibles. En démo, les fiches restent sur le téléphone. En cloud, Supabase Auth gère le compte et Supabase stocke les fiches. Une copie locale permet de relire les dernières fiches hors ligne.

### Diapo 9 · Du formulaire au stockage

- Reprendre le rouge de 2021 noté 4 sur 5. Quand l’utilisateur appuie sur **Enregistrer**, l’application vérifie les champs, sauvegarde la fiche et actualise la liste **Mes vins**.

### Diapo 10 · Tests unitaires

- Un test unitaire vérifie une règle précise du code à partir d’un exemple et d’un résultat attendu.
- **5 tests de validation.** Exemple : une fiche avec un nom vide doit être refusée.
- **2 tests de statistiques.** Exemple : un carnet vide ne doit afficher aucune moyenne.
- **7 tests sur 7 réussis** dans chacune des variantes, démo et cloud.

**Transition** — « On va maintenant reprendre ce parcours dans l’application, en mode démo. »

### Diapo 11 · Démo

- Ouvrir la variante démo avant l’oral. Ajouter un rouge de 2021, lui attribuer une région et le noter 4 sur 5.
- Revenir à **Mes vins** et saisir la région. Ouvrir la fiche et modifier la note.
- Ouvrir **Profil** et montrer le nombre de vins, le nombre de régions et la moyenne. Si la note arrondie ne change pas, montrer la nouvelle note sur la fiche.
- Si l’émulateur est indisponible, raconter ces étapes à partir des trois maquettes.
