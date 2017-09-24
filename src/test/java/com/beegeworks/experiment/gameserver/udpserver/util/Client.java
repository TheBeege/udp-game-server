package com.beegeworks.experiment.gameserver.udpserver.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;

@Slf4j
public class Client {

    private static final String TARGET_HOST = "localhost";
    private static final int TARGET_PORT = 9876;

    public static String sendAndReceiveString(String input) {
        // Sockets are used for any process to communicate with another, client or server.
        DatagramSocket clientSocket;
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            // This could fail if the security manager says no.
            // I don't actually know what a security manager is. Let's hope he's a nice guy.
            log.error("Failed to create new UDP socket", e);
            throw new RuntimeException(e);
        }

        InetAddress serverIPAddress;
        try {
            // Get the address of the server we plan on talking to.
            serverIPAddress = InetAddress.getByName(TARGET_HOST);
        } catch (UnknownHostException e) {
            // What do you mean "myfakeaddress.bogustld" isn't a real hostname?
            log.error("Failed to get address for hostname " + TARGET_HOST, e);
            throw new RuntimeException(e);
        }

        // Whatever sentence we're going to enter, it needs to be bytes to send along the socket.
        byte[] sendData = input.getBytes();
        // Package up our sentence bytes into a datagram packet
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, TARGET_PORT);
        try {
            // Send that data to our server buddy.
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            log.error("Failed to send packet", e);
            throw new RuntimeException(e);
        }

        // Get ready to receive data
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            // Receive the data
            clientSocket.receive(receivePacket);
        } catch (IOException e) {
            log.error("Failed to receive packet", e);
            throw new RuntimeException(e);
        }
        // Convert the bytes we got from the server into a string
        String receivedString = new String(receivePacket.getData());
        System.out.println("FROM SERVER:" + receivedString);
        clientSocket.close();

        return receivedString;
    }
}