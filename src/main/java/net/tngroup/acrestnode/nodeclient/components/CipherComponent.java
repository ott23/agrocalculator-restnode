package net.tngroup.acrestnode.nodeclient.components;

import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class CipherComponent {

    String encodeDes(String input, String keyString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("DES");
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(encValue), StandardCharsets.UTF_8);
    }

    String decodeDes(String input, String keyString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("DES");
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.getDecoder().decode(input);
        byte[] decValue = cipher.doFinal(decodedValue);
        return new String(decValue, StandardCharsets.UTF_8);
    }
}