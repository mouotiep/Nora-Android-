# Nora Cameroun 🇨🇲

Plateforme artisanale interactive d'achat, de vente et de partage vidéo (Reels) célébrant la culture camerounaise.

---

## 🚀 Compilation Automatique via GitHub Actions

Un workflow GitHub Actions est prêt dans le dépôt (`.github/workflows/android.yml`).

### Procédure en 2 étapes :

1. **Poussez vos modifications sur la branche principale :**
   ```bash
   git push origin main
   ```
2. **Téléchargez l'APK généré :**
   Rendez-vous dans l'onglet **Actions** de votre dépôt GitHub, cliquez sur le dernier build exécuté, puis téléchargez l'artefact **`Nora-Cameroun-Debug-APK`**.

*(Le workflow peut également être déclenché manuellement depuis l'onglet Actions via le bouton **Run workflow**).*

---

## 🛠️ Compilation Locale (Commande unique)

Pour compiler le projet en local avec Gradle :

```bash
gradle assembleDebug
```

L'APK sera généré dans :
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📋 Prérequis & Configuration
- **Java JDK :** Version 17
- **Fichier de configuration :** Les variables d'environnement sont automatiquement chargées depuis `.env` (ou `.env.example` en cas d'absence).
- **Clef de signature Debug :** Décodée automatiquement depuis `debug.keystore.base64` si présente.
