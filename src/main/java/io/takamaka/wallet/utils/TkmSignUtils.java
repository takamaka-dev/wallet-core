/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.exceptions.KeyDecodeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.UrlBase64;
import static org.apache.commons.codec.digest.DigestUtils.digest;

/**
 *
 * @author giovanni
 */
@Slf4j
public class TkmSignUtils {
    
    public static final AsymmetricCipherKeyPair stringPublicKeyToKeyPairBCEd25519(String publicKey) throws KeyDecodeException {
        try {
            UrlBase64 b64e = new UrlBase64();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            UrlBase64.decode(publicKey, baos);
            Ed25519PublicKeyParameters edPublicKey = new Ed25519PublicKeyParameters(baos.toByteArray(), 0);
            baos.close();
            AsymmetricCipherKeyPair ackp = new AsymmetricCipherKeyPair(edPublicKey, null);
            return ackp;
        } catch (Exception ex) {
            log.warn("error in conversion from string PublicKey To Key Pair BCEd25519", ex);
            throw new KeyDecodeException(ex);
        }
    }
    
    public static final AsymmetricCipherKeyPair stringPublicKeyToKeyPairBCQTESLAPSSC1(String publicKey) throws KeyDecodeException {
        try {
            //UrlBase64 b64e = new UrlBase64();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            UrlBase64.decode(publicKey, baos);
            QTESLAPublicKeyParameters edPublicKey = new QTESLAPublicKeyParameters(QTESLASecurityCategory.PROVABLY_SECURE_I, baos.toByteArray());
            baos.close();
            AsymmetricCipherKeyPair ackp = new AsymmetricCipherKeyPair(edPublicKey, null);
            return ackp;
        } catch (Exception ex) {
            log.warn("error in conversion from string Public Key To Key Pair BCQTESLAPSSC1", ex);
            throw new KeyDecodeException(ex);
        }
    }

    /**
     * return the base64 rappresentation of SHA3-128 input hash
     *
     */
    public static final String Hash128(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.Hash(input, FixedParameters.HASH_256_ALGORITHM);
    }

    /**
     * return the base64 rappresentation of SHA3-256 input hash
     *
     * @param input
     *
     */
    public static final String Hash256(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.Hash(input, FixedParameters.HASH_256_ALGORITHM);
    }
    
    public static final String Hash256B64URL(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return Hash256(input);
    }
    
    public static final String Hash384(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.Hash(input, FixedParameters.HASH_384_ALGORITHM);
    }
    
    public static final String Hash384B64URL(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return Hash384(input);
    }
    
    public static final String Hash512(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.Hash(input, FixedParameters.HASH_512_ALGORITHM);
    }
    
    public static final String Hash512B64URL(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return Hash512(input);
    }
    
    public static final byte[] Hash256byte(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.HashByte(input, FixedParameters.HASH_256_ALGORITHM);
    }
    
    public static final byte[] Hash384byte(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.HashByte(input, FixedParameters.HASH_384_ALGORITHM);
    }

    /**
     * internal method used in the "EXPRESS" version for psql tables. Use
     *
     * @Hash384ToHex to bookmark and sohrtened public addresses (meta addresses)
     *
     * @param input
     * @return
     */
    public static final String getShortenedAddr(String input) {
        try {
            byte[] HashByte = Hash256byte(input);
            return TkmSignUtils.fromByteArrayToHexString(HashByte);
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("error in conversion to shortened address", ex);
            return null;
        }
    }
    
    public static final byte[] Hash512byte(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.HashByte(input, FixedParameters.HASH_512_ALGORITHM);
    }
    
    public static final String PWHashB64(String input, String salt, int iterations, int bitLegnthKey) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {
        try {
            PBEKeySpec spec = new PBEKeySpec(input.toCharArray(), salt.getBytes(), iterations, bitLegnthKey);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(FixedParameters.HASH_PWH_ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            ByteBuffer bb = ByteBuffer.wrap(hash);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            UrlBase64.encode(bb.array(), baos);
            bb.clear();
            String out = baos.toString(FixedParameters.CHARSET.name());
            baos.close();
            return out;
        } catch (Exception ex) {
            Logger.getLogger(TkmSignUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static final byte[] PWHash(String input, String salt, int iterations, int bitLegnthKey) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {
        PBEKeySpec spec = new PBEKeySpec(input.toCharArray(), salt.getBytes(), iterations, bitLegnthKey);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(FixedParameters.HASH_PWH_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     *
     * @param input
     * @param hashType
     * @return
     * @throws HashEncodeException
     * @throws HashAlgorithmNotFoundException
     * @throws HashProviderNotFoundException
     */
    private static final String Hash(String input, String hashType) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        try {
            //Base64 b64enc = new Base64();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //string -> byte[]
            byte[] inputBytes = input.getBytes(FixedParameters.CHARSET);
            //set hashing provider
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            MessageDigest digest = MessageDigest.getInstance(hashType, BouncyCastleProvider.PROVIDER_NAME);
            digest.reset();
            ByteBuffer bb = ByteBuffer.wrap(inputBytes);
            digest.update(bb);
            UrlBase64.encode(digest.digest(), baos);
            bb.clear();
            String out = baos.toString(FixedParameters.CHARSET.name());
            baos.close();
            return out;
            
        } catch (Exception ex) {
            log.error("hash funcion error", ex);
            throw new HashEncodeException(ex);
        }
    }
    
    public static final byte[] Hash256Byte(byte[] input, String hashType) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[] result = null;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        MessageDigest digest = MessageDigest.getInstance(hashType, BouncyCastleProvider.PROVIDER_NAME);
        digest.reset();
        ByteBuffer bb = ByteBuffer.wrap(input);
        digest.update(bb);
        result = digest.digest();
        digest.reset();
        bb.clear();
        return result;
    }
    
    private static byte[] HashByte(String input, String hashType) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        try {
            //Base64 b64enc = new Base64();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //string -> byte[]
            byte[] inputBytes = input.getBytes(FixedParameters.CHARSET);
            //set hashing provider
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            MessageDigest digest = MessageDigest.getInstance(hashType, BouncyCastleProvider.PROVIDER_NAME);
            digest.reset();
            ByteBuffer bb = ByteBuffer.wrap(inputBytes);
            digest.update(bb);
            UrlBase64.encode(digest.digest(), baos);
            bb.clear();
            byte[] out = baos.toByteArray();
            baos.close();
            return out;
            
        } catch (Exception ex) {
            log.error("hash to byte error", ex);
            throw new HashEncodeException(ex);
        }
    }

    /**
     * return the HEX rappresentation of SHA3-256 input hash
     *
     * @param input
     *
     *
     */
    public static final String Hash256ToHex(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.HashToHex(input, FixedParameters.HASH_256_ALGORITHM);
    }
    
    public static final String Hash384ToHex(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.HashToHex(input, FixedParameters.HASH_384_ALGORITHM);
    }
    
    public static final String Hash512ToHex(String input) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        return TkmSignUtils.HashToHex(input, FixedParameters.HASH_512_ALGORITHM);
    }
    
    public static final String fromByteArrayToB64URL(byte[] input) {
        String out = null;
        try {
            //UrlBase64 b64e = new UrlBase64();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Base64.encode(input, baos);
            UrlBase64.encode(input, baos);
            out = baos.toString(FixedParameters.CHARSET.name());
            baos.close();
        } catch (Exception ex) {
            Logger.getLogger(TkmSignUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }
    
    private static final byte[] streamToHash(InputStream in, String hashType) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        byte[] res = null;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        MessageDigest digestMsg = MessageDigest.getInstance(hashType, BouncyCastleProvider.PROVIDER_NAME);
        byte[] digestRes = digest(digestMsg, in);
        //org.bouncycastle.crypto.io.DigestInputStream dis = new org.bouncycastle.crypto.io.DigestInputStream(in,digest(digest, in));
        res = digestRes;
        return res;
    }
    
    public static final byte[] StreamToHash256Byte(InputStream in) {
        try {
            return streamToHash(in, FixedParameters.HASH_256_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException ex) {
            log.error("stream to hash error", ex);
            return null;
        }
    }
    
    public static final byte[] StreamToHash160Byte(InputStream in) {
        try {
            return streamToHash(in, FixedParameters.HASH_160_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException ex) {
            log.error("stream to hash 160 error", ex);
            return null;
        }
    }
    
    public static final byte[] StreamToHash384Byte(InputStream in) {
        try {
            return streamToHash(in, FixedParameters.HASH_384_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException ex) {
            log.error("stream to hash 384 error", ex);
            return null;
        }
    }
    
    public static final byte[] StreamToHash512Byte(InputStream in) {
        try {
            return streamToHash(in, FixedParameters.HASH_512_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException ex) {
            log.error("stream to hash 512 error", ex);
            return null;
        }
    }
    
    public static final String fromByteArrayToB64(byte[] input) {
        String out = null;
        try {
            //UrlBase64 b64e = new UrlBase64();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Base64.encode(input, baos);
            Base64.encode(input, baos);
            out = baos.toString(FixedParameters.CHARSET.name());
            baos.close();
        } catch (Exception ex) {
            log.error("byte array to base64 error", ex);
        }
        return out;
    }
    
    private static String HashToHex(String input, String hashType) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        try {
            Hex henc = new Hex();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //string -> byte[]
            byte[] inputBytes = input.getBytes(FixedParameters.CHARSET);
            //set hashing provider
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            MessageDigest digest = MessageDigest.getInstance(hashType, BouncyCastleProvider.PROVIDER_NAME);
            digest.reset();
            ByteBuffer bb = ByteBuffer.wrap(inputBytes);
            digest.update(bb);
            Hex.encode(digest.digest(), baos);
            bb.clear();
            return baos.toString(FixedParameters.CHARSET.name());
            
        } catch (Exception ex) {
            log.error("hash to hex", ex);
            throw new HashEncodeException(ex);
        }
    }

    /**
     * from url base64 to byte, null if invalid
     *
     * @param input
     * @return
     */
    public static final byte[] fromB64URLToByteArray(String input) {
        byte[] res = null;
        try {
            //UrlBase64 b64e = new UrlBase64();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            UrlBase64.decode(input, baos);
            res = baos.toByteArray();
            
            baos.close();
        } catch (Exception ex) {
            log.error("base64url to byte array error", ex);
        }
        return res;
    }

    /**
     * from standard base64 to byte, null if invalid
     *
     * @param input
     * @return
     */
    public static final byte[] fromB64ToByteArray(String input) {
        byte[] res = null;
        try {
            //UrlBase64 b64e = new UrlBase64();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64.decode(input, baos);
            res = baos.toByteArray();
            
            baos.close();
        } catch (Exception ex) {
            log.error("base64 to byte array error", ex);
        }
        return res;
    }

    /**
     * convert transactionbox "from", "to" address to hex format
     *
     * @param input
     * @return
     */
    public static final String fromB64UrlToHEX(String input) {
        byte[] b64ToByte = fromB64URLToByteArray(input);
        if (b64ToByte != null) {
            return fromByteArrayToHexString(b64ToByte);
        }
        return null;
    }

    /**
     * convert transactionbox "from", "to" address to hex format
     *
     * @param input
     * @return
     */
    public static final String fromB64ToHEX(String input) {
        byte[] b64ToByte = fromB64ToByteArray(input);
        if (b64ToByte != null) {
            return fromByteArrayToHexString(b64ToByte);
        }
        return null;
    }
    
    public static final String fromB64URLToB64(String b64URLKey) {
        byte[] b64URLToByte = fromB64URLToByteArray(b64URLKey);
        if (b64URLToByte != null) {
            return fromByteArrayToB64(b64URLToByte);
        }
        return null;
    }
    
    public static final String fromB64ToB64URL(String b64URL) {
        byte[] b64ToByte = fromB64ToByteArray(b64URL);
        if (b64ToByte != null) {
            return fromByteArrayToB64URL(b64ToByte);
        }
        return null;
    }
    
    public static final String fromHexToB64(String hexMessage) {
        String res = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //byte[] mBytes = hexMessage.getBytes(FixedParameters.CHARSET);
            Hex.decode(hexMessage, baos);
            byte[] toByteArray = baos.toByteArray();
            baos.close();
            res = fromByteArrayToB64(toByteArray);
        } catch (Exception ex) {
            log.error("hex to base64 error", ex);
        }
        return res;
    }

    /**
     * from byte to hex, null if invalid
     *
     * @param input
     * @return
     */
    public static final String fromByteArrayToHexString(byte[] input) {
        String res = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Hex.encode(input, baos);
            res = baos.toString(FixedParameters.CHARSET.name());
            baos.close();
        } catch (Exception ex) {
           log.error("from byte array to hex string error", ex);
        }
        return res;
    }
    


    /**
     * Return the hex of the Ripemd160 hash of the input string.
     *
     * @param publicKey
     * @return
     */
    public static final String Hash160ToHex(String publicKey) {
        try {
            return TkmSignUtils.HashToHex(publicKey, FixedParameters.HASH_160_ALGORITHM);
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("hash 160 to hex", ex);
        }
        return null;
    }
    
    public static final String fromStringToHexString(String message) {
        String res = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] mBytes = message.getBytes(FixedParameters.CHARSET);
            Hex.encode(mBytes, baos);
            res = baos.toString(FixedParameters.CHARSET.name());
            baos.close();
        } catch (Exception ex) {
            log.error("string to hex string error", ex);
        }
        return res;
    }

    /**
     *
     * @param message
     * @return null if decode fail
     */
    public static final byte[] fromHexToByteArray(String message) {
        byte[] res = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //byte[] mBytes = hexMessage.getBytes(FixedParameters.CHARSET);
            Hex.decode(message, baos);
            res = baos.toByteArray();
            baos.close();
        } catch (Exception ex) {
            log.error("hex to byte array error", ex);
        }
        return res;
    }
    
    public static final String fromHexToString(String hexMessage) {
        String res = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //byte[] mBytes = hexMessage.getBytes(FixedParameters.CHARSET);
            Hex.decode(hexMessage, baos);
            res = baos.toString(FixedParameters.CHARSET.name());
            baos.close();
        } catch (Exception ex) {
           log.error("from hex to string error", ex);
        }
        return res;
    }
    
    public static final byte[] fromStringToByteArray(String message) {
        return message.getBytes(FixedParameters.CHARSET);
    }
    
    public static final char[] fromStringToCharArray(String message) {
        return message.toCharArray();//message.getBytes(FixedParameters.CHARSET);
    }
    
    public static final String fromCharArrayToString(char[] message) {
        return new String(message);
    }
    
    public static final String fromStringToBase64URL(String message) {
        return fromByteArrayToB64URL(fromStringToByteArray(message));
    }
    
    public static final String fromBase64URLToString(String message) {
        try {
            return new String(fromB64URLToByteArray(message), FixedParameters.CHARSET.name());
            
        } catch (UnsupportedEncodingException ex) {
            log.error("from b46url to string error", ex);
            return null;
        }
    }
    
    private static long textToLongID(String input, int bitLen) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            byte[] byIn = fromStringToByteArray(input);
            SHAKEDigest sh = new SHAKEDigest(bitLen);
            sh.reset();
            sh.update(byIn, 0, byIn.length);
            byte[] byteRes = new byte[bitLen / 8];
            sh.doFinal(byteRes, 0, bitLen / 8);
            //MessageDigest digest = MessageDigest.getInstance(hashType, BouncyCastleProvider.PROVIDER_NAME);
            ByteBuffer buffer = ByteBuffer.allocate(bitLen / 8);
            buffer.put(byteRes);
            buffer.flip();//need flip 
            return buffer.getLong();
        } catch (Exception ex) {
            log.error("text to longId error", ex);
            throw new HashEncodeException(ex);
        }
    }

    /**
     *
     * @return a random long starting from 1 to MaxLong
     */
    public static final long getRandomPositiveLong() {
        long nextLong = RandomUtils.nextLong(1, Long.MAX_VALUE);
        return nextLong;
    }
    
    public static final long getLongID(String text) {
        try {
            return textToLongID(text, 256);
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("long id generation error", ex);
            return 0L;
        }
    }
    
    public static final String getHexCRC(String addressBASE64URL) {
        System.out.println("bytes: " + Arrays.toString(fromB64URLToByteArray(addressBASE64URL)));
        byte[] ba = fromB64URLToByteArray(addressBASE64URL);
        int[] ia = new int[ba.length];
        for (int i = 0; i < ba.length; i++) {
            byte b = ba[i];
            ia[i] = ((int) b) & 0xff;
            
        }
        System.out.println("AI: " + Arrays.toString(ia));
        return getHexCRC(fromB64URLToByteArray(addressBASE64URL));
    }
    
    private static final String getHexCRC(byte[] input) {
        Checksum checksum = new CRC32();
        checksum.reset();
        checksum.update(input);
        Long value = checksum.getValue();
        System.out.println("LONG: " + value);
        return Long.toHexString(value).substring(0, 4);
    }
    
    public static final void main(String[] args) throws HashEncodeException, HashAlgorithmNotFoundException, HashProviderNotFoundException {
        System.out.println(getLongID("gatto"));
    }
    
}
