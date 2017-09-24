package com.beegeworks.experiment.gameserver.udpserver.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;

@Slf4j
public class Client {

    private static final String TARGET_HOST = "localhost";
    private static final int TARGET_PORT = 9876;

    public static String sendAndReceiveString(String input) {
        DatagramSocket clientSocket;
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            log.error("Failed to create new UDP socket", e);
            throw new RuntimeException(e);
        }

        InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getByName(TARGET_HOST);
        } catch (UnknownHostException e) {
            log.error("Failed to get address for hostname " + TARGET_HOST, e);
            throw new RuntimeException(e);
        }

        byte[] sendData = input.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, TARGET_PORT);
        try {
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            log.error("Failed to send packet", e);
            throw new RuntimeException(e);
        }

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            clientSocket.receive(receivePacket);
        } catch (IOException e) {
            log.error("Failed to receive packet", e);
            throw new RuntimeException(e);
        }
        String receivedString = new String(receivePacket.getData());
        System.out.println("FROM SERVER:" + receivedString);
        clientSocket.close();

        return receivedString;
    }
}