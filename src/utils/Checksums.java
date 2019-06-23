package utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class Checksums {

    public static String calculateMD5(String filename) {
        try (InputStream fis = new FileInputStream(filename)) {
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            while ((numRead = fis.read(buffer)) != -1) {
                complete.update(buffer, 0, numRead);
            }
            var result = new StringBuilder();
            for (byte b: complete.digest()) {
                result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
