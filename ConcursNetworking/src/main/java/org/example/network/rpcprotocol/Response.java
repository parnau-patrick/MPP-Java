package org.example.network.rpcprotocol;

import java.io.Serializable;

/**
 * Response class for RPC protocol
 */
public class Response implements Serializable {
    private ResponseType type;
    private Object data;

    public Response() {}

    public Response(ResponseType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public ResponseType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}