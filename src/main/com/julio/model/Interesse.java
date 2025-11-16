package main.com.julio.model;

public enum Interesse {
    OUI("Oui"),
    NON("Non");

    private final String libelle;

    Interesse(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
