package vn.tayjava.exception;

public class InvalidDataExcetion extends RuntimeException {

    public InvalidDataExcetion(String message)
    {
        super(message);
    }

    public InvalidDataExcetion(String message, Throwable cause)
    {
        super(message, cause);
    }
}
