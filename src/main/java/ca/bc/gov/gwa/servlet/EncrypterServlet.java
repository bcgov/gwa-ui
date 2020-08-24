package ca.bc.gov.gwa.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.profile.Pac4JPrincipal;

import ca.bc.gov.gwa.util.Json;
import ca.bc.gov.gwa.v1.ApiService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

@WebServlet(urlPatterns = "/encrypt", loadOnStartup = 1)
public class EncrypterServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    public static String ALGO_TRANSFORMATION_STRING = "AES/CBC/PKCS5Padding" ;

    private static final int IV_SIZE = 16;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            byte iv[] = new byte[IV_SIZE];

            String valueToEncrypt = request.getParameter("v");

            SecretKey aesKey = loadKey(new File("/tls/aes.key"));

            SecureRandom secRandom = new SecureRandom();
            secRandom.nextBytes(iv);

            IvParameterSpec ivParamSpec = new IvParameterSpec(iv);

            byte[] encryptedText = aesEncrypt(valueToEncrypt, aesKey, ivParamSpec);

            String secret = String.format("{{%s/%s}}", Hex.encodeHexString(iv), Hex.encodeHexString(encryptedText));

            response.getWriter().println(secret);
        } catch (DecoderException ex) {
            Logger.getLogger(EncrypterServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Unable to encrypt");
        } catch (InvalidKeyException ex) {
            Logger.getLogger(EncrypterServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Unable to encrypt");
        }
    }

    public static SecretKey loadKey(File file) throws IOException, DecoderException {
        String data = new String(readFileToByteArray(file));
        byte[] encoded;
        encoded = decodeHex(data.toCharArray());
        return new SecretKeySpec(encoded, "AES");
    }

    public byte[] aesEncrypt(String message, SecretKey aesKey, IvParameterSpec paramSpec) throws InvalidKeyException {
        Cipher c = null;

        try {
            c = Cipher.getInstance(ALGO_TRANSFORMATION_STRING); // Transformation specifies algortihm, mode of operation and padding
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println("Exception while encrypting. Algorithm being requested is not available in this environment " + noSuchAlgoExc);
            System.exit(1);
        } catch (NoSuchPaddingException noSuchPaddingExc) {
            System.out.println("Exception while encrypting. Padding Scheme being requested is not available this environment " + noSuchPaddingExc);
            System.exit(1);
        }

        try {
            c.init(Cipher.ENCRYPT_MODE, aesKey, paramSpec, new SecureRandom());
        } catch (InvalidKeyException invalidKeyExc) {
            System.out.println("Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized " + invalidKeyExc);
            System.exit(1);
        } catch (InvalidAlgorithmParameterException invalidAlgoParamExc) {
            System.out.println("Exception while encrypting. Algorithm parameters being specified are not valid " + invalidAlgoParamExc);
            System.exit(1);
        }

        byte[] cipherTextInByteArr = null;
        try {
            cipherTextInByteArr = c.doFinal(message.getBytes());
        } catch (IllegalBlockSizeException illegalBlockSizeExc) {
            System.out.println("Exception while encrypting, due to block size " + illegalBlockSizeExc);
            System.exit(1);
        } catch (BadPaddingException badPaddingExc) {
            System.out.println("Exception while encrypting, due to padding scheme " + badPaddingExc);
            System.exit(1);
        }

        return cipherTextInByteArr;
    }

}
