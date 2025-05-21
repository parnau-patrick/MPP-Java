package org.example.network.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.services.ServiceException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Abstract server class for network operations
 */
public abstract class AbstractServer {
    private static final Logger logger = LogManager.getLogger();
    private int port;
    private ServerSocket server = null;

    public AbstractServer(int port) {
        this.port = port;
    }

    /**
     * Start the server
     * @throws ServiceException if server cannot be started
     */
    public void start() throws ServiceException {
        try {
            server = new ServerSocket(port);
            logger.info("Server started on port: {}", port);
            while (true) {
                logger.info("Waiting for clients...");
                Socket client = server.accept();
                logger.info("Client connected from: {}", client.getInetAddress());
                processRequest(client);
            }
        } catch (IOException e) {
            logger.error("Error starting server", e);
            throw new ServiceException("Error starting server: " + e.getMessage(), e);
        } finally {
            stop();
        }
    }

    /**
     * Process a client request
     * @param client client socket
     */
    protected abstract void processRequest(Socket client);

    /**
     * Stop the server
     * @throws ServiceException if server cannot be stopped
     */
    public void stop() throws ServiceException {
        logger.info("Stopping server...");
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                logger.error("Error closing server", e);
                throw new ServiceException("Error closing server: " + e.getMessage(), e);
            }
        }
    }
}