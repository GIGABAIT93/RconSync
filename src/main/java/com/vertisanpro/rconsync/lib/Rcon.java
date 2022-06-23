package com.vertisanpro.rconsync.lib;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Rcon
{
    public Rcon(String host, int port, byte[] password) throws IOException, AuthenticationException {
        this.sync = new Object();
        this.rand = new Random();
        this.charset = StandardCharsets.UTF_8;

        connect(host, port, password);
    }

    private final Object sync;

    private final Random rand;

    private int requestId;

    private Socket socket;
    private final Charset charset;

    private void connect(String host, int port, byte[] password) throws IOException, AuthenticationException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host can't be null or empty");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port is out of range");
        }

        synchronized (this.sync) {

            this.requestId = this.rand.nextInt();
            this.socket = new Socket(host, port);
        }

        RconPacket res = send(3, password);

        if (res.getRequestId() == -1) {
            throw new AuthenticationException("Password rejected by server");
        }
    }

    public void disconnect() throws IOException {
        synchronized (this.sync) {
            this.socket.close();
        }
    }

    public String command(String payload) throws IOException {
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("Payload can't be null or empty");
        }

        RconPacket response = send(2, payload.getBytes());

        return new String(response.getPayload(), getCharset());
    }

    private RconPacket send(int type, byte[] payload) throws IOException {
        synchronized (this.sync) {
            return RconPacket.send(this, type, payload);
        }
    }

    public int getRequestId() { return this.requestId; }

    public Socket getSocket() { return this.socket; }

    public Charset getCharset() { return this.charset; }

}
