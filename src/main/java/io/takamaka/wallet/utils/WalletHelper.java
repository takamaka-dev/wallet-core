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
import io.takamaka.wallet.exceptions.KeystoreFileExistsException;
import io.takamaka.wallet.exceptions.PublicKeyFileExistsException;
import io.takamaka.wallet.exceptions.PublicKeySerializzationException;
import io.takamaka.wallet.exceptions.UnlockWalletException;
import io.takamaka.wallet.exceptions.WalletException;
import static io.takamaka.wallet.utils.FixedParameters.PUBLICKEY_EXTENSION;
import io.takamaka.wallet.utils.FixedParameters.WalletError;
import io.takamaka.wallet.utils.KeyContexts.WalletCypher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author francesco.pasetto@takamaka.io
 */
@Slf4j
public class WalletHelper {

    public static InstanceWalletKeystoreInterface readWallet(String filename, String password) throws InvalidCypherException, FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException, WalletException {
        Path pt = Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), filename + DefaultInitParameters.WALLET_EXTENSION);
        return readWalletInternal(filename, pt, password);
    }

    public static InstanceWalletKeystoreInterface readWalletRecoveryGui(String filename, String password) throws InvalidCypherException, FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException, WalletException {
        Path pt = Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), filename);
        return readWalletInternal(filename, pt, password);
    }

    private static InstanceWalletKeystoreInterface readWalletInternal(String filename, Path pt, String password) throws InvalidCypherException, FileNotFoundException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnlockWalletException, WalletException {

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

    public static Path writeKeyFile(Path path, String filename, KeyBean key, String password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, KeystoreFileExistsException {
        // 0.10.0 H1 fix — fail fast at the top, before any crypto work, if the
        // target keystore already exists. Eliminates the silent-corruption
        // footgun where prior versions called writeStringToFile(..., overwrite=false),
        // discarded the false-on-conflict return, and reported success
        // anyway.  See exceptions/KeystoreFileExistsException.java javadoc and
        // nodeflux/docs/TASK-wallet-core-app-root-overload.md §11 for context.
        Path walletPath = Paths.get(path.toString(), filename);
        if (FileHelper.fileExists(walletPath)) {
            throw new KeystoreFileExistsException(
                    "Refusing to overwrite existing keystore at " + walletPath
                    + " — delete the file explicitly if replacement is intended.");
        }

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

            // overwrite=false retained for defence-in-depth — unreachable now
            // that writeKeyFile pre-checks at the top, but harmless and
            // protects against future removal of the top-level guard.
            FileHelper.writeStringToFile(path, filename, TkmTextUtils.toJson(ekb), false);
            log.info("WALLET WRITTEN IN " + walletPath.toString());
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
        EncKeyBean ekb = TkmTextUtils.enckeyBeanFromJson(encJson);
        if (ekb == null) {
            // Corrupt / unparseable container → safe error (no crypto attempted).
            throw new UnlockWalletException("unreadable keystore container");
        }
        // DR-009 — dispatch on the version discriminator. WALLET_JSON_AES_V2
        // ⇒ the header-driven AES-GCM reader; anything else (WALLET_JSON_AES or
        // absent) ⇒ the FROZEN legacy v1 reader below, byte-for-byte unchanged
        // (algorithm value otherwise ignored, exactly as pre-DR-009).
        if (KeyContexts.WALLET_JSON_AES_V2.equals(ekb.getAlgorithm())) {
            return readKeyFileV2(ekb, password);
        }
        byte[][] wallet = ekb.getWallet();
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

    // =========================================================================
    // DR-009 — v2 keystore (AES-GCM, random salt, authenticated PBKDF2 iters).
    // v1 read above is frozen; v2 read below is header-driven; v2 WRITE is a
    // capability (writeKeyFileV2), NOT the default — the default writeKeyFile
    // keeps producing v1 until a release decision flips it.
    // =========================================================================

    /**
     * DR-009 — header-driven v2 reader. Reads the random salt, iteration count
     * and IV from the (authenticated) cleartext header, enforces the minimum
     * iteration floor, derives the key with PBKDF2 over the <b>raw</b> salt
     * bytes, and AES-GCM-decrypts with the header bound as AAD. Any tampering of
     * the header (salt/iterations/iv/algorithm/kdf), a wrong password, or a
     * corrupt tag fails the GCM authentication and is rejected as
     * {@link UnlockWalletException} with no partial state.
     */
    private static KeyBean readKeyFileV2(EncKeyBean ekb, String password) throws UnlockWalletException {
        String kdf = ekb.getKdf();
        byte[] salt = ekb.getSalt();
        int iterations = ekb.getIterations();
        byte[][] wallet = ekb.getWallet();
        if (kdf == null || salt == null || wallet == null || wallet.length < 2
                || wallet[0] == null || wallet[1] == null) {
            throw new UnlockWalletException("malformed v2 keystore header");
        }
        // Minimum-security floor — reject below-floor (e.g. maliciously weakened) headers.
        if (iterations < KeyContexts.WALLET_V2_MIN_ITERATIONS) {
            throw new UnlockWalletException("v2 keystore iterations below security floor: " + iterations);
        }
        if (!KeyContexts.WALLET_V2_KDF.equals(kdf)) {
            throw new UnlockWalletException("unsupported v2 kdf: " + kdf);
        }
        byte[] iv = wallet[0];
        byte[] cipherText = wallet[1];
        try {
            SecretKey sk = new SecretKeySpec(deriveV2Key(password, salt, iterations), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sk, new GCMParameterSpec(KeyContexts.WALLET_V2_GCM_TAG_BITS, iv));
            cipher.updateAAD(buildV2Aad(ekb.getAlgorithm(), kdf, iterations, salt, iv));
            String json = new String(cipher.doFinal(cipherText), FixedParameters.CHARSET);
            return TkmTextUtils.keyBeanFromJson(json);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                | BadPaddingException ex) {
            // BadPaddingException covers AEADBadTagException (auth failure → reject).
            log.error("v2 key file read error", ex);
            throw new UnlockWalletException("v2 keystore unlock failed", ex);
        }
    }

    /**
     * DR-009 — v2 write capability with the default iteration count
     * ({@link KeyContexts#WALLET_V2_DEFAULT_ITERATIONS}). Not invoked by the
     * default write path; enabling v2 as the production default is a separate
     * release decision (read-everywhere-before-write-anywhere).
     */
    public static Path writeKeyFileV2(Path path, String filename, KeyBean key, String password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, KeystoreFileExistsException {
        return writeKeyFileV2(path, filename, key, password, KeyContexts.WALLET_V2_DEFAULT_ITERATIONS);
    }

    /**
     * DR-009 — v2 write capability with an explicit iteration count (≥ floor).
     * Generates a fresh random salt and GCM IV per file.
     */
    public static Path writeKeyFileV2(Path path, String filename, KeyBean key, String password, int iterations) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, KeystoreFileExistsException {
        Path walletPath = Paths.get(path.toString(), filename);
        if (FileHelper.fileExists(walletPath)) {
            throw new KeystoreFileExistsException(
                    "Refusing to overwrite existing keystore at " + walletPath
                    + " — delete the file explicitly if replacement is intended.");
        }
        if (iterations < KeyContexts.WALLET_V2_MIN_ITERATIONS) {
            throw new IllegalArgumentException("v2 iterations below security floor: " + iterations);
        }
        SecureRandom rng = new SecureRandom();
        byte[] salt = new byte[KeyContexts.WALLET_V2_SALT_BYTES];
        rng.nextBytes(salt);
        byte[] iv = new byte[KeyContexts.WALLET_V2_GCM_IV_BYTES];
        rng.nextBytes(iv);
        try {
            EncKeyBean ekb = encryptV2(key, password, iterations, salt, iv);
            FileHelper.writeStringToFile(path, filename, TkmTextUtils.toJson(ekb), false);
            log.info("WALLET (v2) WRITTEN IN " + walletPath.toString());
            return walletPath;
        } catch (InvalidKeySpecException | InvalidAlgorithmParameterException ex) {
            log.error("writeKeyFileV2 error", ex);
        }
        return null;
    }

    /**
     * DR-009 — deterministic v2 encryption core. Production callers
     * ({@link #writeKeyFileV2}) pass a secure-random {@code salt} and {@code iv};
     * the explicit-parameter form exists so cross-platform parity vectors can be
     * generated deterministically (fixed salt + IV ⇒ fixed ciphertext + tag).
     * <b>Never</b> reuse a salt/IV pair in production.
     */
    static EncKeyBean encryptV2(KeyBean key, String password, int iterations, byte[] salt, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        String json = TkmTextUtils.toJson(key);
        SecretKey sk = new SecretKeySpec(deriveV2Key(password, salt, iterations), "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, sk, new GCMParameterSpec(KeyContexts.WALLET_V2_GCM_TAG_BITS, iv));
        cipher.updateAAD(buildV2Aad(KeyContexts.WALLET_JSON_AES_V2, KeyContexts.WALLET_V2_KDF, iterations, salt, iv));
        byte[] cipherText = cipher.doFinal(json.getBytes(FixedParameters.CHARSET));
        // wallet = [iv, ciphertext||tag] — GCM appends the 128-bit tag to the ciphertext.
        return new EncKeyBean(KeyContexts.WALLET_JSON_AES_V2, new byte[][]{iv, cipherText},
                KeyContexts.WALLET_V2_KDF, salt, iterations);
    }

    /** DR-009 — PBKDF2-HMAC-SHA512 over the RAW salt bytes (cross-platform contract). */
    private static byte[] deriveV2Key(String password, byte[] salt, int iterations)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KeyContexts.WALLET_V2_KEY_BITS);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(KeyContexts.WALLET_V2_KDF);
            return skf.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * DR-009 — canonical AAD binding the cleartext v2 header so it cannot be
     * tampered or downgraded (GCM authenticates AAD + ciphertext together).
     * Layout (must be byte-identical Java↔Flutter):
     * {@code UTF8(algorithm) | 0x1F | UTF8(kdf) | 0x1F | BE4(iterations) | salt | iv}.
     */
    static byte[] buildV2Aad(String algorithm, String kdf, int iterations, byte[] salt, byte[] iv) {
        byte[] algB = algorithm.getBytes(FixedParameters.CHARSET);
        byte[] kdfB = kdf.getBytes(FixedParameters.CHARSET);
        ByteBuffer buf = ByteBuffer.allocate(algB.length + 1 + kdfB.length + 1 + 4 + salt.length + iv.length);
        buf.put(algB).put((byte) 0x1F).put(kdfB).put((byte) 0x1F).putInt(iterations).put(salt).put(iv);
        return buf.array();
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
     * @throws io.takamaka.wallet.exceptions.WalletException
     */
    public static Path importKeyFromWords(List<String> words, Path path, String filename, KeyContexts.WalletCypher cypher, String newPassword) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, WalletException {
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
            throw new WalletException("import key error", ex);
        }
    }

    public static boolean verifyWordsIntegrity(List<String> words) {
        return SeedGenerator.verifySeedWords(words);
    }

    public static void writePublicKey(String walletname, String password, int keyIndex, WalletCypher cypher) throws InvalidCypherException, InvalidWalletIndexException, PublicKeySerializzationException, UnlockWalletException, IOException, WalletException, PublicKeyFileExistsException {
        // 0.10.0 H2 fix — fail fast if the public-key export file already
        // exists. Three nearly-identical writeStringToFile(..., false) call
        // sites used to silently no-op on conflict; promoted to a hard
        // failure so callers can detect and act. See exceptions/
        // PublicKeyFileExistsException javadoc and TASK §12.1.
        String pkFileName = walletname + FixedParameters.PUBLICKEY_EXTENSION;
        Path pkPath = Paths.get(FileHelper.getPublicKeyDirectoryPath().toString(), pkFileName);
        if (FileHelper.fileExists(pkPath)) {
            throw new PublicKeyFileExistsException(
                    "Refusing to overwrite existing public-key export at " + pkPath
                    + " — delete the file explicitly if replacement is intended.");
        }

        switch (cypher) {
            case Ed25519BC:
                InstanceWalletKeyStoreBCED25519 wallet = new InstanceWalletKeyStoreBCED25519(walletname, password);
                String pk = wallet.getPublicKeyAtIndexURL64(keyIndex);
                PublicKeyBean pkb = new PublicKeyBean(cypher, KeyContexts.PUBLICKEY_CURRENT_VERSION, pk);
                FileHelper.writeStringToFile(FileHelper.getPublicKeyDirectoryPath(), pkFileName, TkmTextUtils.toJson(pkb), false);
                break;
            case BCQTESLA_PS_1:
                InstanceWalletKeyStoreBCQTESLAPSSC1Round1 walletQT = new InstanceWalletKeyStoreBCQTESLAPSSC1Round1(walletname, password);
                String pkQT = walletQT.getPublicKeyAtIndexURL64(keyIndex);
                PublicKeyBean pkbQT = new PublicKeyBean(cypher, KeyContexts.PUBLICKEY_CURRENT_VERSION, pkQT);
                FileHelper.writeStringToFile(FileHelper.getPublicKeyDirectoryPath(), pkFileName, TkmTextUtils.toJson(pkbQT), false);
                break;

            case BCQTESLA_PS_1_R2:
                InstanceWalletKeyStoreBCQTESLAPSSC1Round2 walletQTr2 = new InstanceWalletKeyStoreBCQTESLAPSSC1Round2(walletname, password);
                String pkQTr2 = walletQTr2.getPublicKeyAtIndexURL64(keyIndex);
                PublicKeyBean pkbQTr2 = new PublicKeyBean(cypher, KeyContexts.PUBLICKEY_CURRENT_VERSION, pkQTr2);
                FileHelper.writeStringToFile(FileHelper.getPublicKeyDirectoryPath(), pkFileName, TkmTextUtils.toJson(pkbQTr2), false);
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
