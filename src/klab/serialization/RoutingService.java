/**
 * Author:      Alex DeVries
 * Assignment:  Program 1
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization;


import java.util.Arrays;

/**
 * routing service
 */
public enum RoutingService{
    /**
     * values of the enum
     */
    BREADTHFIRST(0),
    /**
     * enum value for depthfirst search
     */
    DEPTHFIRST(1);
    /**
     * the code related to each enum value
     */
    private final int code;

    /**
     * constructor for the code
     * @param code the enum value to use
     */
    RoutingService(int code) {
        this.code = code;
    }

    /**
     * Get code for routing service
     * @return routing service code
     */
    public int getCode(){
        return this.code;
    }

    /**
     * Get routing service for given code
     * @param code code of routing service
     * @return routing service corresponding to code
     * @throws BadAttributeValueException if bad code value
     */
    public static RoutingService getRoutingService(int code)
                                throws BadAttributeValueException{

        return Arrays.stream(values()).filter(r -> code ==
                r.getCode()).findFirst().orElseThrow(() ->
                new BadAttributeValueException("Bad enum value",
                        "RoutingService"));
    }
}
