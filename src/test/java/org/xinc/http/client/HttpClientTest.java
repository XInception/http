package org.xinc.http.client;

import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientTest {

    @Test
    void start() throws IOException, InterruptedException {
        HttpClient httpClient=new HttpClient(new HttpClientProperty("/application-client.properties"),null);
        httpClient.get("/1111");
        Thread.sleep(10000);
    }
}