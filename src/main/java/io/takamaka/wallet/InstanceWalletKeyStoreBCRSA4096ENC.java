/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet;

import io.takamaka.wallet.beans.KeyBean;
import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.exceptions.InvalidWalletIndexException;
import io.takamaka.wallet.exceptions.PublicKeySerializzationException;
import io.takamaka.wallet.exceptions.UnlockWalletException;
import io.takamaka.wallet.exceptions.WalletBurnedException;
import io.takamaka.wallet.exceptions.WalletEmptySeedException;
import io.takamaka.wallet.utils.DefaultInitParameters;
import io.takamaka.wallet.utils.FileHelper;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.WalletHelper;
import io.takamaka.wallet.utils.TkmTextUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.UrlBase64;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class InstanceWalletKeyStoreBCRSA4096ENC implements InstanceWalletKeystoreInterface {

    private Map<Integer, AsymmetricCipherKeyPair> signKeys;
    private Map<Integer, String> hexPublicKeys;
    private Map<Integer, byte[]> bytePublicKeys;
    private String seed;
    private String currentWalletName;
    private boolean isInitialized; //default to false
    private final static KeyContexts.WalletCypher walletCypher = KeyContexts.WalletCypher.RSA_4096_ECB_OAEP;
    private static final String ALGORITHM = "RSA";
    private final Object constructorLock = new Object();
    private final Object getKeyPairAtIndexLock = new Object();
    private final Object getPublicKeyAtIndexHexLock = new Object();
    private final Object getPublicKeyAtIndexByteLock = new Object();

    /** 0.10.0 — appRoot for filesystem isolation. @since 0.10.0 */
    private Path appRoot;

    /**
     * Method to get the cypher used in the current wallet.
     *
     * @return the cypher used in the wallet
     */
    @Override
    public KeyContexts.WalletCypher getWalletCypher() {
        return walletCypher;
    }

    /**
     * Method to get the identifier of the current wallet on system.
     *
     * @return the name of the wallet concatenated with the algorithm used.
     */
    @Override

    public String getCurrentWalletID() {
        return currentWalletName + walletCypher.name();
    }

    /**
     * Constructor for InstanceWalletKeyStoreBCED25519.
     *
     * It initializes the collections for key pairs and public keys, and calls
     * initWallet method to initialize or load an existing wallet using a
     * default hardcoded password
     *
     * @param walletName the name of the wallet file
     * @throws UnlockWalletException if there is an error with unlocking the
     * wallet
     */
    public InstanceWalletKeyStoreBCRSA4096ENC(String walletName) throws UnlockWalletException {
        this(walletName, (Path) null);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public InstanceWalletKeyStoreBCRSA4096ENC(String walletName, Path appRoot) throws UnlockWalletException {
        synchronized (constructorLock) {
            if (!isInitialized) {
                try {
                    currentWalletName = walletName + DefaultInitParameters.WALLET_EXTENSION;
                    this.appRoot = appRoot;
                    signKeys = Collections.synchronizedMap(new HashMap<Integer, AsymmetricCipherKeyPair>());
                    hexPublicKeys = Collections.synchronizedMap(new HashMap<Integer, String>());
                    bytePublicKeys = Collections.synchronizedMap(new HashMap<Integer, byte[]>());
                    initWallet("Password", appRoot);
                } catch (IOException | NoSuchAlgorithmException | HashEncodeException | InvalidKeySpecException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
                    log.error("instance error name", ex);
                    throw new UnlockWalletException(ex);
                }
                isInitialized = true;
            }
        }

    }

    public InstanceWalletKeyStoreBCRSA4096ENC(String walletName, String password) throws UnlockWalletException {
        this(walletName, password, (Path) null);
    }

    /** 0.10.0 — appRoot-aware overload (with password). @since 0.10.0 */
    public InstanceWalletKeyStoreBCRSA4096ENC(String walletName, String password, Path appRoot) throws UnlockWalletException {
        synchronized (constructorLock) {
            if (!isInitialized) {
                try {
                    currentWalletName = walletName + DefaultInitParameters.WALLET_EXTENSION;
                    this.appRoot = appRoot;
                    signKeys = Collections.synchronizedMap(new HashMap<Integer, AsymmetricCipherKeyPair>());
                    hexPublicKeys = Collections.synchronizedMap(new HashMap<Integer, String>());
                    bytePublicKeys = Collections.synchronizedMap(new HashMap<Integer, byte[]>());
                    initWallet(password, appRoot);
                } catch (IOException | NoSuchAlgorithmException | HashEncodeException | InvalidKeySpecException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
                    log.error("instance error name password", ex);
                    throw new UnlockWalletException("instance error name password", ex);
                }
                isInitialized = true;
            }
        }

    }

    /**
     * 0.10.0 — BREAKING change. Replaces (walletName, int nCharForSeed)
     * ephemeral ctor with epoch-aware signature. See ED25519 sibling for
     * full migration story.
     * @since 0.10.0
     */
    public InstanceWalletKeyStoreBCRSA4096ENC(String walletName, int seedLength, int epoch) throws UnlockWalletException, WalletEmptySeedException, WalletBurnedException {
        this(walletName, seedLength, epoch, null);
    }

    /** 0.10.0 — BREAKING ephemeral ctor with appRoot. @since 0.10.0 */
    public InstanceWalletKeyStoreBCRSA4096ENC(String walletName, int seedLength, int epoch, Path appRoot) throws UnlockWalletException, WalletEmptySeedException, WalletBurnedException {
        synchronized (constructorLock) {
            if (!isInitialized) {
                try {
                    currentWalletName = walletName + DefaultInitParameters.WALLET_EXTENSION;
                    this.appRoot = appRoot;
                    signKeys = Collections.synchronizedMap(new HashMap<Integer, AsymmetricCipherKeyPair>());
                    hexPublicKeys = Collections.synchronizedMap(new HashMap<Integer, String>());
                    bytePublicKeys = Collections.synchronizedMap(new HashMap<Integer, byte[]>());
                    initWalletEphemeral(seedLength, epoch, appRoot);
                } catch (IOException | NoSuchAlgorithmException | HashEncodeException | InvalidKeySpecException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
                    log.error("instance error seed", ex);
                    throw new UnlockWalletException("instance error seed", ex);
                }
                isInitialized = true;
            }
        }

    }

    /**
     * Initializes a new wallet or loads an existing one
     *
     * If the wallet directory does not exist, it will be created If the wallet
     * file does not exist, a new seed and words will be generated using the
     * SeedGenerator and the seed will be written to the wallet file using the
     * WalletHelper If the wallet file exists, the seed is read from the file
     * using the WalletHelper
     *
     * @param password The password to be used for encrypting the keyfile
     * @throws IOException if there is an error reading or writing to the file
     * @throws NoSuchAlgorithmException if the algorithm specified is not
     * available
     * @throws HashEncodeException if an error occurs while encoding
     * @throws InvalidKeySpecException if an error occurs while generating the
     * key
     * @throws HashAlgorithmNotFoundException if an error occurs while
     * generating the key
     * @throws HashProviderNotFoundException if an error occurs while generating
     * the key
     * @throws UnlockWalletException if there is an error with unlocking the
     * wallet
     * @throws NoSuchProviderException if the provider specified is not
     * available
     */
    private void initWallet(String password) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException {
        initWallet(password, null);
    }

    /** 0.10.0 — appRoot-aware variant. @since 0.10.0 */
    private void initWallet(String password, Path appRoot) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException {
        // 0.10.0 — ensure full path chain exists for fresh appRoot tempdirs.
        Path appDir = FileHelper.getDefaultApplicationDirectoryPath(appRoot);
        if (!appDir.toFile().isDirectory()) {
            FileHelper.createDir(appDir);
        }
        if (!FileHelper.walletDirExists(appRoot)) {
            FileHelper.createDir(FileHelper.getDefaultWalletDirectoryPath(appRoot));
        }
        if (!FileHelper.fileExists(Paths.get(FileHelper.getDefaultWalletDirectoryPath(appRoot).toString(), currentWalletName))) {
            List<String> words = SeedGenerator.generateWords();
            seed = SeedGenerator.generateSeedPWH(words);

            String concat = words.get(0);
            for (int i = 1; i < words.size(); i++) {
                concat += " " + words.get(i);
            }

            KeyBean kb = new KeyBean("POWSEED", KeyContexts.WalletCypher.Ed25519BC, seed, concat);
            try {
                WalletHelper.writeKeyFile(FileHelper.getDefaultWalletDirectoryPath(appRoot), currentWalletName, kb, password);

            } catch (NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | io.takamaka.wallet.exceptions.KeystoreFileExistsException ex) {
                // 0.10.0 — KeystoreFileExistsException added; pre-check shields.
                log.error("instance error password", ex);
                throw new UnlockWalletException("instance error password", ex);
            }
        }
        Path currentWalletPath = Paths.get(FileHelper.getDefaultWalletDirectoryPath(appRoot).toString(), currentWalletName);

        if (FileHelper.fileExists(currentWalletPath)) {
            try {
                seed = WalletHelper.readKeyFile(currentWalletPath, password).getSeed();
            } catch (InvalidAlgorithmParameterException | FileNotFoundException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException ex) {
                log.error("initWallet unreadable file?", ex);
                throw new IOException("initWallet unreadable file?", ex);
            }
        }
    }

    /**
     * Initializes a new wallet or loads an existing one
     *
     * If the wallet directory does not exist, it will be created If the wallet
     * file does not exist, a new seed of alphabetic characters will be
     * generated, and the seed will be written to the wallet file If the wallet
     * file exists, the seed is read from the file If the seed is "burned",
     * WalletBurnedException is thrown If the seed is empty,
     * WalletEmptySeedException is thrown
     *
     * @param nCharSeed number of characters for the seed
     * @throws IOException if there is an error reading or writing to the file
     * @throws NoSuchAlgorithmException if the algorithm specified is not
     * available
     * @throws HashEncodeException if an error occurs while encoding
     * @throws InvalidKeySpecException if an error occurs while generating the
     * key
     * @throws HashAlgorithmNotFoundException if an error occurs while
     * generating the key
     * @throws HashProviderNotFoundException if an error occurs while generating
     * the key
     * @throws WalletBurnedException if the seed is "burned"
     * @throws WalletEmptySeed
     */
    /** 0.10.0 — replaces initWallet(int) with epoch- + appRoot-aware ephemeral routing. @since 0.10.0 */
    private void initWalletEphemeral(int seedLength, int epoch, Path appRoot) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException, WalletBurnedException, WalletEmptySeedException {
        // 0.10.0 — pre-create entire path chain.
        Path appDir = FileHelper.getDefaultApplicationDirectoryPath(appRoot);
        if (!appDir.toFile().isDirectory()) {
            FileHelper.createDir(appDir);
        }
        Path flatEphDir = FileHelper.getEphemeralWalletDirectoryPath(appRoot);
        if (!flatEphDir.toFile().isDirectory()) {
            FileHelper.createDir(flatEphDir);
        }
        Path ephDir = FileHelper.getEphemeralWalletDirectoryPath(epoch, appRoot);
        if (!ephDir.toFile().isDirectory()) {
            FileHelper.createDir(ephDir);
        }
        Path currentWalletPath = Paths.get(ephDir.toString(), currentWalletName);
        if (!FileHelper.fileExists(currentWalletPath)) {
            RandomStringGenerator generator = new RandomStringGenerator.Builder()
                    .withinRange('0', 'z')
                    .filteredBy(Character::isLetterOrDigit)
                    .get();
            seed = generator.generate(seedLength);
            FileHelper.writeStringToFile(ephDir, currentWalletName, seed, false);
        }
        if (FileHelper.fileExists(currentWalletPath)) {
            try {
                seed = FileHelper.readStringFromFile(currentWalletPath);
                if ("burned".equals(seed)) {
                    throw new WalletBurnedException("WALLET IS BURNED");
                }
                if (TkmTextUtils.isNullOrBlank(seed)) {
                    throw new WalletEmptySeedException("WALLET SEED IS EMPTY");
                }
            } catch (FileNotFoundException ex) {
                log.error("instance error nseed", ex);
                throw new IOException("instance error nseed", ex);
            }
        }
    }

    /**
     * Retrieve the keypair at a specific index in the wallet.
     *
     * If the keypair is not yet stored in the signKeys collection, it will be
     * generated using Ed25519KeyPairGenerator, initialized with a seed and an
     * index, then added to the signKeys collection
     *
     * @param index index of the keypair to be retrieved
     * @return the keypair at the given index
     * @throws InvalidWalletIndexException if the index is not valid
     */
    @Override
    public AsymmetricCipherKeyPair getKeyPairAtIndex(int index) throws InvalidWalletIndexException {
        if (!signKeys.containsKey(index)) {
            synchronized (getKeyPairAtIndexLock) {
                //call key creation
                RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
                if (index < 0 || index >= Integer.MAX_VALUE) {
                    throw new InvalidWalletIndexException("index outside wallet range");
                }
                /**
                 * https://docs.oracle.com/javase/8/docs/api/java/security/spec/RSAKeyGenParameterSpec.html
                 */

                /**
                 * Always instantiate a new SeededRandom generator to get the
                 * deterministically correct seed.
                 *
                 */
                rsaKeyPairGenerator.init(new RSAKeyGenerationParameters(RSAKeyGenParameterSpec.F4, new SeededRandom(seed, KeyContexts.RSA_PK_ENCRYPTION, index + 1), 4096, 1));
                //keyPairGenerator.init(new Ed25519KeyGenerationParameters(new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, index + 1)));
                signKeys.put(index, rsaKeyPairGenerator.generateKeyPair());
            }
        }
        return signKeys.get(index);
    }

    /**
     * Retrieve the public key at a specific index in the wallet in URL-safe
     * Base64 format.
     *
     * If the key is not yet stored in the hexPublicKeys collection, it will be
     * retrieved from the keypair collection and encoded in URL-safe Base64
     * format before being added to the hexPublicKeys collection
     * https://the-pi-guy.com/blog/restassured_api_testing_with_data_encryption_and_decryption/
     *
     * @param index index of the key to be retrieved
     * @return the public key at the given index in URL-safe Base64 format
     * @throws InvalidWalletIndexException if the index is not valid
     * @throws PublicKeySerializzationException if the key cannot be serialized
     */
    @Override
    public String getPublicKeyAtIndexURL64(int index) throws InvalidWalletIndexException, PublicKeySerializzationException {
        if (!hexPublicKeys.containsKey(index)) {
            synchronized (getPublicKeyAtIndexHexLock) {
                try {
                    AsymmetricCipherKeyPair keyPairAtIndex = getKeyPairAtIndex(index);
                    //UrlBase64 b64e = new UrlBase64();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    AsymmetricKeyParameter aPublic = keyPairAtIndex.getPublic();
                    RSAKeyParameters rsaPublic = (RSAKeyParameters) aPublic;
                    RSAPublicKeySpec spec = new RSAPublicKeySpec(rsaPublic.getModulus(), rsaPublic.getExponent());
                    KeyFactory factory = KeyFactory.getInstance("RSA");
                    PublicKey pub = factory.generatePublic(spec);
                    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pub.getEncoded());
                    
//                    KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
//                    return keyFactory.generatePublic(x509KeySpec);
                    UrlBase64.encode(x509KeySpec.getEncoded(), baos);
                    hexPublicKeys.put(index, baos.toString());
//                    baos.close();
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
                    log.error("Wallet can not serialize public key", ex);
                    throw new PublicKeySerializzationException(ex);
                }

            }
        }
        return hexPublicKeys.get(index);
    }

    /**
     * Retrieve the public key at a specific index in the wallet in byte format.
     * https://stackoverflow.com/questions/42861943/publickey-from-bouncycastle-rsakeyparameters
     * If the key is not yet stored in the bytePublicKeys collection, it will be
     * retrieved from the keypair collection and encoded in byte format before
     * being added to the bytePublicKeys collection
     *
     * @param index index of the key to be retrieved
     * @return the public key at the given index in byte format
     * @throws InvalidWalletIndexException if the index is not valid
     * @throws PublicKeySerializzationException if the key cannot be serialized
     */
    @Override
    public byte[] getPublicKeyAtIndexByte(int index) throws InvalidWalletIndexException, PublicKeySerializzationException {
        if (!bytePublicKeys.containsKey(index)) {
            synchronized (getPublicKeyAtIndexByteLock) {
                try {
                    AsymmetricCipherKeyPair keyPairAtIndex = getKeyPairAtIndex(index);
                    //UrlBase64 b64e = new UrlBase64();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    AsymmetricKeyParameter aPublic = keyPairAtIndex.getPublic();
                    RSAKeyParameters rsaPublic = (RSAKeyParameters) aPublic;
                    RSAPublicKeySpec spec = new RSAPublicKeySpec(rsaPublic.getModulus(), rsaPublic.getExponent());
                    KeyFactory factory = KeyFactory.getInstance("RSA");
                    PublicKey pub = factory.generatePublic(spec);
//                    new RSAKeyParameters(isInitialized, BigInteger.ONE, BigInteger.ONE)
                    //Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) aPublic;
                    bytePublicKeys.put(index, pub.getEncoded());
                    baos.close();
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
                    log.error("Wallet can not serialize public key", ex);
                    throw new PublicKeySerializzationException(ex);
                }
            }
        }
        return bytePublicKeys.get(index);
    }

    /**
     *
     * compare two wallet using their file system name
     */
    @Override
    public int compareTo(InstanceWalletKeystoreInterface t) {
        return getCurrentWalletID().compareTo(t.getCurrentWalletID());
    }

}
