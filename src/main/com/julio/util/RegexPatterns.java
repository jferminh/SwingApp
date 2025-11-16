package main.com.julio.util;

import java.util.regex.Pattern;

public class RegexPatterns {
//    public static final Pattern CODE_POSTAL = Pattern.compile("^\\d{5}$");
    public static final String CODE_POSTAL = "^\\d{5}$";
//    public static final Pattern TELEPHONE = Pattern.compile("^(?:\\+?\\d{1,3})?[ .-]?(?:\\d{2}[ .-]?){4}\\d{2}$");
//    public static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");public static final Pattern CODE_POSTAL = Pattern.compile("^\\d{5}$");
    public static final String TELEPHONE = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$";
    public static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
}
