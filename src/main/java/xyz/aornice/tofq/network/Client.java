package xyz.aornice.tofq.network;

import xyz.aornice.tofq.network.command.Command;

/**
 * client interface
 * Created by drfish on 07/05/2017.
 */
public interface Client extends State {
    Command invokeSync(String addr, Command request, long timeoutMillis);

    void invokeAsync(String addr, Command request, long timeoutMillis);
}