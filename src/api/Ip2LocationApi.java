package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dto.response.IpInfoResponseDto;
import config.PropertiesLoader;
import resolver.DomainResolver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Ip2LocationApi {

    private static final PropertiesLoader propertiesLoader = new PropertiesLoader();
    private static final String API_KEY = propertiesLoader.getIp2locationApiKey();
    private static final DomainResolver domainResolver = new DomainResolver();

    public static String getIpInfo(String ip) {
        if (ip.contains("www.")) {
            ip = domainResolver.domainToIp(ip);
        }

        String urlString = String.format("https://api.ip2location.io/?key=%s&ip=%s", API_KEY, ip);

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                System.out.println("요청 실패: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static IpInfoResponseDto parseJsonToDto(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, IpInfoResponseDto.class);
    }
}
