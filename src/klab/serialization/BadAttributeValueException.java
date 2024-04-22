/**
 * Author:      Alex DeVries
 * Assignment:  Program 0
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Exception for signaling attribute validations
 */
public class BadAttributeValueException extends Exception
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 35939654L;
    /**
     * attribute of the exception instance
     */
    private String attribute = null;

    /**
     * Constructor Exception
     *
     * @param message error message
     * @param attribute name of attribute
     */
    public BadAttributeValueException(String message, String attribute){
        super(message);
        Objects.requireNonNull(message, "message was null");
        Objects.requireNonNull(attribute, "attribute was null");
        this.attribute = attribute;
    }

    /**
     * Constructor Exception
     *
     * @param message error message
     * @param attribute name of attribute
     * @param cause exception cause (maybe null)
     */
    public BadAttributeValueException(String message,
                                      String attribute,
                                      Throwable cause){
        super(message, cause);
        Objects.requireNonNull(message, "message was null");
        Objects.requireNonNull(attribute, "attribute was null");
        this.attribute = attribute;
    }

    /**
     * Gets the bad attribute
     * @return bad attribute
     */
    public String getAttribute(){
        return attribute;
    }
}
