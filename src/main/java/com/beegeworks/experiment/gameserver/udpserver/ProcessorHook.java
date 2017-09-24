package com.beegeworks.experiment.gameserver.udpserver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessorHook extends Thread {

    private final Server server;

    ProcessorHook(Server server) {
        this.server = server;
    }

    @Override
    public void run(){
        log.info("Stopping");
        server.stopServer();
    }
}
