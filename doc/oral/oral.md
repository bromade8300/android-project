# Présentation orale – Architecture Android

« Cette diapositive présente l'architecture technique de notre application Android, c'est-à-dire la manière dont elle est organisée pour traiter et enregistrer les informations saisies par l'utilisateur.

Pour développer cette application, nous utilisons **Kotlin**.

Kotlin est un **langage de programmation**, officiellement pris en charge par Google pour le développement Android. C'est avec ce langage que nous écrivons le code de notre application.

Notre architecture repose ensuite sur trois éléments principaux.



Le premier élément est **Jetpack Compose**.

C'est un **framework d'interface graphique développé par Google**. Il permet de construire les différents écrans d'une application Android directement avec Kotlin.

Dans notre cas, nous l'utilisons pour afficher les formulaires, les boutons et les informations concernant les bouteilles de vin.

Par exemple, lorsqu'un utilisateur souhaite ajouter un vin à son carnet, Jetpack Compose permet d'afficher les champs dans lesquels il peut renseigner le nom du vin, son année ou sa région.



Le deuxième élément est le **ViewModel**.

C'est une **classe fournie par une bibliothèque Android Jetpack**, utilisée pour gérer l'état et les données nécessaires à un écran.

Son rôle est de faire le lien entre l'interface graphique et la partie qui gère les données.

Dans notre application, lorsqu'un utilisateur remplit un formulaire, le ViewModel récupère les informations et peut effectuer des vérifications.

Par exemple, il peut vérifier que le nom du vin a bien été renseigné avant de demander son enregistrement.

Il permet aussi de conserver l'état de l'écran lors de certains changements, comme une rotation du téléphone.



Le troisième élément est le **Repository**.

Contrairement aux deux précédents, ce n'est pas un framework. C'est un **pattern architectural**, autrement dit une façon d'organiser le code.

Son objectif est de centraliser l'accès aux données, qu'elles soient stockées sur le téléphone ou sur un serveur distant.

Dans notre application, lorsqu'un utilisateur ajoute un vin, le ViewModel transmet la demande au Repository, qui se charge de communiquer avec le système de stockage.

Cela évite que l'interface graphique ait à gérer directement la base de données.



Enfin, notre application prévoit deux possibilités de stockage.

**La première est le stockage local.**

Cela signifie que les informations sont enregistrées directement dans la mémoire du téléphone.

C'est la solution utilisée pour notre démonstration. Elle permet de conserver les données sur l'appareil, sans nécessiter systématiquement une connexion Internet.

**La deuxième est le stockage cloud avec Supabase.**

Supabase est une **plateforme Backend-as-a-Service, ou BaaS**.

Cela signifie qu'elle fournit des services backend déjà prêts à l'emploi, notamment une base de données PostgreSQL, des API et un système d'authentification.

Une API est une interface qui permet à deux logiciels de communiquer entre eux.

Dans notre projet, Supabase est prévu pour gérer les comptes utilisateurs et les informations concernant les vins.

L'intérêt du cloud est de conserver les données sur un serveur distant, ce qui peut notamment permettre à un utilisateur de retrouver son carnet sur plusieurs appareils.

Pour résumer, notre application suit un fonctionnement en trois étapes :

**Jetpack Compose** affiche le formulaire et permet à l'utilisateur de saisir les informations.

**ViewModel** gère ces informations et leur validation.

**Repository** assure ensuite la communication avec le système de stockage.

Cette séparation permet d'obtenir une application mieux organisée, plus facile à maintenir et à faire évoluer. »




# Présentation orale – Architecture Android

Après avoir résumer comment fonctionne notre application, je vais maintenant vous présentez les tests unitaires que nous avons écrits pour vérifier certaines règles du carnet.

Un test unitaire est une vérification automatisée qui porte sur une petite partie du code, par exemple une fonction. On lui donne des données précises, puis on vérifie que le résultat obtenu correspond au résultat attendu.

Dans notre application, nous avons écrit sept tests unitaires, répartis en deux groupes.

Le premier groupe comprend cinq tests sur la validation des fiches.

La validation consiste à vérifier que les informations saisies respectent les règles de l’application avant leur enregistrement.

Par exemple, l’un de nos tests utilise une fiche dont le nom est vide. Le résultat attendu est une erreur de validation, puisque le nom du vin est obligatoire. Nous vérifions aussi qu’une fiche correctement remplie est acceptée.

Le deuxième groupe comprend deux tests sur le calcul des statistiques du profil.

Le premier vérifie ce qui se passe lorsque le carnet est vide. Dans ce cas, le calcul renvoie une absence de moyenne. Dans l’interface, cette absence est représentée par un tiret.

Le deuxième vérifie notamment que chaque région est comptée une seule fois. Par exemple, si deux vins viennent de Bordeaux, ils comptent comme deux vins, mais comme une seule région. Ce test vérifie aussi que le calcul prend uniquement en compte les vins de l’utilisateur concerné.