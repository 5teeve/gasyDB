# TODO – UI Swing (Design d’affichage amélioré)

## Objectif
Créer un **design Swing élégant et simple** pour le gasy-SGBD, version débutant, prêt à évoluer.  
Éléments principaux :
- éditeur SQL
- bouton Execute
- barre de statut
- futur emplacement pour résultats

---

## Layout principal
- `JFrame` principal avec **titre et icône**
- `JPanel` principal avec `BorderLayout`
- Couleurs et marges pour un rendu moderne

+------------------------------------------------------+
| Gasy-SGBD (JFrame) |
+------------------------------------------------------+
| |
| +----------------------------------------------+ |
| | | |
| | JTextArea (éditeur SQL) | |
| | avec JScrollPane et bord arrondi | |
| | | |
| +----------------------------------------------+ |
| |
+------------------------------------------------------+
| JButton Execute (gauche) | JLabel Statut (droite)|
| style coloré / icône | fond clair, texte gras|
+------------------------------------------------------+
---

## Composants avec embellissement (la creation de tous les composants seront des methode implementer dans la class UtilsUI)

### Centre
- `JTextArea` pour requêtes SQL
  - **Font monospace** (ex: Consolas, 14pt)
  - **Couleur de fond douce** (ex: #f5f5f5)
  - **Bord arrondi** via `BorderFactory`
  - Scroll vertical avec `JScrollPane`

### Bas (South)
- `JButton` Execute
  - Couleur de fond agréable (ex: bleu ou vert)
  - Texte blanc en gras
  - Option : petite icône pour “lancer”
- `JLabel` barre de statut
  - Fond légèrement contrasté
  - Texte en gras
  - Couleur différente selon état :
    - Vert → “Ready”
    - Rouge → “Erreur SQL”
    - Bleu → “Exécution…”

---

## Structure objet
- **Classe `MainView.java`**
  - Hérite de `JFrame`
  - Contient tout le design
  - Méthodes exposées pour le contrôleur :
    - `getSqlText()`
    - `getExecuteButton()`
    - `setStatus(String text)`

- **Classe `QueryController.java`**
  - Gère les clics et événements
  - Modifie l’UI uniquement via `MainView`

---

## Notes de design
- Layout minimaliste mais élégant
- Couleurs douces et éléments espacés
- Facile à étendre pour :
  - Ajouter `JTable` pour résultats
  - Ajouter `JTree` pour bases/tables
  - Ajouter `JMenuBar` pour options
- Design pensé pour **bonne lisibilité** et **utilisation agréable**

---

## Idée bonus
- `JTextArea` avec **placeholder clair**
- `JButton Execute` qui change de couleur quand on clique
- Barre de statut avec **icônes** pour succès / erreur
- Padding et marges autour des composants pour éviter l’effet “tassé”
