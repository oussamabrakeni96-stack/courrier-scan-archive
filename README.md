# Courier Archive

Application Android pour scanner et archiver des courriers localement.

## Fonctions
- Scanner un courrier en plusieurs pages avec le scanner de documents Google Play services (recadrage et import galerie inclus).
- Enregistrer les pages dans le stockage privé de l'application.
- Renseigner une référence, un numéro de dossier, un objet, l'expéditeur/destinataire et une référence liée.
- Rechercher dans l'archive par numéro de dossier, référence du courrier, référence liée, objet ou partie.
- Migrer automatiquement les anciennes fiches vers le format JSON sans supprimer leurs informations existantes.

## Prérequis
- Android Studio et JDK 17
- Un appareil Android avec Google Play services à jour (le scanner n'est pas disponible sur certains appareils sans services Google).

## Lancer
1. Ouvrir le dépôt dans Android Studio et synchroniser Gradle.
2. Exécuter `app` sur un appareil Android.
3. Remplir les champs puis appuyer sur **Scanner plusieurs pages**. Dans l'interface du scanner, capturer toutes les pages puis valider.
4. Utiliser le champ de recherche pour retrouver un dossier ou un numéro.

Les documents restent dans le stockage privé de l'application sur le téléphone. Le transfert vers Windows n'est pas encore implémenté.
