package test.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link Client}.
 * <p>
 * Teste la validation métier, les setters, getters et les règles de gestion
 * des clients avec utilisation extensive de tests paramétrés.
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.1
 * @since 22/01/2026
 */
@DisplayName("Tests Client - Validation et Métier")
class ClientTest {

    private Adresse adresseValide;

    @BeforeEach
    void setUp() throws ValidationException {

        // Créer une adresse valide pour les tests
        adresseValide = new Adresse("123", "Rue de la Paix", "75001", "Paris");
    }

    // ========== TESTS CRÉATION VALIDE ==========

    @Test
    @DisplayName("Création client valide - Données minimales")
    void testCreationClientValide() throws ValidationException {
        // Arrange & Act
        Client client = new Client(
                "ACME Corporation",
                adresseValide,
                "0123456789",
                "contact@acme.fr",
                "Client premium",
                1000000L,
                50
        );

        // Assert
        assertNotNull(client);
        assertEquals("ACME Corporation", client.getRaisonSociale());
        assertEquals(1000000L, client.getChiffreAffaires());
        assertEquals(50, client.getNbEmployes());
        assertEquals("Client", client.getTypeSociete());
        assertTrue(client.getContrats().isEmpty());
    }

    @Test
    @DisplayName("Création client - Commentaire null accepté")
    void testCreationClientCommentaireNull() throws ValidationException {
        // Act
        Client client = new Client(
                "TechCorp",
                adresseValide,
                "0987654321",
                "info@techcorp.fr",
                null,  // Commentaire null
                500000L,
                25
        );

        // Assert
        assertNull(client.getCommentaires());
    }

    @Test
    @DisplayName("Création client - Chiffre affaires minimal (200)")
    void testCreationClientChiffreAffairesMinimal() throws ValidationException {
        // Act
        Client client = new Client(
                "StartupCo",
                adresseValide,
                "0111111111",
                "hello@startup.co",
                "",
                200L,  // Valeur minimale
                1
        );

        // Assert
        assertEquals(200L, client.getChiffreAffaires());
    }

    // ========== TESTS PARAMÉTRÉS - CHIFFRE AFFAIRES INVALIDE ==========

    @ParameterizedTest(name = "Chiffre affaires invalide: {0}")
    @DisplayName("Validation chiffre affaires - Valeurs invalides")
    @ValueSource(longs = {-1, 0, 100, 199})
    void testChiffreAffairesInvalide(long chiffreAffaires) {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Client(
                    "Test Corp",
                    adresseValide,
                    "0123456789",
                    "test@test.fr",
                    "",
                    chiffreAffaires,
                    10
            );
        });

        assertEquals("Le chiffre d'affaires doit être >= 200.",
                exception.getMessage());
    }

    @ParameterizedTest(name = "Chiffre affaires valide: {0}")
    @DisplayName("Validation chiffre affaires - Valeurs valides")
    @ValueSource(longs = {200, 500, 1000, 1000000, 999999999})
    void testChiffreAffairesValide(long chiffreAffaires) throws ValidationException {
        // Act
        Client client = new Client(
                "Valid Corp",
                adresseValide,
                "0123456789",
                "valid@corp.fr",
                "",
                chiffreAffaires,
                5
        );

        // Assert
        assertEquals(chiffreAffaires, client.getChiffreAffaires());
    }

    // ========== TESTS PARAMÉTRÉS - NOMBRE EMPLOYÉS INVALIDE ==========

    @ParameterizedTest(name = "Nb employés invalide: {0}")
    @DisplayName("Validation nombre employés - Valeurs invalides")
    @ValueSource(ints = {-10, -1, 0})
    void testNbEmployesInvalide(int nbEmployes) {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Client(
                    "Test Corp",
                    adresseValide,
                    "0123456789",
                    "test@test.fr",
                    "",
                    1000L,
                    nbEmployes
            );
        });

        assertEquals("Le nombre d'employés doit être >= 1",
                exception.getMessage());
    }

    @ParameterizedTest(name = "Nb employés valide: {0}")
    @DisplayName("Validation nombre employés - Valeurs valides")
    @ValueSource(ints = {1, 5, 10, 50, 100, 1000, 50000})
    void testNbEmployesValide(int nbEmployes) throws ValidationException {
        // Act
        Client client = new Client(
                "Valid Corp",
                adresseValide,
                "0123456789",
                "valid@corp.fr",
                "",
                1000L,
                nbEmployes
        );

        // Assert
        assertEquals(nbEmployes, client.getNbEmployes());
    }

    // ========== TESTS PARAMÉTRÉS - EMAIL INVALIDE ==========

    @ParameterizedTest(name = "Email invalide: \"{0}\"")
    @DisplayName("Validation email - Formats invalides")
    @NullAndEmptySource
    @ValueSource(strings = {
            "invalide",
            "@test.com",
            "test@",
            "test@.com",
            "test @domain.com",
            "test@domain",
    })
    void testEmailInvalide(String email) {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Client(
                    "Test Corp",
                    adresseValide,
                    "0123456789",
                    email,
                    "",
                    1000L,
                    10
            );
        });

        assertTrue(exception.getMessage().contains("email"));
    }

    @ParameterizedTest(name = "Email valide: \"{0}\"")
    @DisplayName("Validation email - Formats valides")
    @ValueSource(strings = {
            "simple@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "test123@test-domain.fr",
            "a@b.co"
    })
    void testEmailValide(String email) throws ValidationException {
        // Act
        Client client = new Client(
                "Valid Corp",
                adresseValide,
                "0123456789",
                email,
                "",
                1000L,
                5
        );

        // Assert
        assertEquals(email, client.getEmail());
    }

    // ========== TESTS PARAMÉTRÉS - TÉLÉPHONE INVALIDE ==========

    @ParameterizedTest(name = "Téléphone invalide: \"{0}\"")
    @DisplayName("Validation téléphone - Formats invalides")
    @NullAndEmptySource
    @ValueSource(strings = {
            "123",
            "abcdefghij",
            "012345678",       // 9 chiffres
            "012345678901",    // 12 chiffres
    })
    void testTelephoneInvalide(String telephone) {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Client(
                    "Test Corp",
                    adresseValide,
                    telephone,
                    "test@test.fr",
                    "",
                    1000L,
                    10
            );
        });

        assertTrue(exception.getMessage().contains("téléphone"));
    }

    @ParameterizedTest(name = "Téléphone valide: \"{0}\"")
    @DisplayName("Validation téléphone - Formats valides")
    @ValueSource(strings = {
            "0123456789",
            "0987654321",
            "0600000000",
            "0711111111"
    })
    void testTelephoneValide(String telephone) throws ValidationException {
        // Act
        Client client = new Client(
                "Valid Corp",
                adresseValide,
                telephone,
                "valid@corp.fr",
                "",
                1000L,
                5
        );

        // Assert
        assertEquals(telephone, client.getTelephone());
    }

    // ========== TESTS PARAMÉTRÉS - RAISON SOCIALE INVALIDE ==========

    @ParameterizedTest(name = "Raison sociale invalide: \"{0}\"")
    @DisplayName("Validation raison sociale - Valeurs invalides")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testRaisonSocialeInvalide(String raisonSociale) {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Client(
                    raisonSociale,
                    adresseValide,
                    "0123456789",
                    "test@test.fr",
                    "",
                    1000L,
                    10
            );
        });

        assertTrue(exception.getMessage().contains("raison sociale"));
    }

    // ========== TESTS MÉTHODE SOURCE ==========

    @ParameterizedTest(name = "Test données complètes: {0}")
    @DisplayName("Création clients - Jeu de données complet")
    @MethodSource("provideClientData")
    void testCreationClientsComplets(String raison, String tel, String email,
                                     long ca, int nbEmp) throws ValidationException {
        // Act
        Client client = new Client(raison, adresseValide, tel, email, "", ca, nbEmp);

        // Assert
        assertEquals(raison, client.getRaisonSociale());
        assertEquals(tel, client.getTelephone());
        assertEquals(email, client.getEmail());
        assertEquals(ca, client.getChiffreAffaires());
        assertEquals(nbEmp, client.getNbEmployes());
    }

    static Stream<Arguments> provideClientData() {
        return Stream.of(
                Arguments.of("ACME Corp", "0123456789", "acme@test.fr", 1000000L, 50),
                Arguments.of("TechStart", "0987654321", "info@tech.com", 200L, 1),
                Arguments.of("MegaCorp", "0600000000", "contact@mega.fr", 999999999L, 10000),
                Arguments.of("SmallBiz", "0711111111", "hello@small.biz", 500L, 2)
        );
    }

    // ========== TESTS SETTERS ==========

    @Test
    @DisplayName("Setter chiffre affaires - Modification valide")
    void testSetChiffreAffairesValide() throws ValidationException {
        // Arrange
        Client client = new Client("Test", adresseValide, "0123456789",
                "test@test.fr", "", 1000L, 5);

        // Act
        client.setChiffreAffaires(2000000L);

        // Assert
        assertEquals(2000000L, client.getChiffreAffaires());
    }

    @Test
    @DisplayName("Setter chiffre affaires - Modification invalide")
    void testSetChiffreAffairesInvalide() throws ValidationException {
        // Arrange
        Client client = new Client("Test", adresseValide, "0123456789",
                "test@test.fr", "", 1000L, 5);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            client.setChiffreAffaires(100L);
        });
    }

    @Test
    @DisplayName("Setter nb employés - Modification valide")
    void testSetNbEmployesValide() throws ValidationException {
        // Arrange
        Client client = new Client("Test", adresseValide, "0123456789",
                "test@test.fr", "", 1000L, 5);

        // Act
        client.setNbEmployes(100);

        // Assert
        assertEquals(100, client.getNbEmployes());
    }

    @Test
    @DisplayName("Setter nb employés - Modification invalide")
    void testSetNbEmployesInvalide() throws ValidationException {
        // Arrange
        Client client = new Client("Test", adresseValide, "0123456789",
                "test@test.fr", "", 1000L, 5);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            client.setNbEmployes(0);
        });
    }

    // ========== TESTS ADRESSE NULL ==========

    @Test
    @DisplayName("Création client - Adresse null rejetée")
    void testCreationClientAdresseNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Client(
                    "Test Corp",
                    null,  // Adresse null
                    "0123456789",
                    "test@test.fr",
                    "",
                    1000L,
                    10
            );
        });

        assertTrue(exception.getMessage().contains("adresse"));
    }

    // ========== TESTS GESTION CONTRATS ==========

    @Test
    @DisplayName("Gestion contrats - Liste initialement vide")
    void testContratsListeVide() throws ValidationException {
        // Arrange
        Client client = new Client("Test", adresseValide, "0123456789",
                "test@test.fr", "", 1000L, 5);

        // Assert
        assertNotNull(client.getContrats());
        assertTrue(client.getContrats().isEmpty());
    }

    // ========== TESTS toString() ET getTypeSociete() ==========

    @Test
    @DisplayName("toString() retourne format attendu")
    void testToString() throws ValidationException {
        // Arrange
        Client client = new Client("ACME Corp", adresseValide, "0123456789",
                "test@test.fr", "", 1000L, 5);

        // Act
        String result = client.toString();

        // Assert
        assertEquals("ACME Corp (Client)", result);
    }

    @Test
    @DisplayName("getTypeSociete() retourne 'Client'")
    void testGetTypeSociete() throws ValidationException {
        // Arrange
        Client client = new Client("Test", adresseValide, "0123456789",
                "test@test.fr", "", 1000L, 5);

        // Act & Assert
        assertEquals("Client", client.getTypeSociete());
    }
}
