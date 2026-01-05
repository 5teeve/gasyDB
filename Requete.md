
## SELECT (ALAIVO)
ALAIVO * @ Employes
ALAIVO Age, Name @ Employes RAHA Age > 30
ALAIVO Name, Salaire @ Employes RAHA Salaire > 50000 AND Age < 40

## INSERT (AMPIDIRO)
AMPIDIRO @ Employes IRETO ('Alice', 25, 60000)
AMPIDIRO @ Employes(Name, Age) IRETO ('Bob', 30)
AMPIDIRO @ Employes(Age, Name, Salaire) IRETO (30, 'Alice', 50000)

## CREATE (MAMORONA)
MAMORONA TABLE Employes @ Age(Integer|0-120|) Name(String||) Salaire(Double|0-100000|)
MAMORONA DATABASE Entreprise
MAMORONA TABLE Clients @ Nom(String||) Email(String||)

## CREATE DOMAIN (MAMORONA DOMAINE)
MAMORONA DOMAINE AgeDomain @ Integer|0-120|
MAMORONA DOMAINE StatusDomain @ String||ACTIF,INACTIF,SUSPENDU
MAMORONA DOMAINE SalaireDomain @ Double|0-1000000|

## Utilisation des domaines dans les tables
MAMORONA TABLE Employes @ Age(AgeDomain) Name(String||) Salaire(SalaireDomain) Status(StatusDomain)

## SHOW (ASEHOY)
ASEHOY TABLE *
ASEHOY TABLE Employes
ASEHOY DATABASE *
ASEHOY DATABASE Entreprise
ASEHOY DOMAINE *

## DELETE (FAFAO)
FAFAO @ Employes
FAFAO DATABASE TestDB

## UPDATE (OVAY)
OVAY @ Employes AMBOARY Salaire = 60000
OVAY @ Employes AMBOARY Age = 35, Salaire = 70000 RAHA Name = 'Alice'
OVAY @ Employes AMBOARY Name = 'Jean' RAHA Age > 25 AND Salaire < 50000

## ALTER TABLE (AMPIO/FAFAO COLUMN)
AMPIO COLUMN @ Employes Telephone(String||)
AMPIO COLUMN @ Employes Email(String||)
FAFAO COLUMN @ Employes Telephone
FAFAO COLUMN @ Employes Email

## SHOW (ASEHOY)
ASEHOY TABLE *
ASEHOY TABLE Employes
ASEHOY DATABASE *
ASEHOY DATABASE Entreprise

## USE (AMPIASAO)
AMPIASAO Entreprise
AMPIASAO TestDB
