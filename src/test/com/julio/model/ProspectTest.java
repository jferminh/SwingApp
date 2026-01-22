package test.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link Prospect}.
 * <p>
 * Teste la validation métier, les setters, getters et les règles de gestion
 * des prospects avec utilisation extensive de tests paramétrés.
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 22/01/2026
 */
@DisplayName("Tests Prospect - Validation et Métier")
class ProspectTest {

    private Adresse adresseValide;

    @BeforeEach
    void setUp() throws ValidationException {

        // Créer une adresse valide pour les tests
        adresseValide = new Adresse("456", "Avenue des Champs", "75008", "Paris");
    }

    // ========== TESTS CRÉATION VALIDE ==========

    @Test
    @DisplayName("Création prospect valide - Données complètes")
    void testCreationProspectValide() throws ValidationException {
        // Arrange
        LocalDate dateProspection = LocalDate.of(2026, 1, 15);

        // Act
        Prospect prospect = new Prospect(
                "FutureCorp",
                adresseValide,
                "0123456789",
                "contact@future.fr",
                "Prospect prometteur",
                dateProspection,
                Interesse.OUI
        );

        // Assert
        assertNotNull(prospect);
        assertEquals("FutureCorp", prospect.getRaisonSociale());
        assertEquals(dateProspection, prospect.getDateProspection());
        assertEquals(Interesse.OUI, prospect.getInteresse());
        assertEquals("Prospect", prospect.getTypeSociete());
    }

    @Test
    @DisplayName("Création prospect - Commentaire null accepté")
    void testCreationProspectCommentaireNull() throws ValidationException {
        // Act
        Prospect prospect = new Prospect(
                "NoCom Corp",
                adresseValide,
                "0987654321",
                "info@nocom.fr",
                null,  // Commentaire null
                LocalDate.now(),
                Interesse.NON
        );

        // Assert
        assertNull(prospect.getCommentaires());
    }

    @Test
    @DisplayName("Création prospect - Date aujourd'hui")
    void testCreationProspectDateAujourdhui() throws ValidationException {
        // Arrange
        LocalDate today = LocalDate.now();

        // Act
        Prospect prospect = new Prospect(
                "TodayCorp",
                adresseValide,
                "0111111111",
                "hello@today.co",
                "",
                today,
                Interesse.OUI
        );

        // Assert
        assertEquals(today, prospect.getDateProspection());
    }

    // ========== TESTS PARAMÉTRÉS - INTERESSE ==========

    @ParameterizedTest(name = "Interesse valide: {0}")
    @DisplayName("Validation interesse - Valeurs enum valides")
    @EnumSource(Interesse.class)
    void testInteresseValide(Interesse interesse) throws ValidationException {
        // Act
        Prospect prospect = new Prospect(
                "Test Corp",
                adresseValide,
                "0123456789",
                "test@test.fr",
                "",
                LocalDate.now(),
                interesse
        );

        // Assert
        assertEquals(interesse, prospect.getInteresse());
    }

    @Test
    @DisplayName("Validation interesse - Null rejeté")
    void testInteresseNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Prospect(
                    "Test Corp",
                    adresseValide,
                    "0123456789",
                    "test@test.fr",
                    "",
                    LocalDate.now(),
                    null  // Interesse null
            );
        });

        assertEquals("Le champ 'intéressé' est obligatoire.", exception.getMessage());
    }

    // ========== TESTS PARAMÉTRÉS - DATE PROSPECTION ==========

    @ParameterizedTest(name = "Date prospection valide: {0}")
    @DisplayName("Validation date prospection - Différentes dates")
    @MethodSource("provideDatesValides")
    void testDateProspectionValide(LocalDate date) throws ValidationException {
        // Act
        Prospect prospect = new Prospect(
                "DateTest Corp",
                adresseValide,
                "0123456789",
                "test@test.fr",
                "",
                date,
                Interesse.OUI
        );

        // Assert
        assertEquals(date, prospect.getDateProspection());
    }

    static Stream<LocalDate> provideDatesValides() {
        return Stream.of(
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2025, 6, 15),
                LocalDate.now(),
                LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(10)
        );
    }

    @Test
    @DisplayName("Validation date prospection - Null rejetée")
    void testDateProspectionNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Prospect(
                    "Test Corp",
                    adresseValide,
                    "0123456789",
                    "test@test.fr",
                    "",
                    null,  // Date null
                    Interesse.OUI
            );
        });

        assertEquals("La date de prospection est obligatoire.", exception.getMessage());
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
    })
    void testEmailInvalide(String email) {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            new Prospect(
                    "Test Corp",
                    adresseValide,
                    "0123456789",
                    email,
                    "",
                    LocalDate.now(),
                    Interesse.OUI
            );
        });
    }

    @ParameterizedTest(name = "Email valide: \"{0}\"")
    @DisplayName("Validation email - Formats valides")
    @ValueSource(strings = {
            "simple@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "test123@test-domain.fr"
    })
    void testEmailValide(String email) throws ValidationException {
        // Act
        Prospect prospect = new Prospect(
                "Valid Corp",
                adresseValide,
                "0123456789",
                email,
                "",
                LocalDate.now(),
                Interesse.OUI
        );

        // Assert
        assertEquals(email, prospect.getEmail());
    }

    // ========== TESTS PARAMÉTRÉS - TÉLÉPHONE ==========

    @ParameterizedTest(name = "Téléphone invalide: \"{0}\"")
    @DisplayName("Validation téléphone - Formats invalides")
    @NullAndEmptySource
    @ValueSource(strings = {
            "123",
            "abcdefghij",
            "012345678",
            "012345678901"
    })
    void testTelephoneInvalide(String telephone) {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            new Prospect(
                    "Test Corp",
                    adresseValide,
                    telephone,
                    "test@test.fr",
                    "",
                    LocalDate.now(),
                    Interesse.OUI
            );
        });
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
        Prospect prospect = new Prospect(
                "Valid Corp",
                adresseValide,
                telephone,
                "valid@corp.fr",
                "",
                LocalDate.now(),
                Interesse.OUI
        );

        // Assert
        assertEquals(telephone, prospect.getTelephone());
    }

    // ========== TESTS MÉTHODE SOURCE COMPLÈTE ==========

    @ParameterizedTest(name = "Test données complètes: {0}")
    @DisplayName("Création prospects - Jeu de données complet")
    @MethodSource("provideProspectData")
    void testCreationProspectsComplets(String raison, String tel, String email,
                                       LocalDate date, Interesse interesse) throws ValidationException {
        // Act
        Prospect prospect = new Prospect(raison, adresseValide, tel, email, "", date, interesse);

        // Assert
        assertEquals(raison, prospect.getRaisonSociale());
        assertEquals(tel, prospect.getTelephone());
        assertEquals(email, prospect.getEmail());
        assertEquals(date, prospect.getDateProspection());
        assertEquals(interesse, prospect.getInteresse());
    }

    static Stream<Arguments> provideProspectData() {
        return Stream.of(
                Arguments.of("FutureCorp", "0123456789", "future@test.fr",
                        LocalDate.of(2026, 1, 15), Interesse.OUI),
                Arguments.of("MaybeCorp", "0987654321", "maybe@test.com",
                        LocalDate.of(2025, 12, 1), Interesse.NON),
                Arguments.of("NewStart", "0600000000", "contact@new.fr",
                        LocalDate.now(), Interesse.OUI),
                Arguments.of("OldProspect", "0711111111", "old@legacy.biz",
                        LocalDate.of(2020, 6, 1), Interesse.NON)
        );
    }

    // ========== TESTS SETTERS ==========

    @Test
    @DisplayName("Setter date prospection - Modification valide")
    void testSetDateProspectionValide() throws ValidationException {
        // Arrange
        Prospect prospect = new Prospect("Test", adresseValide, "0123456789",
                "test@test.fr", "", LocalDate.now(), Interesse.OUI);
        LocalDate nouvelleDate = LocalDate.of(2025, 6, 15);

        // Act
        prospect.setDateProspection(nouvelleDate);

        // Assert
        assertEquals(nouvelleDate, prospect.getDateProspection());
    }

    @Test
    @DisplayName("Setter date prospection - Null rejeté")
    void testSetDateProspectionNull() throws ValidationException {
        // Arrange
        Prospect prospect = new Prospect("Test", adresseValide, "0123456789",
                "test@test.fr", "", LocalDate.now(), Interesse.OUI);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            prospect.setDateProspection(null);
        });
    }

    @Test
    @DisplayName("Setter interesse - Modification valide")
    void testSetInteresseValide() throws ValidationException {
        // Arrange
        Prospect prospect = new Prospect("Test", adresseValide, "0123456789",
                "test@test.fr", "", LocalDate.now(), Interesse.NON);

        // Act
        prospect.setInteresse(Interesse.OUI);

        // Assert
        assertEquals(Interesse.OUI, prospect.getInteresse());
    }

    @Test
    @DisplayName("Setter interesse - Null rejeté")
    void testSetInteresseNull() throws ValidationException {
        // Arrange
        Prospect prospect = new Prospect("Test", adresseValide, "0123456789",
                "test@test.fr", "", LocalDate.now(), Interesse.OUI);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            prospect.setInteresse(null);
        });
    }

    // ========== TESTS ADRESSE NULL ==========

    @Test
    @DisplayName("Création prospect - Adresse null rejetée")
    void testCreationProspectAdresseNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            new Prospect(
                    "Test Corp",
                    null,  // Adresse null
                    "0123456789",
                    "test@test.fr",
                    "",
                    LocalDate.now(),
                    Interesse.OUI
            );
        });

        assertTrue(exception.getMessage().contains("adresse"));
    }

    // ========== TESTS FORMATAGE DATE ==========

    @Test
    @DisplayName("getDateProspectionFormatee() retourne format DD/MM/YYYY")
    void testGetDateProspectionFormatee() throws ValidationException {
        // Arrange
        LocalDate date = LocalDate.of(2026, 1, 22);
        Prospect prospect = new Prospect("Test", adresseValide, "0123456789",
                "test@test.fr", "", date, Interesse.OUI);

        // Act
        String dateFormatee = prospect.getDateProspectionFormatee();

        // Assert
        assertEquals("22/01/2026", dateFormatee);
    }

    // ========== TESTS toString() ET getTypeSociete() ==========

    @Test
    @DisplayName("toString() retourne format attendu")
    void testToString() throws ValidationException {
        // Arrange
        Prospect prospect = new Prospect("FutureCorp", adresseValide, "0123456789",
                "test@test.fr", "", LocalDate.now(), Interesse.OUI);

        // Act
        String result = prospect.toString();

        // Assert
        assertEquals("FutureCorp (Prospect)", result);
    }

    @Test
    @DisplayName("getTypeSociete() retourne 'Prospect'")
    void testGetTypeSociete() throws ValidationException {
        // Arrange
        Prospect prospect = new Prospect("Test", adresseValide, "0123456789",
                "test@test.fr", "", LocalDate.now(), Interesse.OUI);

        // Act & Assert
        assertEquals("Prospect", prospect.getTypeSociete());
    }
}
