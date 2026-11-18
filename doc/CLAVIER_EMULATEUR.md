# Clavier du PC et presse-papiers dans l'émulateur

Correction appliquée le 17 novembre 2026 au téléphone `Grappe_Phone`.

## Utilisation

Cliquer dans un champ de Grappe avant de saisir du texte. Utiliser `Ctrl+A` pour sélectionner, `Ctrl+C` pour copier et `Ctrl+V` pour coller. Tab passe au champ suivant. Le partage du presse-papiers permet de copier un texte depuis Windows vers Android et inversement.

Les raccourcis sont transmis à l'appareil virtuel : certains raccourcis de contrôle de l'émulateur sont donc remplacés par les raccourcis Android. Ses boutons latéraux restent disponibles. Le clavier tactile ne s'affiche plus automatiquement lorsqu'un clavier matériel est présent.

## Correction

`scripts/Configure-EmulatorInput.ps1` configure l'AVD avant son démarrage :

- `hw.keyboard=yes` : clavier matériel présent.
- `hw.keyboard.lid=no` : aucune fermeture de clavier simulée.
- Préférence Qt `forwardShortcutsToDevice=true` : raccourcis transmis à Android.
- Préférence Qt `clipboardSharing=true` : partage du presse-papiers Windows/Android.

Les préférences Qt sont stockées sous `HKCU\Software\Android Open Source Project\Emulator\set`. Elles concernent l'émulateur pour l'utilisateur Windows courant. Aucun réglage de sécurité Windows ni code métier Android n'est modifié.

`Run.ps1` applique ces paramètres lorsqu'il doit démarrer l'émulateur. Après le boot, il désactive le tutoriel de stylet et l'affichage automatique du clavier tactile avec les paramètres Android `stylus_handwriting_enabled=0` et `show_ime_with_hard_keyboard=0`. Si l'émulateur était déjà ouvert avant la correction, le fermer puis relancer Grappe pour appliquer le matériel et les préférences Qt.

La configuration AVD initiale est sauvegardée à côté de `config.ini`, dans `config.ini.before-keyboard.bak`. Aucun effacement des données de l'appareil n'est effectué.

## Vérification réalisée

Après redémarrage, `hardware-qemu.ini` confirme `hw.keyboard=true` et `hw.keyboard.lid=false`. Les préférences Qt relues confirment le partage du presse-papiers et la transmission des raccourcis.

`scripts/Test-EmulatorInput.ps1` a réussi sur un brouillon non enregistré :

1. Copier depuis le presse-papiers Windows puis coller dans Grappe avec l'événement Android Ctrl+V : texte avec espaces, accents et chiffres conservé.
2. Sélectionner avec Ctrl+A, remplacer, copier avec Ctrl+C : texte retrouvé dans le presse-papiers Windows.
3. Tab : focus passé au champ Millésime.
4. Restaurer les formats du presse-papiers Windows et annuler le brouillon.

Commande de test :

```powershell
pwsh -NoProfile -Sta -File .\scripts\Test-EmulatorInput.ps1
```

Ne pas manipuler l'émulateur ou le presse-papiers pendant cet essai. Il teste le transfert réel entre Windows et Android, mais injecte les combinaisons avec ADB. La saisie physique AZERTY et le passage des événements depuis la fenêtre Qt restent à confirmer manuellement : le contrôle Windows automatisé était indisponible dans cette session. Aucun succès de cette dernière étape n'est présumé.

## Références techniques

- [Android Developers : transmettre les raccourcis au périphérique virtuel](https://developer.android.com/studio/run/emulator-extended-controls).
- [Source AOSP : noms des préférences Qt du clavier et du presse-papiers](https://android.googlesource.com/platform/external/qemu/+/d92062968c9fb8cb4c9afa5a70f7422be16f64d3/android/android-emu/android/skin/qt/qt-settings.h).
