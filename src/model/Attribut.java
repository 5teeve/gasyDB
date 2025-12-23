package model;
public class Attribut {

    String nom; 
    Domaine domaine;

    public Attribut(String nom, Domaine domaine){
        this.nom = nom;
        this.domaine = domaine;
    }

    public Domaine getDomaine(){
        return this.domaine;
    }
    
    public String getNom() {
        return this.nom;
    }
}