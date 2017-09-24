package com.beegeworks.experiment.gameserver.udpserver;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

// based on https://systembash.com/a-simple-java-udp-server-and-udp-client/
// enhancements guided by https://gafferongames.com/post/client_server_connection/
@Slf4j
public class Server extends Thread {

    private boolean shouldContinue = true;

    private final int port;

    private final int maxConnectionSlots;
    /*
    What's a datagram and a socket?
    Put simply, a datagram is a packet of data. A socket is an
    object that allows two processes to communicate with each
    other, whether on the same computer or across the internet.

    Are there other kinds of sockets?
    Yes. The two main protocols at this layer are TCP and UDP.
    UDP (User Datagram Protocol) is what we're doing here. It's
    basically a "fire and forget" style. You send data to some
    target, and you don't care what happens.
    TCP (Transmission Control Protocol) is much fancier. It
    communicates more heavily with the target to ensure that
    all data is received and in the correct order.

    So why UDP?
    UDP is generally favored by game servers. Why? Performance.
    All of the verification that TCP does is costly, so we go
    with UDP just to get stuff going. Why not both UDP and
    TCP together? Well, trying to use them together can lead
    to Bad Things. Often, game servers will use UDP and add
    some components of TCP for the data they really need
    to make sure arrives properly. This way, you get the
    speed of UDP, the parts of TCP you absolutely need,
    and avoid Bad Things.
     */

    @Getter
    private ServerStatus status = ServerStatus.NOT_STARTED;
    // This part is what could be considered the core "server"
    @Getter
    private DatagramSocket serverSocket;
    private int countAvailableSlots;
    private List<GameConnection> connections;

    Server(int port, int maxConnectionSlots) {
        this.port = port;
        this.maxConnectionSlots = maxConnectionSlots;
        this.countAvailableSlots = maxConnectionSlots;
    }

    /*
    The run() method is required via the Runnable interface,
    which Thread implements. This method is what's executed
    in the separate thread when you run start(). Usually
    for things like servers, this is where you'll have
    your infinite loop, just like we do here. We use a state
    variable (shouldContinue) to know when our loop should
    end. Realistically, the break when checking for a received
    packet should also do the trick, but I'm being lazy
    right now :)
     */
    public void run() {
        log.info("Server starting");
        // Set the server status. Probably not necessary, but readable.
        status = ServerStatus.LISTENING;
        try {
            // Open up that port and get ready for data
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            /*
             It could fail in weird cases, or what happens most often,
             if the port was already bound to.
              */
            log.error("Failed to bind to port " + port, e);
            status = ServerStatus.NOT_STARTED;
            return;
        }
        byte[] receiveData = new byte[1024];
        byte[] sendData;
        // Loop of Doom! Doom I say! Seriously, the game Doom probably has a loop like this.
        while (shouldContinue) {
            // Get ready to receive that juicy, juicy data.
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                // This is where we receive data from some client.
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                /*
                 If the server is stopping, we'll have closed the socket. If that's the
                 case, we should stop looping.
                  */
                if (serverSocket.isClosed())
                    break;
                log.error("Failed to receive packet", e);
            }
            // Convert the data we got to text
            String sentence = new String(receivePacket.getData());
            System.out.println("RECEIVED: " + sentence);
            // Get the address of the client we received data from
            InetAddress IPAddress = receivePacket.getAddress();
            // Get the port the client opened up the connection through
            int port = receivePacket.getPort();

        }
        // Loop's done! Close up the socket and call it a day.
        serverSocket.close();
        status = ServerStatus.NOT_STARTED;
        log.info("Server stopping");
    }

    void stopServer() {
        connections.forEach(GameConnection::closeConnection);
        /*
         We close the socket when we stop so that trying to receive packets
         doesn't block. Otherwise, we'd need to wait for a client to send us
         data to receive and hit the start of the loop again.
          */
        serverSocket.close();
        shouldContinue = false;
    }

    private boolean checkIfAlreadyConnected(GameConnection connection) {
        return connections.stream()
                .filter(c -> c.toString().equals(connection.toString()))
                .count() > 0;
    }

    private void useSlot(GameConnection connection) {
        if (checkIfAlreadyConnected(connection)) {
            connection.accept();
            return;
        }

        if (countAvailableSlots >= maxConnectionSlots) {
            connection.deny();
            return;
        }
        connection.accept();
        connections.add(connection);
        countAvailableSlots--;
    }

    synchronized void freeSlot(GameConnection connection) {
        connections.remove(connection);
        countAvailableSlots++;
    }
}
