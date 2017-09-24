package com.beegeworks.experiment.gameserver.udpserver;

import lombok.extern.slf4j.Slf4j;

// This is the magic that handles things like keyboard interrupts.
@Slf4j
public class ProcessorHook extends Thread {

    private final Server server;

    ProcessorHook(Server server) {
        this.server = server;
    }

    // This is the method that's run when an interrupt is encountered
    @Override
    public void run(){
        log.info("Stopping");
        server.stopServer();
    }
}
