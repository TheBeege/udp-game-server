package com.beegeworks.experiment.gameserver.udpserver;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

// based on https://systembash.com/a-simple-java-udp-server-and-udp-client/
@Slf4j
public class Server extends Thread {
    private boolean shouldContinue = true;

    private final int port;
    // This part is what could be considered the core "server"
    private DatagramSocket serverSocket;
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

    Server(int port) {
        this.port = port;
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
            // WE LIKE CAPS. WE'RE AN OLD SERVER AND DON'T KNOW WHAT THIS INTERWEBZ THING IS.
            String capitalizedSentence = sentence.toUpperCase();
            // Sockets work in bytes. Make Strings into bytes or die trying.
            sendData = capitalizedSentence.getBytes();
            // Package all of our home-grown, organic data to send to our lovely clients.
            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            try {
                // If you don't know what this does... We should practice English ;)
                serverSocket.send(sendPacket);
            } catch (IOException e) {
                log.error("Failed to send packet", e);
            }
        }
        // Loop's done! Close up the socket and call it a day.
        serverSocket.close();
        status = ServerStatus.NOT_STARTED;
        log.info("Server stopping");
    }

    void stopServer() {
        /*
         We close the socket when we stop so that trying to receive packets
         doesn't block. Otherwise, we'd need to wait for a client to send us
         data to receive and hit the start of the loop again.
          */
        serverSocket.close();
        shouldContinue = false;
    }
}
