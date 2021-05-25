package org.xinc.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Admin
 */
public class HttpServerProperty {

    String server;
    Integer port;

    public HttpServerProperty(String s) throws IOException {
        this.loadProperty(s);
    }

    public void loadProperty(InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        this.server=properties.getProperty("app.api.server");
        this.port=Integer.parseInt(properties.getProperty("app.api.port"));
    }

    public void loadProperty(String path) throws IOException {
        loadProperty( this.getClass().getResourceAsStream(path));
    }
}
