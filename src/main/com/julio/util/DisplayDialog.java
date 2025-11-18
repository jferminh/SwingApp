package main.com.julio.util;

import javax.swing.*;

public class DisplayDialog {
    public static void messageInfo (String titre, Object message) {
        JOptionPane.showMessageDialog(null,
                message, titre, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void messageWarning (String titre, Object message) {
        JOptionPane.showMessageDialog(null,
                message, titre, JOptionPane.WARNING_MESSAGE);
    }

    public static void messageError (String titre, Object message) {
        JOptionPane.showMessageDialog(null,
                message, titre, JOptionPane.ERROR_MESSAGE);
    }

}
