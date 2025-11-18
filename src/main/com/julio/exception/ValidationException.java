package main.com.julio.exception;

public class ValidationException extends Exception {
//    private final String field;

    public ValidationException(String message) {
        super(message);
//        this.field = field;
    }

//    public String getField() {
//        return this.field;
//    }
}
