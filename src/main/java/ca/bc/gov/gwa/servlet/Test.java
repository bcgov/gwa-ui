/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

/**
 *
 * @author aidancope
 */
public class Test {
            public static int AES_KEY_SIZE = 256 ;
        public static int IV_SIZE = 16 ;
        public static int TAG_BIT_LENGTH = 128 ;
 //       public static int IV_SIZE = 96 ;
//        public static String ALGO_TRANSFORMATION_STRING = "AES/ECB/PKCS5Padding" ;
        public static String ALGO_TRANSFORMATION_STRING = "AES/CBC/PKCS5Padding" ;
        
    static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    static public void main(String[] args) throws Throwable {
        System.out.println("Great!");
        byte[] arrayBytes = "ThisIsSpartaThisIsSparta".getBytes();
        KeySpec ks = new DESedeKeySpec(arrayBytes); // first 24 bytes
        SecretKeyFactory skf = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);
        Cipher cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
        SecretKey key = skf.generateSecret(ks);
        
            PublicKey publicKey = readPublicKey("/Users/aidancope/Solutions/sbbs/projects/bcgov-apigateway/gwa-ui/src/main/java/ca/bc/gov/gwa/servlet/test.crt");
        
            byte[] s = encrypt(publicKey, new String("https://github.com/1MansiS/java_crypto/blob/master/cipher/SecuredGCMUsage.java").getBytes());
            System.out.println(Hex.encodeHexString(s));

        
        // http://aes.online-domain-tools.com/
        
        System.out.println(encrypt(key, "some text"));

                       String messageToEncrypt = "https://github.com/1MansiS/java_crypto/blob/master/cipher/SecuredGCMUsage.java" ;
                
                byte[] aadData = "random".getBytes() ; // Any random data can be used as tag. Some common examples could be domain name...

                System.out.println("TAG = "+Hex.encodeHexString(aadData));
                // Use different key+IV pair for encrypting/decrypting different parameters

                // Generating Key
                SecretKey aesKey = loadKey(new File("/Users/aidancope/Solutions/sbbs/projects/bcgov-apigateway/gwa-ui/src/main/java/ca/bc/gov/gwa/servlet/aes.key"));
                //SecretKey aesKey = getAESKeyFromPassword("ABC",getRandomNonce()) ;
                    System.out.println("Key = "+Hex.encodeHexString(aesKey.getEncoded()));
//                try {
//                    KeyGenerator keygen = KeyGenerator.getInstance("AES") ; // Specifying algorithm key will be used for 
//                    keygen.init(AES_KEY_SIZE) ; // Specifying Key size to be used, Note: This would need JCE Unlimited Strength to be installed explicitly 
//                    aesKey = keygen.generateKey() ;
//                    
//                    System.out.println("Key = "+Hex.encodeHexString(aesKey.getEncoded()));
//                } catch(NoSuchAlgorithmException noSuchAlgoExc) { System.out.println("Key being request is for AES algorithm, but this cryptographic algorithm is not available in the environment "  + noSuchAlgoExc) ; System.exit(1) ; }

                // Generating IV
                byte iv[] = Hex.decodeHex("029cc40f79a43e1e085082900cb4fe65".toCharArray()); //new byte[IV_SIZE];
                SecureRandom secRandom = new SecureRandom() ;
                secRandom.nextBytes(iv); // SecureRandom initialized using self-seeding

                System.out.println("IV = "+new String(Hex.encodeHexString(iv)));
                // Initialize GCM Parameters
                IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
                //GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv) ;   
                
                
                byte[] encryptedText = aesEncrypt(messageToEncrypt, aesKey,  ivParamSpec, aadData) ;          

                secRandom.nextBytes(iv);
                
                System.out.println("Encrypted Text = " + Hex.encodeHexString(encryptedText)) ;

                
//                              byte[] decryptedText = aesDecrypt(encryptedText, aesKey, gcmParamSpec, aadData) ; // Same key, IV and GCM Specs for decryption as used for encryption.
//
//                System.out.println("Decrypted text " + new String(decryptedText)) ;
//
//                // Make sure not to repeat Key + IV pair, for encrypting more than one plaintext.
//                secRandom.nextBytes(iv);

        /*        
            PublicKey publicKey = readPublicKey("/Users/aidancope/Solutions/sbbs/projects/bcgov-apigateway/gwa-ui/src/main/java/ca/bc/gov/gwa/servlet/test.crt");
        
            byte[] s = encrypt(publicKey, new String("hello").getBytes());
            System.out.println(new String(Base64.encode(s)));
            
            */
    }
    
       static public byte[] readFileBytes(String filename) throws IOException {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }
    static public PublicKey readPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PemReader a;
        ByteArrayInputStream tube = new ByteArrayInputStream(readFileBytes(filename));

        Reader fRd = new BufferedReader(new InputStreamReader(tube));

        PemReader pr = new PemReader(fRd);
        PemObject po = pr.readPemObject();
        //PKCS8EncodedKeySpec publicSpec = new PKCS8EncodedKeySpec(po.getContent());
        
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(po.getContent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicSpec);
    }
    
        static public byte[] encrypt(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }

    static public String encrypt(SecretKey key, String unencryptedString) {
        String encryptedString = null;
        try {
            Cipher cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = unencryptedString.getBytes("UTF8");
            byte[] encryptedText = cipher.doFinal(plainText);
            encryptedString = new String(Base64.encodeBase64(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }

        static public byte[] aesEncrypt(String message, SecretKey aesKey, IvParameterSpec paramSpec, byte[] aadData) throws InvalidKeyException {
                Cipher c = null ;

                try {
                        c = Cipher.getInstance(ALGO_TRANSFORMATION_STRING); // Transformation specifies algortihm, mode of operation and padding
                }catch(NoSuchAlgorithmException noSuchAlgoExc) {System.out.println("Exception while encrypting. Algorithm being requested is not available in this environment " + noSuchAlgoExc); System.exit(1); }
                 catch(NoSuchPaddingException noSuchPaddingExc) {System.out.println("Exception while encrypting. Padding Scheme being requested is not available this environment " + noSuchPaddingExc); System.exit(1); }

                //c.init(Cipher.ENCRYPT_MODE, aesKey);

                try {
                    c.init(Cipher.ENCRYPT_MODE, aesKey, paramSpec, new SecureRandom()) ;
                } catch(InvalidKeyException invalidKeyExc) {System.out.println("Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized " + invalidKeyExc) ; System.exit(1); }
                 catch(InvalidAlgorithmParameterException invalidAlgoParamExc) {System.out.println("Exception while encrypting. Algorithm parameters being specified are not valid " + invalidAlgoParamExc) ; System.exit(1); }

//                try { 
//                    c.updateAAD(aadData) ; // add AAD tag data before encrypting
//                }catch(IllegalArgumentException illegalArgumentExc) {System.out.println("Exception thrown while encrypting. Byte array might be null " +illegalArgumentExc ); System.exit(1);} 
//                catch(IllegalStateException illegalStateExc) {System.out.println("Exception thrown while encrypting. CIpher is in an illegal state " +illegalStateExc); System.exit(1);} 
//                catch(UnsupportedOperationException unsupportedExc) {System.out.println("Exception thrown while encrypting. Provider might not be supporting this method " +unsupportedExc); System.exit(1);} 
               
               byte[] cipherTextInByteArr = null ;
               try {
                    cipherTextInByteArr = c.doFinal(message.getBytes()) ;
               } catch(IllegalBlockSizeException illegalBlockSizeExc) {System.out.println("Exception while encrypting, due to block size " + illegalBlockSizeExc) ; System.exit(1); }
                 catch(BadPaddingException badPaddingExc) {System.out.println("Exception while encrypting, due to padding scheme " + badPaddingExc) ; System.exit(1); }

               return cipherTextInByteArr ;
        }    
        
        public static byte[] aesDecrypt(byte[] encryptedMessage, SecretKey aesKey, IvParameterSpec _paramSpec, byte[] aadData) throws InvalidKeyException {
               Cipher c = null ;
                byte iv[] = new byte[IV_SIZE];
                SecureRandom secRandom = new SecureRandom() ;
                secRandom.nextBytes(iv); // SecureRandom initialized using self-seeding
                
                IvParameterSpec paramSpec = new IvParameterSpec(iv);

                // Initialize GCM Parameters
                //GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv) ;      
        
               try {
                   c = Cipher.getInstance(ALGO_TRANSFORMATION_STRING); // Transformation specifies algortihm, mode of operation and padding
                } catch(NoSuchAlgorithmException noSuchAlgoExc) {System.out.println("Exception while decrypting. Algorithm being requested is not available in environment " + noSuchAlgoExc); System.exit(1); }
                 catch(NoSuchPaddingException noSuchAlgoExc) {System.out.println("Exception while decrypting. Padding scheme being requested is not available in environment " + noSuchAlgoExc); System.exit(1); }  

               c.init(Cipher.DECRYPT_MODE, aesKey);
//                try {
//                    c.init(Cipher.DECRYPT_MODE, aesKey, paramSpec) ;
//                } catch(InvalidKeyException invalidKeyExc) {System.out.println("Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized " + invalidKeyExc) ; System.exit(1); }
//                 catch(InvalidAlgorithmParameterException invalidParamSpecExc) {System.out.println("Exception while encrypting. Algorithm Param being used is not valid. " + invalidParamSpecExc) ; System.exit(1); }

//                try {
//                    c.updateAAD(aadData) ; // Add AAD details before decrypting
//                }catch(IllegalArgumentException illegalArgumentExc) {System.out.println("Exception thrown while encrypting. Byte array might be null " +illegalArgumentExc ); System.exit(1);}
//                catch(IllegalStateException illegalStateExc) {System.out.println("Exception thrown while encrypting. CIpher is in an illegal state " +illegalStateExc); System.exit(1);}
                
                byte[] plainTextInByteArr = null ;
                try {
                    plainTextInByteArr = c.doFinal(encryptedMessage) ;
                } catch(IllegalBlockSizeException illegalBlockSizeExc) {System.out.println("Exception while decryption, due to block size " + illegalBlockSizeExc) ; System.exit(1); }
                 catch(BadPaddingException badPaddingExc) {System.out.println("Exception while decryption, due to padding scheme " + badPaddingExc) ; System.exit(1); }

                return plainTextInByteArr ;
        }   
        
    // AES key derived from a password
    public static SecretKey getAESKeyFromPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // iterationCount = 65536
        // keyLength = 256
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;
    }       
    
    public static byte[] getRandomNonce() {
          byte[] nonce = new byte[16];
          new SecureRandom().nextBytes(nonce);
          return nonce;
    }
    
public static SecretKey loadKey(File file) throws IOException, DecoderException
{
    String data = new String(readFileToByteArray(file));
    byte[] encoded;
    encoded = decodeHex(data.toCharArray());
    return new SecretKeySpec(encoded, "AES");
}

}

