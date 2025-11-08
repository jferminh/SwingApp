package main.com.julio.service;

import main.com.julio.util.RegexPatterns;

public class ValidationService {
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isValidCodePostal(String codePostal) {
        return codePostal != null && codePostal.matches(RegexPatterns.CODE_POSTAL);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(RegexPatterns.EMAIL);
    }

    public static boolean isValidTelephone(String telephone) {
        return telephone != null && telephone.matches(RegexPatterns.TELEPHONE);
    }
}
