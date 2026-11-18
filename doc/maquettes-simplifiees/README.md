# Maquettes de Grappe - version simplifiée

Maquettes vectorielles du 17 novembre 2026, basées sur les écrans Compose actuels. Ouvrir `index.html` pour la galerie. Les SVG sont éditables dans un outil vectoriel ; le PDF de présentation se trouve dans `output/pdf/Grappe-maquettes-simplifiees.pdf` à la racine du projet.

## Périmètre

Neuf vues : bibliothèque, ajout, profil, connexion, inscription, À propos, modification et deux vues tablette. Le profil contient uniquement trois statistiques : vins bus, régions et note moyenne. Les trois vins illustratifs donnent trois régions et une moyenne de 4,3.

Connexion, inscription et modification de mot de passe appartiennent à la variante cloud. La démo locale propose à la place le bouton Explorer la démo. Les maquettes ne représentent pas une authentification de démonstration simulée.

## Parcours

Connexion ou inscription -> Mes vins -> Ajouter -> Enregistrer -> Mes vins. Le crayon ouvre le même formulaire en modification, avec conservation de l'identifiant. La corbeille demande confirmation avant suppression. Profil -> À propos -> bouton API externe. L'exemple API n'est pas ajouté au carnet.

Région et notes sont facultatives. Les états de chargement, d'erreur, de recherche vide, de carnet vide, de confirmation de suppression et les formulaires secondaires du compte ne sont pas dessinés séparément. Leurs comportements restent ceux de l'application.

## Exports

- `01-*.svg` à `09-*.svg` : vues individuelles.
- `planche-01.svg` à `planche-03.svg` : planches de présentation.
- `planche-01.png` à `planche-03.png` : aperçus rendus depuis le PDF.
- `index.html` : galerie locale, sans dépendance réseau.

La palette, les titres sérif, les six champs du vin et les trois entrées de navigation suivent l'interface existante. Les espacements sont harmonisés pour la présentation : ce sont des maquettes, pas des captures pixel à pixel. L'ancienne image `doc/maquettes.png` reste conservée comme référence historique.
