package test.com.julio.dao;

import main.com.julio.dao.DatabaseConnexion;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe DatabaseConnection.
 */
class DatabaseConnexionTest {
    private DatabaseConnexion dbConnexion;
    @BeforeEach
    void setUp() throws SQLException {
        // Réinitialiser l'instance avant chaque test
        DatabaseConnexion.resetInstance();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (dbConnexion != null) {
            dbConnexion.closeConnection();
            DatabaseConnexion.resetInstance();
        }
    }

    @Test
    @DisplayName("Test de création de l'instance Singleton")
    void testGetInstance() throws SQLException, IOException, ClassNotFoundException {
        // Act
        dbConnexion = DatabaseConnexion.getInstance();

        // Assert
        assertNotNull(dbConnexion, "L'instance ne doit pas être null");
    }

    @Test
    @DisplayName("Test du pattern Singleton - Une seule instance")
    void testSingletonPattern() throws SQLException, IOException, ClassNotFoundException {
        // Act
        DatabaseConnexion instance1 = DatabaseConnexion.getInstance();
        DatabaseConnexion instance2 = DatabaseConnexion.getInstance();

        // Assert
        assertSame(instance1, instance2, "Les deux instances doivent être identiques (Singleton)");
    }

    @Test
    @DisplayName("Test de connexion réussie à la base de données")
    void testConnectionSuccess() throws SQLException, IOException, ClassNotFoundException {
        // Arrange & Act
        dbConnexion = DatabaseConnexion.getInstance();
        Connection connection = dbConnexion.getConnection();

        // Assert
        assertNotNull(connection, "La connexion ne doit pas être null");
        assertFalse(connection.isClosed(), "La connexion ne doit pas être fermée");
        assertTrue(dbConnexion.testConnexion(), "La connexion doit être valide");
    }

    @Test
    @DisplayName("Test de fermeture de connexion")
    void testCloseConnection() throws SQLException, IOException, ClassNotFoundException {
        // Arrange
        dbConnexion = DatabaseConnexion.getInstance();
        Connection connection = dbConnexion.getConnection();

        // Act
        dbConnexion.closeConnection();

        // Assert
        assertTrue(connection.isClosed(), "La connexion doit être fermée");
    }

    @Test
    @DisplayName("Test de reconnexion automatique")
    void testAutoReconnect() throws SQLException, IOException, ClassNotFoundException {
        // Arrange
        dbConnexion = DatabaseConnexion.getInstance();
        dbConnexion.closeConnection();

        // Act - L'instance devrait se reconnecter automatiquement
        DatabaseConnexion reconnectedInstance = DatabaseConnexion.getInstance();

        // Assert
        assertTrue(reconnectedInstance.testConnexion(),
                "La connexion doit être rétablie automatiquement");
    }

}