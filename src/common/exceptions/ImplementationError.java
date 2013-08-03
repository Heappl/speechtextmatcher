package common.exceptions;

public class ImplementationError extends Exception {

    private static final long serialVersionUID = 1L;

    public ImplementationError(String label) {
		super(label);
	}

	public ImplementationError() {
	}
}
