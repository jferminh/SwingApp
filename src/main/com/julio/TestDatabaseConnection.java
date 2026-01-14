package main.com.julio;

import main.com.julio.dao.DatabaseConnexion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe de test pour vérifier la connexion à la base de données.
 */
public class TestDatabaseConnection {
    static void main(String[] args) {
        System.out.println("Test de connexion à la base de données MySQL\n");

        try {
            // Récupérer l'instance Singleton
            DatabaseConnexion dbConnexion = DatabaseConnexion.getInstance();
            System.out.println("✅ Instance DatabaseConnection créée avec succès");

            // Récupérer la connexion
            Connection con = dbConnexion.getConnection();
            System.out.println("✅ Connexion obtenue : " + con.toString());

            // Tester la connexion
            if (dbConnexion.testConnexion()) {
                System.out.println("✅ Connexion à la base de données active et valide");
            } else {
                System.out.println("❌ Connexion invalide");
            }

            // Test de requête simple
            System.out.println("\nTest de requête SQL");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");

            System.out.println("Tables dans la base de données : ");
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1));
            }

            // Fermeture des ressources
            rs.close();
            stmt.close();

            // Test du Singleton
            System.out.println("\n Test de pattern Singleton");
            DatabaseConnexion dbConnexion2 = DatabaseConnexion.getInstance();
            if (dbConnexion == dbConnexion2) {
                System.out.println("✅ Pattern Singleton respecté : même instance");

            } else {
                System.out.println("❌ Erreur : instances différentes");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données :");
            System.err.println("   Message : " + e.getMessage());
            System.err.println("   Vérifiez :");
            System.err.println("   - MySQL est démarré");
            System.err.println("   - database.properties est correctement configuré");
            System.err.println("   - La base ecf_dao existe");
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
