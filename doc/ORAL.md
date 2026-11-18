# Préparer l'oral de Grappe

## Pitch de 20 secondes

« Grappe garde la liste des vins que j'ai bus. Je peux ajouter, consulter, modifier et supprimer un vin. Mon profil présente trois statistiques. L'application utilise Kotlin et Compose ; Supabase gère le compte et le stockage cloud. Une variante locale permet une démonstration sans réseau. »

## Démonstration courte

1. Ouvrir Mes vins et chercher une bouteille.
2. Ajouter un vin avec une note, puis le modifier.
3. Montrer le profil : nombre de vins, régions, note moyenne.
4. Depuis À propos, charger les exemples de l'API externe.
5. Revenir et supprimer le vin de démonstration après confirmation.

Préciser si la démonstration utilise `demo` ou `cloud`. Une démo locale ne prouve pas le fonctionnement du serveur.

## Les cinq idées à maîtriser

| Question | Réponse et fichier à lire |
|---|---|
| Que représente un vin ? | `Tasting` est une data class : identifiant, propriétaire et champs du formulaire. `Tasting.kt`. |
| Que se passe-t-il quand je clique Enregistrer ? | L'écran envoie le vin au ViewModel, qui valide, appelle le dépôt, recharge l'état et revient à la liste. `EditorScreen.kt`, `GrappeViewModel.kt`. |
| Comment l'écran reçoit-il les changements ? | Le ViewModel expose un `StateFlow`. `collectAsStateWithLifecycle` l'observe ; Compose recompose les fonctions concernées. `GrappeApp.kt`. |
| Pourquoi un repository ? | Même contrat `load/save/delete`, deux stockages : JSON local ou requêtes cloud. Les écrans ne connaissent pas le réseau. `TastingRepository.kt`. |
| Comment sont calculées les statistiques ? | Filtrer les vins du compte ; compter ; normaliser et dédupliquer les régions ; calculer la moyenne ou renvoyer null si vide. `WineStats.kt`. |

## Questions classiques

- **Création ou modification ?** Un nouveau vin reçoit un UUID. Une modification utilise `copy`, donc garde le même identifiant. Le dépôt remplace l'entrée de cet identifiant.
- **Pourquoi une coroutine ?** Les opérations suspendues permettent d'attendre le réseau et le stockage sans bloquer l'interface. Le stockage local s'exécute sur `Dispatchers.IO`.
- **Pourquoi `rememberSaveable` ?** Les champs survivent à une recréation de l'activité, par exemple une rotation. Les mots de passe restent dans `remember` pour éviter de les sauvegarder.
- **Quel est le rôle de Ktor ?** Envoyer des requêtes HTTP à Supabase et à SampleAPIs. La sérialisation transforme JSON et objets Kotlin.
- **Que fait la validation ?** Nom non vide et limité à 120 caractères, année entre 1900 et 2100, note entre 1 et 5, notes limitées à 2 000 caractères.
- **Hors ligne ?** Dernière bibliothèque lisible grâce au cache. Pas d'écriture cloud différée ni de synchronisation automatique de modifications hors ligne.
- **Sécurité ?** Session chiffrée avec Android Keystore et contrôle des écritures par RLS. Le filtre de liste ne remplace pas les permissions serveur. La politique de lecture historique est partagée entre comptes connectés.

## Limites à annoncer

Supabase remplace Firebase Auth/Firestore. Firebase Analytics, Crashlytics et App Distribution ne sont pas intégrés. Vérifier l'accord de l'enseignant. Les photos de profil cloud sont publiques. Les anciennes photos ne sont pas nettoyées automatiquement.

Ne pas apprendre une réponse sans ouvrir la fonction correspondante. Pour s'entraîner : suivre une seule sauvegarde, de `EditorScreen` jusqu'au dépôt puis au retour dans `StateFlow`.
