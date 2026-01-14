package main.com.julio.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * Classe singleton pour gérer la connexion à la base de données MySQL.
 * <p>
 * Cette classe implémente le design pattern Singleton pour garantir qu'une seule
 * instance de connexion existe dans toute l'application. Elle charge la configuration
 * depuis le fichier database.properties et gère les erreurs de connexion avec des logs.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 13/01/2026
 */
public class DatabaseConnexion {
    // Instance unique (Singleton)
    private static DatabaseConnexion instance;

    // Objet Connexion
    private Connection connexion;

    // Propriétés de configuration
    private String url;
    private String username;
    private String password;
    private String driver;

    /**
     * Constructeur privé pour empêcher l'instanciation directe (pattern Singleton).
     * Charge la configuration depuis database.properties et initialise la connexion.
     *
     * @throws SQLException si la connexion échoue
     * @throws IOException si le fichier de configuration est introuvable
     * @throws ClassNotFoundException si le driver JDBC n'est pas trouvé
     */
    private DatabaseConnexion() throws SQLException, IOException, ClassNotFoundException {
        loadProperties();
        connect();
    }

    /**
     * Charge les propriétés de configuration depuis le fichier database.properties.
     *
     * @throws IOException si le fichier est introuvable ou illisible
     */
    private void loadProperties() throws IOException {
        Properties properties = new Properties();

        try (InputStream input = this.getClass().getClassLoader().
                getResourceAsStream("database.properties")) {

            if (input == null) {
                String errorMsg = "Fichier database.properties introuvable";
                LOGGER.log(Level.SEVERE, errorMsg);
                throw new IOException(errorMsg);
            }

            // Charger les propriétés
            properties.load(input);

            // Récupérer les valeurs
            this.url = properties.getProperty("db.url");
            this.username = properties.getProperty("db.username");
            this.password = properties.getProperty("db.password");
            this.driver = properties.getProperty("db.driver");

            LOGGER.log(Level.INFO, "Configuration de la base de données chargée avec succès");

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de database.properties", ex);
            throw ex;
        }
    }

    /**
     * Établit la connexion à la base de données MySQL.
     *
     * @throws SQLException si la connexion échoue
     * @throws ClassNotFoundException si le driver JDBC n'est pas trouvé
     */
    private void connect() throws SQLException, ClassNotFoundException {
        try {
            // Charger le driver JDBC
            Class.forName(driver);

            // Établir la connexion
            this.connexion = DriverManager.getConnection(url, username, password);

            LOGGER.log(Level.INFO, "Connexion à la base de données établie avec succès");

        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Driver JDBC introuvable", ex);
            throw ex;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la connexion à la base de données", e);
            throw e;
        }
    }

    /**
     * Retourne l'instance unique de DatabaseConnection (pattern Singleton).
     * Si l'instance n'existe pas, elle est créée. Si la connexion est fermée,
     * elle est rouverte automatiquement.
     *
     * @return l'instance unique de DatabaseConnection
     * @throws SQLException si la connexion échoue
     */
    public static DatabaseConnexion getInstance() throws SQLException {
        try {
            if (instance == null) {
                synchronized (DatabaseConnexion.class) {
                    if (instance == null) {
                        instance = new DatabaseConnexion();
                    }
                }
            } else {
                // Vérifier si la connexion est toujours active
                if (instance.connexion.isClosed()) {
                    LOGGER.log(Level.WARNING, "Connexion fermée, reconnexion en cours...");
                    instance.connect();
                }
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Impossible de créer l'instance DatabaseConnexion", e);
            throw new SQLException("Erreur initialization DatabaseConnexion", e);
        }
        return instance;
    }

    /**
     * Retourne l'objet Connection pour exécuter des requêtes SQL.
     *
     * @return l'objet Connection JDBC
     */
    public Connection getConnection() {
        return connexion;
    }

    public void closeConnection() throws SQLException {
        try {
            if (connexion != null && !connexion.isClosed()) {
                connexion.close();
                LOGGER.log(Level.INFO, "Connexion à la base de données fermée");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", ex);
            throw ex;
        }
    }

    /**
     * Réinitialise l'instance (utile pour les tests unitaires).
     * ⚠️ À utiliser uniquement dans les tests.
     */
    public static void resetInstance() throws SQLException {
        if (instance != null) {
            instance.closeConnection();
            instance = null;
        }
    }

    /**
     * Teste la connexion à la base de données.
     *
     * @return true si la connexion est active, false sinon
     */
    public boolean testConnexion() {
        try {
            return connexion != null && !connexion.isClosed() && connexion.isValid(5);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Test de connexion échoué", ex);
            return false;
        }
    }

}
