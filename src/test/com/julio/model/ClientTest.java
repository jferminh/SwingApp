package test.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.model.Societe;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test de la classe Client")
class ClientTest {
    private Adresse adresseParDefault;

    @BeforeEach
    void setUp() throws ValidationException{
        // Réinitialiser les compteurs avant chaque test
        Adresse.resetCompteur();
        Client.resetCompteur();

        // Créer une adresse par défault réutilisable
        adresseParDefault = new Adresse("10", "Rue de Nancy", "54390", "Frouard");

    }

    @AfterEach
    void tearDown() {
        adresseParDefault = null;
    }

    // ============================================================
    // TESTS CRÉATION ET VALIDATION
    // ============================================================

    @Nested
    @DisplayName("Création de clients")
    class CreationTests {

        @Test
        @DisplayName("Création d'un client valide avec toutes les données")
        void creationClientValidComplet() throws ValidationException {
            String raisonSociale = "Entreprise ABC";
            String telephone = "0123456789";
            String email = "contact@abc.fr";
            String commentaires = "Client VIP";
            long chiffreAffaires = 50000;
            int nbEmployes = 25;

            Client client = new Client(raisonSociale, adresseParDefault, telephone,
                    email, commentaires, chiffreAffaires, nbEmployes);

            assertAll("Vérification du client créé",
                    () -> assertNotNull(client, "Le client ne devrait pas être null"),
                    () -> assertEquals(1, client.getId(), "L'ID devrait être 1 (premier client"),
                    () -> assertEquals(raisonSociale, client.getRaisonSociale()),
                    () -> assertEquals(adresseParDefault, client.getAdresse()),
                    () -> assertEquals(telephone, client.getTelephone()),
                    () -> assertEquals(email, client.getEmail()),
                    () -> assertEquals(commentaires, client.getCommentaires()),
                    () -> assertEquals(chiffreAffaires, client.getChiffreAffaires()),
                    () -> assertEquals(nbEmployes, client.getNbEmployes()),
                    () -> assertEquals("Client", client.getTypeSociete()),
                    () -> assertTrue(client.getContrats().isEmpty(), "La liste de contrats devrait être vide")
            );
        }

        @Test
        @DisplayName("Création d'un client sans commentaires (optionnel)")
        void creationClientSansCommentaires() throws  ValidationException {
            Client client = new Client("Entreprise Test", adresseParDefault,
                    "0123456789", "test@test.fr", "", 1000, 10);

            assertNotNull(client);
            assertEquals("", client.getCommentaires(), "Les commentaires devraient être vide");
        }

        @ParameterizedTest(name = "Chiffre d''affaires = {0} devrait être valide")
        @ValueSource(longs = {200, 201, 1000, 50000, 999999, Long.MAX_VALUE})
        @DisplayName("Création avec chiffres d'affaires valides (>= 200)")
        void creationClientAvecChiffreAffairesValide(Long chiffreAffaires) {
            assertDoesNotThrow(() ->
                            new Client("Entreprise", adresseParDefault, "0123456789",
                                    "test@test.fr", "", chiffreAffaires, 10),
                    () -> "La création avec CA= " + chiffreAffaires + "devrait réussir"
            );
        }

        @ParameterizedTest(name = "Nombre d''employés = {0} devrait être valide")
        @ValueSource(ints = {1, 2, 10, 100, 1000, Integer.MAX_VALUE})
        @DisplayName("Création avec nombres d'employés valides (> 0)")
        void creationClientAvecNbEmployesValide(int nbEmployes) {
            assertDoesNotThrow(() ->
                            new Client("Entreprise", adresseParDefault, "0123456789",
                                    "test@test.fr", "", 1000, nbEmployes),
                    () -> "La création avec nbEmployes= " + nbEmployes + " devrait réussir"
            );
        }

    }

    // ============================================================
    // TESTS VALIDATION RÈGLES MÉTIER
    // ============================================================

    @Nested
    @DisplayName("Validation des règles métiers")
    class ValidationReglesMetierTest {

        @ParameterizedTest(name = "CA = {0} (< 200) devrait lever une exception")
        @ValueSource(longs = {-1000, -1, 0, 1, 100, 199})
        @DisplayName("Chiffre d'affaires invalide (< 200)")
        void validationChiffreAffairesInvalide(Long chiffreAffaires) {
            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> new Client("Entreprise", adresseParDefault, "0123456789",
                            "test@test.fr", "", chiffreAffaires, 10),
                    "La création avec CA= " + chiffreAffaires + " devrait lever une exception"
            );

            assertTrue(exception.getMessage().contains("chiffre d'affaires"),
                    "Le message d'erreur devrait mentionner 'chiffre d'affaires'");
        }

        @ParameterizedTest(name = "Nb employés = {0} (<= 0) devrait lancer une exception")
        @ValueSource(ints = {-100, -1, 0})
        @DisplayName("Nombre d'employés invalide (<= 0)")
        void validationNbEmployesInvalide(int nbEmployes) {
            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> new Client("Entreprise", adresseParDefault, "0123456789",
                            "test@test.fr", "", 50000, nbEmployes),
                    "La création avec nbEmployés= " + nbEmployes + " devrait lancer une exception"
            );

            assertTrue(exception.getMessage().contains("employés"),
                    "Le message d'erreur devrait mentionner 'employés'");
        }

        @ParameterizedTest(name = "Raison sociale \"{0}\" invalide")
        @CsvSource(value = {
                "NULL, 'null'",
                "'', 'vide'",
                "'  ', 'espaces uniquement'"
        }, nullValues = "NULL")
        @DisplayName("Raison sociale obligatoire (null, vide ou whitespace)")
        void validationRaisonSocialeObligatoire(String raisonSociale, String description) {
            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> new Client(raisonSociale, adresseParDefault, "0123456789",
                            "test@test.fr", "", 1000, 10),
                    "La création avec raison sociale " + description + "devrait lever une exception"
            );

            assertTrue(exception.getMessage().contains("raison sociale"),
                    "Le message d'erreur devrait mentionner 'raison sociale'");
        }

        @Test
        @DisplayName("Adresse obligatoire (null non accepté)")
        void validationAdresseObligatoire() {
            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> new Client("Entreprise", null, "0123456789",
                            "test@test.fr", "", 1000, 10)
            );

            assertTrue(exception.getMessage().contains("adresse"),
                    "Le message d'erreur devrait mentionner 'adresse'");
        }
    }
}