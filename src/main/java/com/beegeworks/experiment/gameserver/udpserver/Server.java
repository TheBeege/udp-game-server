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
    private DatagramSocket serverSocket;

    @Getter
    private ServerStatus status = ServerStatus.NOT_STARTED;

    Server(int port) {
        this.port = port;
    }

    public void run() {
        log.info("Server starting");
        status = ServerStatus.LISTENING;
        try {
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            log.error("Failed to bind to port " + port, e);
            status = ServerStatus.NOT_STARTED;
            return;
        }
        byte[] receiveData = new byte[1024];
        byte[] sendData;
        while (shouldContinue) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                if (serverSocket.isClosed())
                    break;
                log.error("Failed to receive packet", e);
            }
            String sentence = new String(receivePacket.getData());
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            try {
                serverSocket.send(sendPacket);
            } catch (IOException e) {
                log.error("Failed to send packet", e);
            }
        }
        serverSocket.close();
        status = ServerStatus.NOT_STARTED;
        log.info("Server stopping");
    }

    void stopServer() {
        serverSocket.close();
        shouldContinue = false;
    }
}
