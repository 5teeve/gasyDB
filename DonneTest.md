# Script de test pour les nouvelles fonctionnalités UPDATE et ALTER TABLE

## Préparation : Créer une table et insérer des données
```
MAMORONA TABLE TestEmployees @ Age(Integer|0-120|) Name(String||) Salaire(Double|0-100000|)
AMPIDIRO @ TestEmployees(Age, Name, Salaire) IRETO (25, 'Alice', 50000)
AMPIDIRO @ TestEmployees(Age, Name, Salaire) IRETO (30, 'Bob', 60000)
AMPIDIRO @ TestEmployees(Age, Name, Salaire) IRETO (35, 'Charlie', 70000)
```

## Test 1 : UPDATE simple (sans condition)
```
OVAY @ TestEmployees AMBOARY Salaire = 55000
```
*Résultat attendu : Tous les employés ont leur salaire mis à 55000*

## Test 2 : UPDATE avec condition simple
```
OVAY @ TestEmployees AMBOARY Salaire = 65000 RAHA Age > 30
```
*Résultat attendu : Seuls les employés de plus de 30 ans ont leur salaire mis à 65000*

## Test 3 : UPDATE multiple avec condition
```
OVAY @ TestEmployees AMBOARY Age = 40, Salaire = 75000 RAHA Name = 'Alice'
```
*Résultat attendu : Alice a son âge mis à 40 et son salaire à 75000*

## Test 4 : ADD COLUMN (AMPIO COLUMN)
```
AMPIO COLUMN @ TestEmployees Email(String||)
```
*Résultat attendu : La table TestEmployees a maintenant une colonne Email*

## Test 5 : ADD COLUMN puis UPDATE
```
AMPIO COLUMN @ TestEmployees Telephone(String||)
OVAY @ TestEmployees AMBOARY Telephone = '0123456789' RAHA Name = 'Bob'
```
*Résultat attendu : La colonne Telephone est ajoutée, puis Bob a son téléphone mis à jour*

## Test 6 : DROP COLUMN (FAFAO COLUMN)
```
FAFAO COLUMN @ TestEmployees Telephone
```
*Résultat attendu : La colonne Telephone est supprimée de la table TestEmployees*

## Vérification finale : Sélectionner toutes les données
```
ALAIVO * @ TestEmployees
```
*Résultat attendu : Voir toutes les données avec les modifications appliquées*

## Nettoyage : Supprimer la table de test
```
FAFAO @ TestEmployees
```

## Instructions d'exécution :
1. Copier-coller chaque requête dans l'interface gasyDB
2. Vérifier les messages de succès/erreur
3. Confirmer que les données sont correctement modifiées
4. Utiliser ASEHOY TABLE * pour voir les tables disponibles
5. Utiliser ALAIVO * @ TestEmployees pour vérifier les données
