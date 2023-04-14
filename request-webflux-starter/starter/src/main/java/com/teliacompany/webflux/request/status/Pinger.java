package com.teliacompany.webflux.request.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public final class Pinger {
    private static final Logger LOG = LoggerFactory.getLogger(Pinger.class);

    private Pinger() {
        //Static util class
    }

    public static Response ping(String serviceName, String host, Integer port, int pingTimeout, String proxyHost, Integer proxyPort) {
        LOG.debug("Pinging {} on host {} and port {}...", serviceName, host, port);
        if(host != null && port > 0) {
            long startTime = System.currentTimeMillis();

            Proxy proxy = Proxy.NO_PROXY;
            String proxyProtocol = "";
            if (proxyHost != null && proxyPort != null) {
                LOG.debug("Through proxy {} with port {}...", proxyHost, proxyPort);
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                // Using http protocol required for proxy to accept the call from k8s pod.
                proxyProtocol = "https://";
            }

            try(Socket socket = new Socket(proxy)) {
                socket.connect(new InetSocketAddress(proxyProtocol + host, port), pingTimeout);
                return new Response("UP", System.currentTimeMillis() - startTime);
            } catch(IOException e) {
                // Either timeout or unreachable or failed DNS lookup.
                return new Response("DOWN", System.currentTimeMillis() - startTime).setException(e);
            }
        } else {
            LOG.debug("Could not ping {}, host = {}, port = {}", serviceName, host, port);
            return new Response("UNKNOWN", 0);
        }
    }

    public static class Response {
        private final String status;
        private final long responseTime;
        private IOException exception;

        public Response(String status, long responseTime) {
            this.status = status;
            this.responseTime = responseTime;
        }

        public IOException getException() {
            return exception;
        }

        public String getStatus() {
            return status;
        }

        public long getResponseTime() {
            return responseTime;
        }

        public Response setException(IOException error) {
            this.exception = error;
            return this;
        }
    }
}
