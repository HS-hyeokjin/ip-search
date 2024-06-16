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

