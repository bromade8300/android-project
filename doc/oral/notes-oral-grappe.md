# Grappe · Notes d’oral à trois

Environ 5 minutes au total. Chaque personne présente deux diapositives. Les passages ci-dessous servent de repères à l’oral.

[Ouvrir la présentation Grappe](presentation-grappe.html)

## Personne 1 — Le besoin et la bibliothèque

### Diapo 1 · Grappe

- Grappe est un carnet personnel pour garder une trace des vins dégustés.
- Chaque fiche conserve les informations du vin, une note sur 5 et un commentaire facultatif.
- Le parcours principal tient en trois écrans : **Mes vins**, **Ajouter** et **Profil**.

### Diapo 2 · Mes vins

- Montrer le champ de recherche en haut. Il filtre les vins par nom ou par région.
- Pointer une fiche. Le millésime, la couleur et la note sont visibles directement dans la liste.
- Montrer les actions de modification et de suppression. La suppression demande une confirmation.

**Transition** — « La bibliothèque rassemble les vins déjà enregistrés. Voyons comment une fiche est ajoutée et comment elle alimente le profil. »

## Personne 2 — La saisie et le profil

### Diapo 3 · Ajouter un vin

- Montrer les champs principaux : nom, millésime, couleur et note. La région et le commentaire sont facultatifs.
- Prendre un exemple simple : un rouge de 2021, noté 4 sur 5.
- Le même formulaire sert à ajouter ou à modifier un vin. En modification, les champs sont préremplis et la fiche garde son identifiant.

### Diapo 4 · Mon profil

- Pointer les trois statistiques : nombre de vins, nombre de régions et moyenne des notes.
- Les chiffres se calculent à partir des vins du compte. Une région répétée ne compte qu’une fois.
- Le profil permet aussi de modifier le nom et la photo.

**Transition** — « Derrière ces écrans, le même parcours relie la saisie, la validation et l’enregistrement. »

## Personne 3 — L’architecture et les tests

### Diapo 5 · Du formulaire au stockage

- **Kotlin** est le langage de l’application. **Jetpack Compose** construit les écrans.
- Suivre les flèches avec l’exemple du bouton **Enregistrer**. Le formulaire transmet la saisie au **ViewModel**, qui vérifie les valeurs et gère l’état de l’écran.
- Le **Repository** est le point d’accès aux données. En démo, les vins sont enregistrés sur le téléphone. En mode cloud, Supabase gère le compte et le stockage des vins.
- Après l’enregistrement, le ViewModel recharge la liste et Compose actualise l’affichage.

### Diapo 6 · Tests unitaires

- Un test unitaire vérifie une règle précise du code à partir d’un exemple et d’un résultat attendu.
- **5 tests de validation.** Exemple : une fiche avec un nom vide doit être refusée.
- **2 tests de statistiques.** Exemple : un carnet vide ne doit afficher aucune moyenne.
- **7 tests sur 7 réussis** dans chacune des variantes, démo et cloud.

**Conclusion** — « Les tests vérifient deux règles essentielles du carnet : la validité des fiches et le calcul des statistiques. »
