package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private Properties properties = new Properties();

    public PropertiesLoader() {
        try (InputStream input = new FileInputStream("src/resources/config.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            System.out.println("api key 오류");
        }
    }

    public String getGoogleApiKey() {
        return properties.getProperty("google.api.key");
    }

    public String getIp2locationApiKey(){
        return properties.getProperty("ip2.api.key");
    }
}
