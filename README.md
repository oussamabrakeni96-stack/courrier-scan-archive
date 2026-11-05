# Courier Archive

Application mobile Android pour scanner les courriers et les archiver localement dans l'archive du téléphone.

## Fonctionnalités de cette version
- scanning d'un document depuis la caméra
- saisie de la référence du courrier
- choix du type : entrant ou sortant
- association d'un courrier à un autre via une référence liée
- recherche par référence, objet, expéditeur ou relation
- stockage local des documents dans l'application

## Exemple de relation entre courriers
- un courrier entrant peut être lié à un courrier sortant via la même référence ou un numéro de dossier
- la relation est affichée dans la liste de l'archive pour faciliter le contrôle administratif

## Stack
- Kotlin
- Jetpack Compose
- Material 3

## Lancement
1. Ouvrir le projet dans Android Studio
2. Synchroniser Gradle
3. Lancer l'application sur un émulateur ou un téléphone Android

## Prochaine évolution
- export du dossier vers le PC Windows en Wi‑Fi local
- OCR pour la lecture automatique des références
- recherche avancée par date, sujet et partie liée
