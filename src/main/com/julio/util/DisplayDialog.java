package main.com.julio.util;

import javax.swing.*;

/**
 * Classe utilitaire pour l'affichage de boîtes de dialogue.
 * Simplifie l'utilisation de JOptionPane avec des méthodes statiques.
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
public class DisplayDialog {

    /**
     * Affiche un message d'information.
     *
     * @param titre titre de la boîte de dialogue
     * @param message contenu du message
     */
    public static void messageInfo(String titre, Object message) {
        JOptionPane.showMessageDialog(null, message, titre, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Affiche un message d'avertissement.
     *
     * @param titre titre de la boîte de dialogue
     * @param message contenu du message
     */
    public static void messageWarning(String titre, Object message) {
        JOptionPane.showMessageDialog(null, message, titre, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Affiche un message d'erreur.
     *
     * @param titre titre de la boîte de dialogue
     * @param message contenu du message
     */
    public static void messageError(String titre, Object message) {
        JOptionPane.showMessageDialog(null, message, titre, JOptionPane.ERROR_MESSAGE);
    }
}

