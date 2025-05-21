package org.example.network.utils;

import java.net.Socket;

/**
 * Abstract concurrent server with thread-based request processing
 */
public abstract class AbstractConcurrentServer extends AbstractServer {

    public AbstractConcurrentServer(int port) {
        super(port);
    }

    @Override
    protected void processRequest(Socket client) {
        Thread worker = createWorker(client);
        worker.start();
    }

    /**
     * Create a worker thread for client request
     * @param client client socket
     * @return worker thread
     */
    protected abstract Thread createWorker(Socket client);
}