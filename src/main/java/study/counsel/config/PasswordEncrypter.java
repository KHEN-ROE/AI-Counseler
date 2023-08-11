package study.counsel.config;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class PasswordEncrypter {

    public String encrypt(String password) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(password.getBytes());
            byte byteData[] = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : byteData) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }


}
