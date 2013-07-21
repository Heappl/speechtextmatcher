package common.exceptions;

public class DeserializationException extends Throwable
{
    public DeserializationException(String label) {
        super(label);
    }
    public DeserializationException() {
    }
}
