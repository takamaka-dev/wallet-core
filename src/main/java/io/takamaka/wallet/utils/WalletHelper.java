/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.takamaka.wallet.InstanceWalletKeyStoreBCED25519;
import io.takamaka.wallet.InstanceWalletKeyStoreBCQTESLAPSSC1Round1;
import io.takamaka.wallet.InstanceWalletKeyStoreBCQTESLAPSSC1Round2;
import io.takamaka.wallet.InstanceWalletKeystoreInterface;
import io.takamaka.wallet.beans.EncKeyBean;
import io.takamaka.wallet.beans.EncWordsBean;
import io.takamaka.wallet.beans.KeyBean;
import io.takamaka.wallet.beans.PublicKeyBean;
import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.exceptions.InvalidCypherException;
import io.takamaka.wallet.exceptions.InvalidWalletIndexException;
import io.takamaka.wallet.exceptions.PublicKeySerializzationException;
import io.takamaka.wallet.exceptions.UnlockWalletException;
import io.takamaka.wallet.exceptions.WalletException;
import static io.takamaka.wallet.utils.FixedParameters.PUBLICKEY_EXTENSION;
import io.takamaka.wallet.utils.FixedParameters.WalletError;
import io.takamaka.wallet.utils.KeyContexts.WalletCypher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author francesco.pasetto@takamaka.io
 */
@Slf4j
public class WalletHelper {

    public static InstanceWalletKeystoreInterface readWallet(String filename, String password) throws InvalidCypherException, FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException {
        Path pt = Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), filename + DefaultInitParameters.WALLET_EXTENSION);
        return readWalletInternal(filename, pt, password);
    }

    public static InstanceWalletKeystoreInterface readWalletRecoveryGui(String filename, String password) throws InvalidCypherException, FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException {
        Path pt = Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), filename);
        return readWalletInternal(filename, pt, password);
    }

    private static InstanceWalletKeystoreInterface readWalletInternal(String filename, Path pt, String password) throws InvalidCypherException, FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException {

        KeyBean key = readKeyFile(pt, password);
        switch (key.getCypher()) {
            case Ed25519BC:
                return (new InstanceWalletKeyStoreBCED25519(filename, password));
            case BCQTESLA_PS_1:
                return (InstanceWalletKeystoreInterface) (new InstanceWalletKeyStoreBCQTESLAPSSC1Round1(filename, password));
            case BCQTESLA_PS_1_R2:
                return (InstanceWalletKeystoreInterface) (new InstanceWalletKeyStoreBCQTESLAPSSC1Round2(filename, password));
            default:
                throw new InvalidCypherException();

        }
    }

    public static String getRecoveryWords(String filename, String password) throws InvalidCypherException, FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException {
        KeyBean key = readKeyFile(Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), filename + DefaultInitParameters.WALLET_EXTENSION), password);
        return key.getWords();
    }

    public static Path writeKeyFile(Path path, String filename, KeyBean key, String password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        String json = TkmTextUtils.toJson(key);

        /* if (password.length != 16 && password.length != 24 && password.length != 32) {
            throw new IllegalArgumentException("Password wrong length for AES key");
        }*/
        try {
            SecretKey sk = new SecretKeySpec(TkmSignUtils.PWHash(password, "TakamakaWallet", 1, 256), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sk);

            byte[][] encJson = new byte[][]{cipher.getIV(), cipher.doFinal(json.getBytes(FixedParameters.CHARSET))};

            EncKeyBean ekb = new EncKeyBean(KeyContexts.WALLET_JSON_AES, encJson);

            FileHelper.writeStringToFile(path, filename, TkmTextUtils.toJson(ekb), false);
            Path walletPath = Paths.get(path.toString(), filename);
            log.info("WALLET WRITTEN IN " + Paths.get(path.toString(), filename).toString());
            return walletPath;
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException | InvalidKeySpecException ex) {
            log.error("writeKeyFile error", ex);
        }
        return null;
    }

    /**
     * wallet cypher - it returns the words list needed for wallet recovery
     * procedure
     *
     * @param filename
     * @param password
     * @return
     * @throws FileNotFoundException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws UnlockWalletException
     */
    public static KeyBean readKeyFile(Path filename, String password) throws FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException {
        String encJson;
        try {
            encJson = FileHelper.readStringFromFile(filename);
        } catch (IOException ex) {
            log.error("error reading encoded file");
            throw new UnlockWalletException("error reading encoded file", ex);
        }
        byte[][] wallet = TkmTextUtils.enckeyBeanFromJson(encJson).getWallet();
        String json;

        try {
            /*
            System.out.println("password");
            System.out.println(password);
            System.out.println("salt");
            System.out.println("TakamakaWallet");
            System.out.println("Iterazioni");
            System.out.println(1);
            System.out.println("Bit di output");
            System.out.println(256);
             */
            byte[] passwordDigest = TkmSignUtils.PWHash(password, "TakamakaWallet", 1, 256);
            /*
            System.out.println("Password Digest");
            System.out.println(Arrays.toString(passwordDigest));
             */
            SecretKey sk = new SecretKeySpec(passwordDigest, "AES");
            //System.out.println(sk.toString());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            //System.out.println("Algoritmo AES, Concatenatione CBC, Padding PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(wallet[0]));
            json = new String(cipher.doFinal(wallet[1]));
            //System.out.println("JSON:\n" + json);
            return TkmTextUtils.keyBeanFromJson(json);

        } catch (IllegalBlockSizeException | BadPaddingException | HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException | InvalidKeySpecException | NoSuchAlgorithmException ex) {
            log.error("key file read error", ex);
            throw new UnlockWalletException();
        }
    }

    public static final KeyBean readEncFile(Path filename, String password) throws FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException {
        String encJson;
        try {
            encJson = FileHelper.readStringFromFile(filename);
        } catch (IOException ex) {
            log.error("error reading encoded file");
            throw new UnlockWalletException("error reading encoded file", ex);
        }
        byte[][] wallet;//  TkmTextUtils.enckeyBeanFromJson(encJson).getWallet();
        try {
            //        System.out.println("Encoded Json");
//        System.out.println(encJson);
//EncKeyBean enckeyBeanFromJson = TkmTextUtils.enckeyBeanFromJson(encJson);
            EncWordsBean ewb = TkmTextUtils.getJacksonMapper().readValue(encJson, EncWordsBean.class);
            wallet = ewb.getEb().getWallet();

            /*
        System.out.println("Byte Array Zero");
        System.out.println(Arrays.toString(wallet[0]));
        System.out.println("Byte Array Uno");
        System.out.println(Arrays.toString(wallet[1]));
             */
            String json;

            try {
                /*
            System.out.println("password");
            System.out.println(password);
            System.out.println("salt");
            System.out.println("TakamakaWallet");
            System.out.println("Iterazioni");
            System.out.println(1);
            System.out.println("Bit di output");
            System.out.println(256);
                 */
                byte[] passwordDigest = TkmSignUtils.PWHash(password, "TakamakaWallet", 1, 256);
                /*
            System.out.println("Password Digest");
            System.out.println(Arrays.toString(passwordDigest));
                 */
                SecretKey sk = new SecretKeySpec(passwordDigest, "AES");
                //System.out.println(sk.toString());
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                //System.out.println("Algoritmo AES, Concatenatione CBC, Padding PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(wallet[0]));
                json = new String(cipher.doFinal(wallet[1]));
                //System.out.println("JSON:\n" + json);
                return TkmTextUtils.keyBeanFromJson(json);

            } catch (IllegalBlockSizeException | BadPaddingException | HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException | InvalidKeySpecException | NoSuchAlgorithmException ex) {
                log.error("decode file exception", ex);
                throw new UnlockWalletException();
            }
        } catch (JsonProcessingException ex) {
            log.error("json decode file exception", ex);
            throw new UnlockWalletException(ex);
        }
    }

    /**
     * Spawns a refurbished wallet
     *
     * @param words an ordered list of words used for wallet restore procedure
     * @param path is the path on device where the wallet will be restored and
     * saved
     * @param filename the wallet internal name
     * @param cypher the wallet cypher
     * @param newPassword the new chosen password
     * @return Path where the wallet has been created
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     */
    public static Path importKeyFromWords(List<String> words, Path path, String filename, KeyContexts.WalletCypher cypher, String newPassword) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        String seed;
        try {
            seed = SeedGenerator.generateSeedPWH(words);
            //System.out.println(seed);
            String concat = words.get(0);
            for (int i = 1; i < words.size(); i++) {
                concat += " " + words.get(i);
            }
            KeyBean kb = new KeyBean(KeyContexts.WALLET_JSON_AES, cypher, seed, concat);
            Path walltePath = writeKeyFile(path, filename, kb, newPassword);
            return walltePath;
        } catch (HashEncodeException | InvalidKeySpecException | HashAlgorithmNotFoundException | HashProviderNotFoundException | NoSuchProviderException | IllegalBlockSizeException | BadPaddingException | IOException ex) {
            log.error("import key error", ex);
        }
        return null;
    }

    public static boolean verifyWordsIntegrity(List<String> words) {
        return SeedGenerator.verifySeedWords(words);
    }

    public static void writePublicKey(String walletname, String password, int keyIndex, WalletCypher cypher) throws InvalidCypherException, InvalidWalletIndexException, PublicKeySerializzationException, UnlockWalletException, IOException {
        switch (cypher) {
            case Ed25519BC:
                InstanceWalletKeyStoreBCED25519 wallet = new InstanceWalletKeyStoreBCED25519(walletname, password);
                String pk = wallet.getPublicKeyAtIndexURL64(keyIndex);
                PublicKeyBean pkb = new PublicKeyBean(cypher, KeyContexts.PUBLICKEY_CURRENT_VERSION, pk);
                FileHelper.writeStringToFile(FileHelper.getPublicKeyDirectoryPath(), walletname + FixedParameters.PUBLICKEY_EXTENSION, TkmTextUtils.toJson(pkb), false);
                break;
            case BCQTESLA_PS_1:
                InstanceWalletKeyStoreBCQTESLAPSSC1Round1 walletQT = new InstanceWalletKeyStoreBCQTESLAPSSC1Round1(walletname, password);
                String pkQT = walletQT.getPublicKeyAtIndexURL64(keyIndex);
                PublicKeyBean pkbQT = new PublicKeyBean(cypher, KeyContexts.PUBLICKEY_CURRENT_VERSION, pkQT);
                FileHelper.writeStringToFile(FileHelper.getPublicKeyDirectoryPath(), walletname + FixedParameters.PUBLICKEY_EXTENSION, TkmTextUtils.toJson(pkbQT), false);
                break;

            case BCQTESLA_PS_1_R2:
                InstanceWalletKeyStoreBCQTESLAPSSC1Round2 walletQTr2 = new InstanceWalletKeyStoreBCQTESLAPSSC1Round2(walletname, password);
                String pkQTr2 = walletQTr2.getPublicKeyAtIndexURL64(keyIndex);
                PublicKeyBean pkbQTr2 = new PublicKeyBean(cypher, KeyContexts.PUBLICKEY_CURRENT_VERSION, pkQTr2);
                FileHelper.writeStringToFile(FileHelper.getPublicKeyDirectoryPath(), walletname + FixedParameters.PUBLICKEY_EXTENSION, TkmTextUtils.toJson(pkbQTr2), false);
                break;
            default:
                throw new InvalidCypherException();
        }

    }

    public static PublicKeyBean readPublicKey(String publicKeyName) throws WalletException {
        try {
            String json = FileHelper.readStringFromFile(Paths.get(FileHelper.getPublicKeyDirectoryPath().toString(), publicKeyName + FixedParameters.PUBLICKEY_EXTENSION));
            return TkmTextUtils.publicKeyBeanFromJson(json);
        } catch (IOException ex) {
            log.error(WalletError.PKEY_READ.name(), ex);
            throw new WalletException("error reading public key bean", ex);
        }
    }

    public static Map<String, PublicKeyBean> listPublicKeys() throws WalletException {
        Map<String, PublicKeyBean> ret = new LinkedHashMap<String, PublicKeyBean>();
        File folder = new File(FileHelper.getPublicKeyDirectoryPath().toString());
        File[] listOfFiles = folder.listFiles();
        for (File f : listOfFiles) {
            if (f.isFile()) {
                String p = f.getName().substring(0, f.getName().length() - PUBLICKEY_EXTENSION.length());
                ret.put(p, readPublicKey(p));
            }
        }
        return ret;
    }
}
