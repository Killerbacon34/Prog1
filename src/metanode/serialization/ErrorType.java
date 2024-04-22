/**
 * Author:      Alex DeVries
 * Assignment:  Program 4
 * Class:       CSI 4321 Data Communications
 */
package metanode.serialization;

import java.util.Arrays;

/**
 * Enum class for the error types within answer request messages
 */
public enum ErrorType {
    /**
     * if no error is present
     */
    None(0),
    /**
     * if a system call failure occurred
     */
    System(10),
    /**
     * if an incorrect packet was received
     */
    IncorrectPacket(20);
    /**
     * code for the enum value
     */
    private final int code;

    /**
     * constructor for the enum value
     * @param code the code for the enum value
     */
    ErrorType(int code){
        this.code = code;
    }

    /**
     * gets the code of the enum value
     * @return the code for the enum type
     */
    public int getCode(){
        return this.code;
    }

    /**
     * get the enum value based on the code
     * @param code the code to get the enum value of
     * @return the enum value based on the code
     */
    public static ErrorType getByCode(int code){
        return Arrays.stream(values()).filter(r -> code ==
                r.getCode()).findFirst().orElse(null);
    }
}
