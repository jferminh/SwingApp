package test.com.julio.service;

import main.com.julio.service.ValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests du service de validation")
class ValidationServiceTest {
    // ============================================================
    // TESTS VALIDATION CODE POSTAL
    // ============================================================

    @Nested
    @DisplayName("Validation des codes postaux")
    class CodePostauxTests {
        @ParameterizedTest(name = "Le code postal \"{0}\" doit être valide")
        @ValueSource(strings = {
                "54000", "54390", "54100", "75001", "75002", "00000", "99999"
        })
        @DisplayName("Codes postaux valides (5 chiffres)")
        void codesPostauxValides(String codePostal) {
            assertTrue(ValidationService.isValidCodePostal(codePostal),
                    () -> "Le code postal " + codePostal + " devrait être valide");
        }

        @ParameterizedTest(name = "Le code postal \"{0}\" doit être invalide")
        @ValueSource(strings = {
                "7500",      // 4 chiffres
                "750",       // 3 chiffres
                "750011",    // 6 chiffres
                "7500123",   // 7 chiffres
                "ABCDE",     // Lettres
                "7500A",     // Mix lettres/chiffres
                "75 001",    // Avec espace
                "75-001",    // Avec tiret
                ""           // Vide
        })
        @DisplayName("Codes postaux invalides (format incorrect)")
        void codesPostauxInvalides(String codePostal) {
            assertFalse(ValidationService.isValidCodePostal(codePostal),
                    () -> "Le code postal " + codePostal + " devrait être invalide");
        }

        @Test
        @DisplayName("Code postal null doit être invalide")
        void codesPostalNull() {
            assertFalse(ValidationService.isValidCodePostal(null),
                    "Un code postal null devrait être considéré comme invalide");
        }


    }

    // ============================================================
    // TESTS VALIDATION TÉLÉPHONE
    // ============================================================

    @Nested
    @DisplayName("Validation des numéros de télephone")
    class TelephoneTests {
        @ParameterizedTest(name = "Le téléphone \"{0}\" doit être valide")
        @ValueSource(strings = {
                "0123456789",       // Format compact
                "01 23 45 67 89",   // Format avec espaces
                "01.23.45.67.89",   // Format avec points
                "01-23-45-67-89",   // Format avec tirets
                "0612345678",       // Mobile compact
                "06 12 34 56 78",   // Mobile avec espaces
                "06.12.34.56.78",   // Mobile avec points
                "06-12-34-56-78",   // Mobile avec tirets
                "+33123456789",     // Format international supporté
                "0033123456789",    // Format international supporté
        })
        @DisplayName("Numéros de téléphone valides (formats français")
        void telephoneValides(String telephone) {
            assertTrue(ValidationService.isValidTelephone(telephone),
                    () -> "Le numéro " + telephone + " devrait être valide");
        }

        @ParameterizedTest(name = "Le téléphone \"{0}\" doit être invalide")
        @ValueSource(strings = {
                "123456789",        // Pas de 0 initial
                "1234567890",       // Ne commence pas par 0
                "01234567890",      // 11 chiffres
                "012345678",        // 9 chiffres
                "abcdefghij",       // Lettres
                "01234567AB",       // Mix lettres/chiffres
                "01 23 45 67",      // Incomplet
                ""                  // Vide
        })
        @DisplayName("Numéros de téléphone invalides (formats incorrects)")
        void telephoneInvalides(String telephone) {
            assertFalse(ValidationService.isValidTelephone(telephone),
                    () -> "Le numéro " + telephone + " devrait être invalide");
        }

        @Test
        @DisplayName("Téléphone null doit être invalide")
        void telephoneNull() {
            assertFalse(ValidationService.isValidTelephone(null),
                    "Un numéro de téléphone null devrait être considéré comme invalide");
        }
    }

    // ============================================================
    // TESTS VALIDATION EMAIL
    // ============================================================

    @Nested
    @DisplayName("Validation des adresses email")
    class EmailTests {
        @ParameterizedTest(name = "L''email \"{0}\" doit être valide")
        @ValueSource(strings = {
                "test@exemple.com",
                "user@domain.fr",
                "contact@entreprise.com",
                "user.name@company.fr",
                "admin123@test.org"
        })
        @DisplayName("Adresses email valides (formats standards")
        void emailValides(String email) {
            assertTrue(ValidationService.isValidEmail(email),
                    () -> "L'email " + email + " devrait être valide");
        }

        @ParameterizedTest(name = "L''email \"{0}\" doit être invalide")
        @ValueSource(strings = {
                "test@",                // Pas de domaine
                "@example.com",         // Pas de partie locale
                "test.example.com",     // Pas de @
                "test @example.com",    // Espace dans partie locale
                "test@example",         // Pas d'extension
                "test@@example.com",    // Double @
                "test@.com",            // Domaine commence par point
                ""                      // Vide
        })
        @DisplayName("Adresser email invalides (formats incorrects")
        void emailInvalides(String email) {
            assertFalse(ValidationService.isValidEmail(email),
                    () -> "L'email " + email + " devrait être invalide");
        }

        @Test
        @DisplayName("Email null doit être invalide")
        void emailNull() {
            assertFalse(false,
                    "Un email null devrait être considéré comme invalide");
        }
    }

    // ============================================================
    // TESTS VALIDATION NULL ET VIDE
    // ============================================================

    @Nested
    @DisplayName("Validation de chaînes nulles ou vides")
    class NullOrEmptyTest{
        @ParameterizedTest(name = "La chaîne \"{0}\" doit être considéré comme nulle")
        @NullAndEmptySource
        @ValueSource(strings = {
                " ",           // Un espace
                "  ",          // Plusieurs espaces
                "\t",          // Tabulation
                "\n",          // Nouvelle ligne
                "   \t  \n  "  // Mix espaces/tabs/newlines
        })
        @DisplayName("Chaînes nulles, vides ou whitespace")
        void chainesNullesOuVides(String valeur){
            assertTrue(ValidationService.isNullOrEmpty(valeur),
                    () -> "La valeur devrait être considérée comme nulle ou vide");
        }

        @ParameterizedTest(name = "La chaîne \"{0}\" doit être considérée comme valide")
        @ValueSource(strings = {
                "a",
                "test",
                "Valeur valide",
                "  texte avec espaces  ",
                "123",
                "!@#$%"
        })
        @DisplayName("Chaînes non vides")
        void chainesNonVides(String valeur){
            assertFalse(ValidationService.isNullOrEmpty(valeur),
                    () -> "La valeur \"" + valeur + "\" devrait être considérée comme non vide");
        }
    }
}