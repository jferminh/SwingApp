package main.com.julio.util;

public class RegexPatterns {
    public static final String CODE_POSTAL = "^\\d{5}$";
    public static final String TELEPHONE = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$";
    public static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
}
