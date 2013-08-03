package common.exceptions;

public class DeserializationException extends Throwable
{
    private static final long serialVersionUID = 5981924111341690194L;
    
    public DeserializationException(String label) {
        super(label);
    }
    public DeserializationException() {
    }
}
