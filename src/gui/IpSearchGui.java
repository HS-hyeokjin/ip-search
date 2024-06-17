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
