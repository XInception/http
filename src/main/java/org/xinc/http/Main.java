package org.xinc.http;

import org.xinc.http.server.HttpServer;
import org.xinc.http.server.HttpServerProperty;

import java.io.IOException;

public class Main {

    private static HttpServer server;

    public static void main(String[] args) {
        server = new HttpServer();
        try {
            server.start(new HttpServerProperty("/application-server.properties"));
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
