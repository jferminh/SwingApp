package main.com.julio.model;

/**
 * Énumération représentant le niveau d'intérêt d'un prospect.
 * <p>
 * Cette énumération permet de qualifier l'intérêt manifesté par un prospect
 * envers les services ou produits de l'entreprise. Chaque valeur possède
 * un libellé lisible pour l'affichage dans l'interface utilisateur.
 * </p>
 * <p>
 * Valeurs possibles :
 * </p>
 * <ul>
 *   <li>{@link #OUI} - Le prospect a manifesté un intérêt</li>
 *   <li>{@link #NON} - Le prospect n'a pas manifesté d'intérêt</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Prospect
 */
public enum Interesse {

    /** Le prospect est intéressé par les services/produits proposés */
    OUI("Oui"),

    /** Le prospect n'est pas intéressé par les services/produits proposés */
    NON("Non");

    /** Libellé textuel de l'intérêt pour l'affichage utilisateur */
    private final String libelle;

    /**
     * Constructeur privé de l'énumération.
     * <p>
     * Initialise chaque constante avec son libellé associé.
     * </p>
     *
     * @param libelle représentation textuelle de la valeur d'intérêt
     */
    Interesse(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    /**
     * Retourne le libellé textuel de l'intérêt.
     * <p>
     * Cette méthode permet d'afficher directement l'énumération
     * dans l'interface utilisateur sans conversion supplémentaire.
     * </p>
     *
     * @return le libellé de l'intérêt ("Oui" ou "Non")
     */
    @Override
    public String toString() {
        return libelle;
    }
}
