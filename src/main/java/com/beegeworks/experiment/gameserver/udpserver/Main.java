package com.beegeworks.experiment.gameserver.udpserver;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(9876, 100);
        /*
            A shutdown hook is an object that can react to a process shutdown,
            like a keyboard interrupt. This is how we have a "graceful" shutdown,
            allowing us to do any cleanup work before ending the process.
         */
        Runtime.getRuntime().addShutdownHook(new ProcessorHook(server));

        /*
        Server inherits from java.lang.Thread for multithreading (read as, asynchronous)
        execution. I mostly did this so that my tests could both run the server
        and act as a client, but it will also be useful for running multiple
        server instances in the same process. I'm not sure yet if that's actually
        a good idea, but that's my next task.
        The start() method begins the thread and runs it asynchronously.
         */
        server.start();

        /*
         The join() method is provided by Thread. It blocks the current execution
         flow until the thread finishes running.
          */
        server.join();
    }
}
