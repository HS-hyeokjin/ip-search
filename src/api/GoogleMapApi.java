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
