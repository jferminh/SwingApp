package main.com.julio.model;

/**
 * Énumération représentant le niveau d'intérêt d'un prospect.
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
public enum Interesse {
    /**
     * Le prospect a manifesté un intérêt.
     */
    OUI,

    /**
     * Le prospect n'a pas manifesté d'intérêt.
     */
    NON;

    /**
     * Retourne le libellé lisible de l'enum.
     *
     * @return "Oui" ou "Non"
     */
    public String getLibelle() {
        return this == OUI ? "Oui" : "Non";
    }

    /**
     * Convertit un entier (TINYINT) en Interesse.
     *
     * Convention MySQL TINYINT(1) :
     * - 1 = OUI
     * - 0 = NON
     *
     * @param value la valeur entière (0 ou 1)
     * @return l'enum correspondant
     * @throws IllegalArgumentException si la valeur n'est ni 0 ni 1
     */
    public static Interesse fromInt(int value) {
        if (value == 1) {
            return OUI;
        } else if (value == 0) {
            return NON;
        } else {
            throw new IllegalArgumentException(
                    "Valeur 'interesse' invalide : " + value + ". Valeurs attendues : 0 (NON) ou 1 (OUI)"
            );
        }
    }

    /**
     * Convertit l'enum en entier pour stockage en base de données.
     *
     * Convention MySQL TINYINT(1) :
     * - OUI = 1
     * - NON = 0
     *
     * @return 1 si OUI, 0 si NON
     */
    public int toInt() {
        return this == OUI ? 1 : 0;
    }

    /**
     * Convertit une chaîne en Interesse de manière sécurisée.
     *
     * @param value la valeur à convertir ("OUI", "NON", "Oui", "Non", "1", "0")
     * @return l'enum correspondant
     * @throws IllegalArgumentException si la valeur est invalide
     */
    public static Interesse fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("La valeur 'interesse' ne peut pas être null ou vide");
        }

        String normalized = value.trim().toUpperCase();

        // Gestion des valeurs textuelles
        if ("OUI".equals(normalized) || "YES".equals(normalized)) {
            return OUI;
        }
        if ("NON".equals(normalized) || "NO".equals(normalized)) {
            return NON;
        }

        // Gestion des valeurs numériques
        if ("1".equals(normalized)) {
            return OUI;
        }
        if ("0".equals(normalized)) {
            return NON;
        }

        throw new IllegalArgumentException(
                "Valeur 'interesse' invalide : '" + value + "'. Valeurs attendues : OUI, NON, 1, 0"
        );
    }
}
