package xyz.aornice.tofq.network.command.body;

/**
 * {@link CommandBody} specifies the instruction of the command. Each specific command should have an instance of a
 * class implementing this class as its header.
 * Created by drfish on 07/05/2017.
 */
public interface CommandBody {
    void checkFields();
}
