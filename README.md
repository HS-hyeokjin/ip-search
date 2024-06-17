# [스윙] IP 추적 프로그램

## [**자바(swing) - IP/도메인 추적 프로그램 구현하기 ]**

자바에 스윙과 외부 API 를 이용해서 IP를 추적하고 지도에 찍을 수 있는 프로그램을 구현합니다.

[ 메인 화면 ]

<img src = "https://github.com/HS-hyeokjin/ip-search/assets/90303458/90f338f5-4558-4a70-b44c-807fe56461e1">

[도메인 검색] 
<img src = "https://github.com/HS-hyeokjin/ip-search/assets/90303458/d45e416a-0944-4fb4-a85f-fd1327d5fdf8">


[ ip 검색 ]
<img src = "https://github.com/HS-hyeokjin/ip-search/assets/90303458/32761ec2-74b4-4de2-ae38-fbaa7128a916">


### API 서버 정보

### [ip2location]

IP 나 도메인의 정보를 조회하기 위한 API 입니다.
해당 사이트에 가입해서 API 키를 발급받으시면 됩니다.

[https://www.ip2location.io/](https://www.ip2location.io/)

### [GoogleMap]

ip위치를 표기하기위해 ip2location에서 가져온 위도 경도값을 이용하여  이미지를 가져올 수 있도록 해당 사이트에서 API 키를 발급받아야 합니다.

[https://console.cloud.google.com/google/maps-apis/onboard?utm_source=Docs_GS_Button&ref=https:%2F%2Fdevelopers.google.com%2Fmaps%2F](https://console.cloud.google.com/google/maps-apis/onboard?utm_source=Docs_GS_Button&ref=https:%2F%2Fdevelopers.google.com%2Fmaps%2F)

### [ 키값 저장]

먼저 키값을 저장하고 외부로부터 감추기 위해 properties 파일을 만들고 위에 사이트로부터 받은 키를 저장해야 합니다.
그리고 PropertiesLoader  클래스를 만들어서 필요한 API 키를 공급받아서 사용하면 됩니다.

< properties > 

```java
google.api.key=구글 api 키
ip2.api.key=자신의 ip2 api 키
```

< PropertiesLoader .class >

```java
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

```

### [ ip 데이터를 받을 dto 클래스 생성 ]

ip 정보를 저장할 수 있는 dto 클래스가 필요합니다.
아래는 ip2location API 로 정보를 요청하면 받아올 수 있는 JSON데이터 예시이며, 
아래에 데이터를 이용하여 IpInfoResponseDto  클래스를 만듭니다 .

*코드가 너무 길어서 get,set 메서드는 생략했습니다.

< 응답예시 >

```json
{
  "ip": "8.8.8.8",
  "country_code": "US",
  "country_name": "United States of America",
  "region_name": "California",
  "city_name": "Mountain View",
  "latitude": 37.38605,
  "longitude": -122.08385,
  "zip_code": "94035",
  "time_zone": "-07:00",
  "asn": "15169",
  "as": "Google LLC",
  "is_proxy": false
}
```

< IpInfoResponseDto.class >

```java
package dto.response;

public class IpInfoResponseDto {
    private String ip;
    private String country_code;
    private String country_name;
    private String region_name;
    private String cityName;
    private double latitude;
    private double longitude;
    private String zip_code;
    private String time_zone;
    private String asn;
    private String as;

   // Getter Setter 메서드 생략
}

```

### [ IP 정보를 받아올 수 있는 API 연결 클래스 생성 ]

ip api 정보를 받아오기 위해 Ip2LocationApi  클래스를 선언합니다.

먼저 입력받은 값이 순수 ip 인지 도메인 네임인지 구분하기 위해 domainResolver 를 만들고 도메인 네임의 경우 InetAddress 객체를 사용하여 ip 값으로 변환합니다.

 그리고 gui에서 전달받은 ip 값과 properties의 키값을 이용하여 아래 주소로 요청합니다.

> api 주소 : [https://api.ip2location.io/?key=키값 &ip=](https://api.ip2location.io/?key=%s&ip=)아이피
> 

여기서 응답값은 JSON 인데 파싱을 위해 GSON 라이브러리를 사용하였습니다

파싱할때만 사용되므로 어떤 라이브러리를 쓰던 상관 없을 것 같습니다 :)

< DomainResolver.class >

```java
package resolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DomainResolver {

    public String domainToIp(String domain) {
        try {
            InetAddress inetAddress = InetAddress.getByName(domain);
            String ipAddress = inetAddress.getHostAddress();
            return ipAddress;
        } catch (UnknownHostException e) {
            System.out.println("IP 주소를 찾을 수 없습니다.");
        }
        return domain;
    }
}

```

< Ip2LocationApi.class >

```java
package api;

import com.google.gson.Gson;
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
                System.out.println("IP 요청 실패: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static IpInfoResponseDto parseJsonToDto(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, IpInfoResponseDto.class);
    }
}

```

### [ 구글 지도 정보를 받아올 수 있는 API 연결 클래스 생성 ]

Ip2LocationApi 통해 응답을 받으면, 위도와 경도값을 알 수 있습니다
여기서 나온 위도와 경도값을 입력하여 구글 api 요청하면 해당 주소의 이미지 값을 받아올 수 있습니다.

> API 주소 : https://maps.googleapis.com/maps/api/staticmap
> 

 

```java
package api;

import config.PropertiesLoader;
import dto.response.IpInfoResponseDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.swing.ImageIcon;

public class GoogleMapApi {
    private static final PropertiesLoader propertiesLoader = new PropertiesLoader();
    private static final String API_KEY = propertiesLoader.getGoogleApiKey();

    public void searchPointMap(IpInfoResponseDto location) {

        String imageURL = String.format(
                "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=1000x600&maptype=roadmap&markers=color:red%%7Clabel:C%%7C%f,%f&key=%s",
                location.getLatitude(), location.getLongitude(), location.getLatitude(), location.getLongitude(), API_KEY
        );
        try {
            URL url = new URL(imageURL);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream("map.jpg");
            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            is.close();
            os.close();
        } catch (Exception e) {
            System.out.println("이미지 로드 실패");
        }
    }

    public ImageIcon getMap() {
        return new ImageIcon((new ImageIcon("map.jpg")).getImage().getScaledInstance(600, 550, java.awt.Image.SCALE_SMOOTH));
    }

    public void fileDelete() {
        File file = new File("map.jpg");
        file.delete();
    }
}

```

### [ 스윙을 이용한 GUI 인터페이스 구현 ]

먼저 필요한 컴포넌트를 정의하고 차례대로 위치를 끼워 맞췄습니다
가장 대표적인 부분으로 검색창 , ip응답 창, 지도맵으로 나눴으며, ui 는 어떻게 해도 상관 없을 것 같습니다.

 IpSearchGui() 메서드는 실행 기본값을 설정하고 실제 요청 후 이뤄지는 메서드는 setIpInfo() 메서드로, 메서드가 실행되면 ip 요청과 동시에 지도 요청이 이뤄지도록 구현했습니다.

```java
package gui;

import api.GoogleMapApi;
import api.Ip2LocationApi;
import dto.response.IpInfoResponseDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IpSearchGui extends JFrame {

    private GoogleMapApi googleMapApi = new GoogleMapApi();

    private JTextField ipTextField = new JTextField(30);
    private JButton searchButton = new JButton("검색");

    private JTextArea ipInfoTextArea = new JTextArea(10, 30);

    private JLabel map = new JLabel();

    private JPanel mainPanel = new JPanel(new BorderLayout());
    private JPanel searchPanel = new JPanel(new BorderLayout());
    private JPanel resultPanel = new JPanel(new BorderLayout());
    private JPanel mapPanel = new JPanel(new BorderLayout());

    public IpSearchGui() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocation(200, 200);

        searchPanel.add(ipTextField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        ipInfoTextArea.setEditable(false);
        resultPanel.add(new JScrollPane(ipInfoTextArea), BorderLayout.CENTER);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(resultPanel, BorderLayout.CENTER);

        mapPanel.add(map, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.WEST);
        add(mapPanel, BorderLayout.CENTER);

        searchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String ip = ipTextField.getText();
                setIpInfo(ip);
            }
        });

        ipInfoTextArea.setText("Domain name 또는 IP Address로 검색하세요..\n"+
                "ex) www.naver.com 또는 xxx.xxx.xxx.xxx\n\n\n" +
                "예시 사이트 \n Google Public DNS : 8.8.8.8\n" +
                "Cloudflare DNS : 1.1.1.1\n" +
                "Facebook : 69.63.176.13\n" +
                "YouTube : 142.250.72.206\n" +
                "AWS : 54.239.28.8");

        setVisible(true);
    }

    public void setIpInfo(String ip) {
        String response = Ip2LocationApi.getIpInfo(ip);
        if (response != null) {
            IpInfoResponseDto ipInfo = Ip2LocationApi.parseJsonToDto(response);
            ipInfoTextArea.setText("IP 정보: " + ipInfo.getIp() + "\n");
            ipInfoTextArea.append("국가 코드: " + ipInfo.getCountry_code() + "\n");
            ipInfoTextArea.append("국가 정보 : " + ipInfo.getCountry_name() + "\n");
            ipInfoTextArea.append("지역 정보 : " + ipInfo.getRegion_name() + "\n");
            ipInfoTextArea.append("도시 정보 : " + ipInfo.getCityName() + "\n");
            ipInfoTextArea.append("위도: " + ipInfo.getLatitude() + "\n");
            ipInfoTextArea.append("경도: " + ipInfo.getLongitude() + "\n");
            ipInfoTextArea.append("우편번호: " + ipInfo.getZip_code() + "\n");
            ipInfoTextArea.append("asn: " + ipInfo.getAsn() + "\n");
            ipInfoTextArea.append("as: " + ipInfo.getAs() + "\n");

            googleMapApi.searchPointMap(ipInfo);
            map.setIcon(googleMapApi.getMap());
            googleMapApi.fileDelete();
        }
    }
}

```

### [ 실행 메서드 ]

마지막으로 main 메서드를 사용하여  gui 를 실행시키면 됩니다.

< Main.class >

```java
package main;

import gui.IpSearchGui;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IpSearchGui());
    }
}
```
