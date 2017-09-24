package com.beegeworks.experiment.gameserver.udpserver;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;

@Slf4j
public class GameConnection extends Thread {

    private static final int TIMEOUT_THRESHOLD_IN_SECONDS = 5;

    private final Server server;
    private final DatagramSocket socket;
    private final InetAddress clientIPAddress;
    private final int clientPort;
    private LocalDateTime lastActivity;
    private LocalDateTime timeConnected; // not used now, but I think we'll use it later

    private byte[] receiveData = new byte[1024];
    private byte[] sendData;

    private boolean shouldRun = true;

    public GameConnection(Server server, InetAddress clientIPAddress, int clientPort) {
        this.server = server;
        this.socket = server.getServerSocket();
        this.clientIPAddress = clientIPAddress;
        this.clientPort = clientPort;
        this.lastActivity = LocalDateTime.now();
        this.timeConnected = lastActivity;
    }

    public void run() {
        while (shouldRun) {
            if (LocalDateTime.now().minusSeconds(TIMEOUT_THRESHOLD_IN_SECONDS).isAfter(lastActivity))
                // send timeout message, close connection
                break;

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                // This is where we receive data from some client.
                server.getServerSocket().receive(receivePacket);
            } catch (IOException e) {
                /*
                 If the server is stopping, we'll have closed the socket. If that's the
                 case, we should stop looping.
                  */
                if (socket.isClosed())
                    break;
                log.error("Failed to receive packet", e);
            }

            // Convert the data we got to text
            String sentence = new String(receivePacket.getData());
            System.out.println("RECEIVED: " + sentence);
            // WE LIKE CAPS. WE'RE AN OLD SERVER AND DON'T KNOW WHAT THIS INTERWEBZ THING IS.
            String capitalizedSentence = sentence.toUpperCase();
            // Sockets work in bytes. Make Strings into bytes or die trying.
            sendData = capitalizedSentence.getBytes();
            // Package all of our home-grown, organic data to send to our lovely clients.
            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, clientIPAddress, clientPort);
            try {
                // If you don't know what this does... We should practice English ;)
                socket.send(sendPacket);
            } catch (IOException e) {
                log.error("Failed to send packet", e);
            }
            this.lastActivity = LocalDateTime.now();
        }

        server.freeSlot(this);
    }

    void accept() {
        // tell client we're gucci
        sendData = "CONNECTION ACCEPTED".getBytes();
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, clientIPAddress, clientPort);
        try {
            // If you don't know what this does... We should practice English ;)
            socket.send(sendPacket);
        } catch (IOException e) {
            log.error("Failed to send connection acceptance", e);
        }
    }

    void deny() {
        // tell client NOPE.
        sendData = "CONNECTION DENIED".getBytes();
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, clientIPAddress, clientPort);
        try {
            // If you don't know what this does... We should practice English ;)
            socket.send(sendPacket);
        } catch (IOException e) {
            log.error("Failed to send connection denial", e);
        }
    }

    void closeConnection() {
        this.shouldRun = false;
    }

    @Override
    public String toString() {
        return clientIPAddress.getHostAddress() + ":" + clientPort;
    }
}
