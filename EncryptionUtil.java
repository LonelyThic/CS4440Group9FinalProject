import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/*
 * Handles AES encryption and decryption
 */
public class EncryptionUtil {
    private static final String KEY = "1234567890123456";

    public static String encrypt(String message) {
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encryptedMessage) {
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decoded = Base64.getDecoder().decode(encryptedMessage);
            return new String(cipher.doFinal(decoded));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Encrypt raw image bytes by first converting them to Base64 text,
     * then encrypting that text.
     */
    public static String encryptImage(byte[] imageBytes) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return encrypt(base64Image);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Decrypt encrypted image text back into raw image bytes.
     */
    public static byte[] decryptImage(String encryptedImage) {
        try {
            String base64Image = decrypt(encryptedImage);

            if (base64Image == null) {
                return null;
            }

            return Base64.getDecoder().decode(base64Image);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}