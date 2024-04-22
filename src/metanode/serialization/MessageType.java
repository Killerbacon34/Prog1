/**
 * Author:      Alex DeVries
 * Assignment:  Program 4
 * Class:       CSI 4321 Data Communications
 */
package metanode.serialization;

import java.util.Arrays;
import java.util.Objects;

/**
 * Enum class for detailing the possible message type for the class
 */
public enum MessageType {
    /**
     * request nodes
     */
    RequestNodes(0,"RN"),
    /**
     * request MetA nodes
     */
    RequestMetaNodes(1, "RM"),
    /**
     * Answer Request
     */
    AnswerRequest(2, "AR"),
    /**
     * Node additions
     */
    NodeAdditions(3, "NA"),
    /**
     * MetA node additions
     */
    MetaNodeAdditions(4, "MA"),
    /**
     * node deletions
     */
    NodeDeletions(5, "ND"),
    /**
     * MetA Node deletions
     */
    MetaNodeDeletions(6, "MD");

    /**
     * the code for the enum value
     */
    private final int code;
    /**
     * the command string for the enum value
     */
    private final String cmd;

    /**
     * constructor for the enum value
     * @param code the code of the enum
     * @param cmd the command string of the enum
     */
    MessageType(int code,String cmd){
        this.code = code;
        this.cmd = cmd;
    }

    /**
     * get the enum value code
     * @return the enum code
     */
    public int getCode(){
        return this.code;
    }

    /**
     * get the command string
     * @return the command string
     */
    public String getCmd(){
        return this.cmd;
    }

    /**
     * get the enum value based on the code
     * @param code the code to get the enum value of
     * @return the messageType value of that code
     */
    public static MessageType getByCode(int code){
        return Arrays.stream(values()).filter(r -> code ==
                r.getCode()).findFirst().orElse(null);
    }

    /**
     * get the enum value based on the command
     * @param cmd the command to get the enum value of
     * @return the messageType value of that enum
     */
    public static MessageType getByCmd(String cmd){
        return Arrays.stream(values()).filter(r ->
                Objects.equals(cmd, r.getCmd())).
                findFirst().orElse(null);
    }
}
