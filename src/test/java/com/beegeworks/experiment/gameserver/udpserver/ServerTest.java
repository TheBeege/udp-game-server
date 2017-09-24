package com.beegeworks.experiment.gameserver.udpserver;

import com.beegeworks.experiment.gameserver.udpserver.util.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServerTest {

    private Server server;

    @Before
    public void setup(){
        server = new Server(9876);
        server.start();
    }

    @Test
    public void echoString() throws Exception {

        String result = Client.sendAndReceiveString("hello world");

        server.stopServer();

        System.out.println("Result: '" + result + "'");
        assertEquals("HELLO WORLD", result.trim());
    }

    @After
    public void tearDown() throws Exception {
        while (server.getStatus() == ServerStatus.LISTENING)
            server.stopServer();
    }
}
