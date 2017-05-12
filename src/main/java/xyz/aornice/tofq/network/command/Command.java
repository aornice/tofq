package xyz.aornice.tofq.network.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.network.command.body.CommandBody;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link Command} represents command sent between clients and servers, it has different {@link CommandType}.
 * The specific instruction of each command is determined by its {@link #code} field.
 * All data needed in the command is provided by {@link #body} field.
 * Created by drfish on 07/05/2017.
 */
public class Command {
    private static final Logger logger = LoggerFactory.getLogger(Command.class);
    private static AtomicInteger requestId = new AtomicInteger(0);
    /**
     * communicating code between C/S, refer {@link xyz.aornice.tofq.network.command.protocol.RequestCode} and {@link xyz.aornice.tofq.network.command.protocol.ResponseCode}
     */
    private int code;
    /**
     * both of the request and the response of a communication have the same opaque code
     */
    private int opaque = requestId.getAndIncrement();
    /**
     * type of the command
     */
    private CommandType type;
    /**
     * mark of one way command
     */
    private boolean isOneway;
    /**
     * fields in the commandBody
     */
    private Map<String, String> fields;
    /**
     * real command instance
     */
    private transient CommandBody body;

    private Command() {
    }

    public static Command createRequestCommand(int code, CommandBody body) {
        Command cmd = new Command();
        cmd.setType(CommandType.REQUEST);
        cmd.setCode(code);
        cmd.setBody(body);
        return cmd;
    }

    public static Command createResponseCommand(int code, Class<? extends CommandBody> bodyClass) {
        Command cmd = new Command();
        cmd.setType(CommandType.RESPONSE);
        cmd.setCode(code);
        if (bodyClass != null) {
            try {
                CommandBody bodyObj = bodyClass.newInstance();
                cmd.setBody(bodyObj);
            } catch (IllegalAccessException | InstantiationException e) {
                logger.debug("", e);
                return null;
            }
        }
        return cmd;
    }

    public static Command createResponseCommand(int code) {
        return createResponseCommand(code, null);
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public boolean isOneway() {
        return isOneway;
    }

    public void setOneway(boolean oneway) {
        isOneway = oneway;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public CommandBody getBody() {
        return body;
    }

    public void setBody(CommandBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Command [code=" + code + ", opaque=" + opaque + ", type=" + type + " fields=" + fields + " body=" + body + "]";
    }
}
