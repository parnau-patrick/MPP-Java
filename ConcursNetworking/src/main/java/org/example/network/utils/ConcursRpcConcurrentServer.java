package org.example.network.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.network.rpcprotocol.ConcursClientRpcReflectionWorker;
import org.example.network.rpcprotocol.Response;
import org.example.services.ConcursServices;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concurrent server implementation for the Concurs application
 */
public class ConcursRpcConcurrentServer extends AbstractConcurrentServer {
    private static final Logger logger = LogManager.getLogger();
    private final ConcursServices concursServices;

    // List of connected clients for notifications
    private static final List<ConcursClientRpcReflectionWorker> connectedClients =
            Collections.synchronizedList(new ArrayList<>());

    public ConcursRpcConcurrentServer(int port, ConcursServices concursServices) {
        super(port);
        this.concursServices = concursServices;
        logger.info("ConcursRpcConcurrentServer initialized");
    }

    @Override
    protected Thread createWorker(Socket client) {
        logger.info("Creating worker for client {}", client.getInetAddress());
        ConcursClientRpcReflectionWorker worker = new ConcursClientRpcReflectionWorker(concursServices, client);

        // Add worker to connected clients
        addConnectedClient(worker);

        Thread workerThread = new Thread(worker);
        // Add shutdown hook to remove worker when thread ends
        workerThread.setUncaughtExceptionHandler((t, e) -> {
            logger.error("Worker thread crashed", e);
            removeConnectedClient(worker);
        });
        return workerThread;
    }

    /**
     * Add a client to the connected clients list
     */
    public static void addConnectedClient(ConcursClientRpcReflectionWorker client) {
        logger.info("Adding client to connected clients list");
        connectedClients.add(client);
        logger.info("Connected clients: {}", connectedClients.size());
    }

    /**
     * Remove a client from the connected clients list
     */
    public static void removeConnectedClient(ConcursClientRpcReflectionWorker client) {
        logger.info("Removing client from connected clients list");
        connectedClients.remove(client);
        logger.info("Connected clients: {}", connectedClients.size());
    }

    /**
     * Notify all connected clients
     */
    public static void notifyAllClients(Response response) {
        logger.info("Notifying all {} connected clients", connectedClients.size());
        synchronized (connectedClients) {
            for (ConcursClientRpcReflectionWorker client : connectedClients) {
                client.sendNotification(response);
            }
        }
    }
}