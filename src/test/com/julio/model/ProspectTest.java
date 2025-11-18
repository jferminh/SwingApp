package test.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test de la classe Prospect")
class ProspectTest {
    private Adresse adresseParDefault;
    private LocalDate dateProspectionParDefault;

    @BeforeEach
    void setUp() throws ValidationException {
        Adresse.resetCompteur();
        Prospect.resetCompteur();

        adresseParDefault = new Adresse("5", "Avenue Principale", "54000", "Nancy");
        dateProspectionParDefault = LocalDate.of(2024, 11, 6);
    }

    // ============================================================
    // TESTS CRÉATION ET VALIDATION
    // ============================================================

    @Nested
    @DisplayName("Création de prospects")
    class CreationTest {

        @Test
        @DisplayName("Création d'un prospect valide avec toutes les données")
        void creationProspectValideComplet() throws ValidationException {
            String raisonSociale = "Société XYZ";
            String telephone = "0456789012";
            String email = "info@xyz.fr";
            String commentaires = "Prospect intéressant";

            Prospect prospect = new Prospect(raisonSociale, adresseParDefault, telephone,
                    email, commentaires, dateProspectionParDefault, Interesse.OUI);

            assertAll("Vérification du prospect créé",
                    () -> assertNotNull(prospect, "Le prospect ne devrait pas être null"),
                    () -> assertEquals(1, prospect.getId(), "L'ID devrait être 1 (premier prospecst"),
                    () -> assertEquals(raisonSociale, prospect.getRaisonSociale()),
                    () -> assertEquals(adresseParDefault, prospect.getAdresse()),
                    () -> assertEquals(telephone, prospect.getTelephone()),
                    () -> assertEquals(email, prospect.getEmail()),
                    () -> assertEquals(commentaires, prospect.getCommentaires()),
                    () -> assertEquals(dateProspectionParDefault, prospect.getDateProspection()),
                    () -> assertEquals(Interesse.OUI, prospect.getInteresse()),
                    () -> assertEquals("Prospect", prospect.getTypeSociete())
            );


        }

        @ParameterizedTest(name = " Création avec intéressé = {0}")
        @EnumSource(Interesse.class)
        @DisplayName("Création avec tous les valeurs d'Intéressé (OUI/NON)")
        void creationAvecToutesValeursInteresse(Interesse interesse) {

            assertDoesNotThrow(() -> new Prospect("Société Test", adresseParDefault,
                    "0456789012", "test@test.fr", "",
                    dateProspectionParDefault, interesse),
                    () -> "La création avec intéressé= " + interesse + " devrait réussir");
        }
    }

    // ============================================================
    // TESTS VALIDATION RÈGLES MÉTIER
    // ============================================================

    @Nested
    @DisplayName("Validation des règles métier")
    class ValidationReglesMetierTests {

        @Test
        @DisplayName("Date de prospection obligatoire (null non accepté)")
        void validationDateDeProspectionObligatoire() {
            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> new Prospect("Société", adresseParDefault,
                            "0456789012","test@test.fr", "",
                            null, Interesse.OUI),
                    "La création avec date null devrait lever une exception");

            assertTrue(exception.getMessage().contains("date de prospection"),
                    "Le message d'erreur devrait mentionner 'date de prospection'");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Champ 'Intéressé' obligatoire (null non accepté)")
        void validationInteresseObligatoire(Interesse interesse) {

            ValidationException exception = assertThrows(
                    ValidationException.class,
                    () -> new Prospect("Société",
                            adresseParDefault, "0456789012",
                            "test@test.fr", "",
                            dateProspectionParDefault, interesse),
                    "La création avec intéressé null devrait lever une exception");

            assertTrue(exception.getMessage().toLowerCase().contains("intéressé"),
                    "Le message d'erreur devrait mentionner 'intéressé'");
        }
    }

    // ============================================================
    // TESTS FORMAT DATE
    // ============================================================

    @Nested
    @DisplayName("Formatage de la date de prospection")
    class FormatDateTests {

        @Test
        @DisplayName("Format de date jj/MM/aaaa")
        void testFormatDateProspection() throws ValidationException {
            LocalDate date = LocalDate.of(2024, 3, 15);
            Prospect prospect = new Prospect("Société", adresseParDefault, "0456789012",
                    "test@test.fr", "", date, Interesse.NON);

            String dateFormatee = prospect.getDateProspectionFormatee();

            assertEquals("15/03/2024", dateFormatee,
                    "La date devrait être formatée en jj/MM/aaaa");
        }

    }
}


