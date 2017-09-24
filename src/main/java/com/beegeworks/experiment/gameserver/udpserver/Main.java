package com.beegeworks.experiment.gameserver.udpserver;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(9876);
        Runtime.getRuntime().addShutdownHook(new ProcessorHook(server));
        server.start();
        server.join();
    }
}
