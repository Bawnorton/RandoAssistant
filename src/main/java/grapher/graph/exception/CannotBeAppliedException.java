package grapher.graph.exception;

import java.io.Serial;

/**
 * Exception which is thrown when an algorithm cannot be applied (if the algorithm
 * wasn't designed for the graph in question)
 *
 * @author Renata
 */
public class CannotBeAppliedException extends Exception {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private final String message;

    public CannotBeAppliedException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
