package common.exceptions;

public class IllegalArgumentException extends Throwable
{
    private static final long serialVersionUID = 2857364032426040869L;
    
    public IllegalArgumentException(String label) {
        super(label);
    }
    public IllegalArgumentException() {
    }
}