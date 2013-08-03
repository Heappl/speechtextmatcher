package common.exceptions;

public class HMMException extends Throwable
{
    private static final long serialVersionUID = 1755142092859697016L;
    
    public HMMException(String label) {
        super(label);
    }
    public HMMException() {
    }
}
