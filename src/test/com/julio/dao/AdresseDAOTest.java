package test.com.julio.dao;

import main.com.julio.dao.AdresseDAO;
import main.com.julio.dao.DatabaseConnexion;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import org.junit.jupiter.api.*;
import test.com.julio.util.TestDataBuilder;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour AdresseDAO.
 * Ces tests nécessitent une connexion à la base de données de test.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdresseDAOTest {

    private static AdresseDAO adresseDAO;
    private static Integer testAdresseId;

    @BeforeAll
    static void setUpClass() throws SQLException {
        adresseDAO = new AdresseDAO();
    }

    @AfterAll
    static void tearDownClass() throws SQLException {
        DatabaseConnexion.resetInstance();
    }

    @Test
    @Order(1)
    @DisplayName("Test CREATE - Insertion d'une nouvelle adresse")
    void testCreate() throws SQLException, ValidationException {
        // Arrange
        Adresse adresse = TestDataBuilder.createAdresseSansId();

        // Act
        Adresse adresseCreee = adresseDAO.create(adresse);
        testAdresseId = adresseCreee.getId();

        // Assert
        assertNotNull(adresseCreee.getId(), "L'ID doit être généré");
        assertTrue(adresseCreee.getId() > 0, "L'ID doit être positif");
        assertEquals("12", adresseCreee.getNumeroRue());
        assertEquals("Rue de Test", adresseCreee.getNomRue());
    }

    @Test
    @Order(2)
    @DisplayName("Test FIND_BY_ID - Recherche par ID")
    void testFindById() throws SQLException {
        // Act
        Adresse adresseTrouvee = adresseDAO.findById(testAdresseId);

        // Assert
        assertNotNull(adresseTrouvee, "L'adresse doit être trouvée");
        assertEquals(testAdresseId, adresseTrouvee.getId());
        assertEquals("75001", adresseTrouvee.getCodePostal());
    }

    @Test
    @Order(3)
    @DisplayName("Test FIND_ALL - Récupération de toutes les adresses")
    void testFindAll() throws SQLException {
        // Act
        List<Adresse> adresses = adresseDAO.findAll();

        // Assert
        assertNotNull(adresses, "La liste ne doit pas être null");
        assertFalse(adresses.isEmpty(), "La liste doit contenir au moins une adresse");
    }

    @Test
    @Order(4)
    @DisplayName("Test SAVE - Mise à jour d'une adresse")
    void testSave() throws SQLException, ValidationException {
        // Arrange
        Adresse adresse = adresseDAO.findById(testAdresseId);
        adresse.setVille("Lyon");

        // Act
        boolean success = adresseDAO.save(adresse);

        // Assert
        assertTrue(success, "La mise à jour doit réussir");

        // Vérifier la modification
        Adresse adresseModifiee = adresseDAO.findById(testAdresseId);
        assertEquals("Lyon", adresseModifiee.getVille());
    }

    @Test
    @Order(5)
    @DisplayName("Test DELETE - Suppression d'une adresse")
    void testDelete() throws SQLException {
        // Act
        boolean success = adresseDAO.delete(testAdresseId);

        // Assert
        assertTrue(success, "La suppression doit réussir");

        // Vérifier que l'adresse n'existe plus
        Adresse adresseSupprimee = adresseDAO.findById(testAdresseId);
        assertNull(adresseSupprimee, "L'adresse ne doit plus exister");
    }

    @Test
    @DisplayName("Test FIND_BY_ID avec ID invalide")
    void testFindByIdInvalide() throws SQLException {
        // Act
        Adresse adresse = adresseDAO.findById(999999);

        // Assert
        assertNull(adresse, "Aucune adresse ne doit être trouvée avec un ID inexistant");
    }
}
