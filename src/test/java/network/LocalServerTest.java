package network;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import xyz.aornice.tofq.network.Client;
import xyz.aornice.tofq.network.Server;
import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.command.body.CommandBody;
import xyz.aornice.tofq.network.exception.NetworkConnectException;
import xyz.aornice.tofq.network.exception.NetworkSendRequestException;
import xyz.aornice.tofq.network.exception.NetworkTimeoutException;
import xyz.aornice.tofq.network.exception.NetworkTooManyRequestsException;
import xyz.aornice.tofq.network.netty.TofqNettyClient;
import xyz.aornice.tofq.network.netty.TofqNettyClientConfig;
import xyz.aornice.tofq.network.netty.TofqNettyServer;
import xyz.aornice.tofq.network.netty.TofqNettyServerConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * Created by drfish on 10/05/2017.
 */
public class LocalServerTest {
    private static Client client;
    private static Server server;
    private static final String ADDRESS = "localhost:31515";
    private static final int TIMEOUT = 3000;

    private static Server createTofqServer() {
        TofqNettyServerConfig serverConfig = new TofqNettyServerConfig();
        Server server = new TofqNettyServer(serverConfig);
        server.registerProcessor(0, (ctx, request) -> {
            return request;
        }, Executors.newCachedThreadPool());
        server.start();
        return server;
    }

    private static Client createTofqClient() {
        TofqNettyClientConfig clientConfig = new TofqNettyClientConfig();
        Client client = new TofqNettyClient(clientConfig);
        client.start();
        return client;
    }

    @BeforeClass
    public static void setup() {
        client = createTofqClient();
        server = createTofqServer();
    }

    @AfterClass
    public static void cleanup() {
        client.shutdown();
        server.shutdown();
    }

    @Test
    public void testInvokeSync() throws InterruptedException, NetworkConnectException, NetworkTimeoutException, NetworkSendRequestException {
        SampleCommandBody commandBody = new SampleCommandBody();
        commandBody.setId(1);
        commandBody.setMessage("test");
        Command request = Command.createRequestCommand(0, commandBody);
        Command response = client.invokeSync(ADDRESS, request, TIMEOUT);
        System.out.println(response);
    }

    @Test
    public void testInvokeAsync() throws InterruptedException, NetworkConnectException, NetworkSendRequestException, NetworkTooManyRequestsException {
        CountDownLatch latch = new CountDownLatch(1);
        Command request = Command.createRequestCommand(0, null);
        client.invokeAsync(ADDRESS, request, TIMEOUT, responseFuture -> {
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testInvokeOneway() throws InterruptedException, NetworkSendRequestException, NetworkTooManyRequestsException, NetworkTimeoutException {
        Command request = Command.createRequestCommand(0, null);
        client.invokeOneway(ADDRESS, request, TIMEOUT);
    }
}

class SampleCommandBody implements CommandBody {
    private Integer id;
    private String message;

    @Override
    public void checkFields() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "[id=" + id + " message=" + message + "]";
    }
}
