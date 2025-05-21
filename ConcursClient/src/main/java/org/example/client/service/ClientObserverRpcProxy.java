package org.example.client.service;

import org.example.observer.Observer;

/**
 * Proxy for client-side observer implementation that will receive notifications from server
 */
public class ClientObserverRpcProxy implements Observer {
    private final Observer client;

    public ClientObserverRpcProxy(Observer client) {
        this.client = client;
    }

    @Override
    public void update() {
        client.update();
    }
}