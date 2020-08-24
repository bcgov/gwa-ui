package ca.bc.gov.gwa.servlet;

import static ca.bc.gov.gwa.servlet.authentication.GitHubPrincipal.DEVELOPER_ROLE;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
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

@WebServlet(urlPatterns = "/encrypt", loadOnStartup = 1)
public class EncrypterServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            PublicKey publicKey = readPublicKey("/tls/tls.crt");
            byte[] message = request.getParameter("v").getBytes("UTF8");
            byte[] secret = encrypt(publicKey, message);
            response.getWriter().println(new String(secret, "UTF8"));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EncrypterServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Unable to generate");
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(EncrypterServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Unable to generate");
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(EncrypterServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Unable to generate");
        } catch (InvalidKeyException ex) {
            Logger.getLogger(EncrypterServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Unable to generate");
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(EncrypterServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Unable to generate");
        } catch (BadPaddingException ex) {
            Logger.getLogger(EncrypterServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException("Unable to generate");
        }
    }

    public byte[] readFileBytes(String filename) throws IOException {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    public PublicKey readPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicSpec);
    }

    public PrivateKey readPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public byte[] encrypt(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(PrivateKey key, byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(ciphertext);
    }
}
