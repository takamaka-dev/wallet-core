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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.util.encoders.UrlBase64;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class InstanceWalletKeyStoreBCED25519 implements InstanceWalletKeystoreInterface {

    private Map<Integer, AsymmetricCipherKeyPair> signKeys;
    private Map<Integer, String> hexPublicKeys;
    private Map<Integer, byte[]> bytePublicKeys;
    private String seed;
    private String currentWalletName;
    private boolean isInitialized; //default to false
    private final static KeyContexts.WalletCypher walletCypher = KeyContexts.WalletCypher.Ed25519BC;
    private final Object constructorLock = new Object();
    private final Object getKeyPairAtIndexLock = new Object();
    private final Object getPublicKeyAtIndexHexLock = new Object();
    private final Object getPublicKeyAtIndexByteLock = new Object();

    /**
     * 0.10.0 — appRoot for filesystem isolation. Captured at construction
     * time; null preserves legacy resolution against
     * {@link FileHelper#getDefaultApplicationDirectoryPath()}. Threaded
     * through all initWallet variants and any future on-demand keystore I/O.
     * @since 0.10.0
     */
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
    public InstanceWalletKeyStoreBCED25519(String walletName) throws UnlockWalletException {
        this(walletName, (Path) null);
    }

    /**
     * 0.10.0 — appRoot-aware overload. {@code appRoot} non-null routes all
     * keystore I/O to that directory; null preserves legacy resolution.
     *
     * @param walletName the name of the wallet file
     * @param appRoot explicit application directory; null falls back to
     *                {@link FileHelper#getDefaultApplicationDirectoryPath()}
     * @throws UnlockWalletException
     * @since 0.10.0
     */
    public InstanceWalletKeyStoreBCED25519(String walletName, Path appRoot) throws UnlockWalletException {
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

    /**
     * Constructor for InstanceWalletKeyStoreBCED25519.
     *
     * It initializes the collections for key pairs and public keys, and calls
     * initWallet method to initialize or load an existing wallet
     *
     * @param walletName the name of the wallet file
     * @param password the password used to encrypt the keyfile
     * @throws UnlockWalletException if there is an error with unlocking the
     * wallet
     */
    public InstanceWalletKeyStoreBCED25519(String walletName, String password) throws UnlockWalletException {
        this(walletName, password, (Path) null);
    }

    /**
     * 0.10.0 — appRoot-aware overload (with password).
     * @since 0.10.0
     */
    public InstanceWalletKeyStoreBCED25519(String walletName, String password, Path appRoot) throws UnlockWalletException {
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
     * 0.10.0 — BREAKING change. Replaces the prior {@code (walletName, int nCharForSeed)}
     * ephemeral ctor (REMOVED in 0.10.0) with an epoch-aware signature.
     *
     * <p>The previous parameter name {@code nCharForSeed} was misleading: it
     * is the LENGTH of a generated random alphanumeric string, NOT a seed
     * value. Renamed to {@code seedLength} for clarity (Proposal 1).
     *
     * <p>The new {@code epoch} parameter selects the ephemeral storage
     * routing per Proposal 3 — see {@link FileHelper#getEphemeralWalletDirectoryPath(int, Path)}
     * for the strict sentinel rules:
     * <ul>
     *   <li>{@code epoch == -1}: legacy flat directory
     *       (preserves pre-0.10.0 file layout)</li>
     *   <li>{@code epoch >= 0}: per-epoch subdirectory
     *       {@code E{epoch:05d}/}</li>
     *   <li>{@code epoch < -1}: throws {@link IllegalArgumentException}</li>
     * </ul>
     *
     * <p>Migration: callers of the old {@code (walletName, int nCharForSeed)}
     * ctor must add an explicit epoch parameter (use {@code -1} for the
     * legacy flat-directory behaviour, or any non-negative epoch number for
     * the new routing).
     *
     * @param walletName the name of the wallet file
     * @param seedLength number of characters for the random ephemeral seed
     * @param epoch -1 for legacy flat dir; >= 0 for epoch-routed subdir
     * @throws UnlockWalletException
     * @throws WalletEmptySeedException
     * @throws WalletBurnedException
     * @since 0.10.0
     */
    public InstanceWalletKeyStoreBCED25519(String walletName, int seedLength, int epoch) throws UnlockWalletException, WalletEmptySeedException, WalletBurnedException {
        this(walletName, seedLength, epoch, null);
    }

    /**
     * 0.10.0 — BREAKING change with appRoot variant. See sibling 3-arg form for
     * the full migration story.
     *
     * @param walletName the name of the wallet file
     * @param seedLength number of characters for the random ephemeral seed
     * @param epoch -1 for legacy flat dir; >= 0 for epoch-routed subdir
     * @param appRoot explicit application directory; null = legacy default
     * @since 0.10.0
     */
    public InstanceWalletKeyStoreBCED25519(String walletName, int seedLength, int epoch, Path appRoot) throws UnlockWalletException, WalletEmptySeedException, WalletBurnedException {
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

    /**
     * 0.10.0 — appRoot-aware variant. Routes all keystore I/O under the
     * supplied appRoot; null preserves legacy resolution.
     * @since 0.10.0
     */
    private void initWallet(String password, Path appRoot) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException {
        // 0.10.0 — ensure full path chain exists. Legacy code only created
        // ephemeralWallets/ when walletDir was missing, never the wallets/
        // dir itself; worked in production by accident because some earlier
        // bootstrap created it. Fresh appRoot tempdirs (test fixtures, multi-
        // instance deployments) need both app dir AND wallet dir created.
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
                //System.out.println("loading " + currentWalletName + " wallet seed...");
                seed = WalletHelper.readKeyFile(currentWalletPath, password).getSeed();
                //System.out.println("seed loaded");
                //System.out.println(seed);
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
    /**
     * 0.10.0 — replaces the prior {@code initWallet(int nCharSeed)} body.
     * Renamed parameter {@code nCharSeed} → {@code seedLength} (Proposal 1)
     * and added epoch + appRoot routing per Proposal 3.
     *
     * @param seedLength number of random alphanumeric characters
     * @param epoch -1 for legacy flat dir; >= 0 for E{epoch:05d}/ subdir
     * @param appRoot explicit application directory; null = legacy default
     * @since 0.10.0
     */
    private void initWalletEphemeral(int seedLength, int epoch, Path appRoot) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException, WalletBurnedException, WalletEmptySeedException {
        // 0.10.0 — pre-create the entire path chain. fresh appRoot tempdir
        // may not have appRoot itself created (lazily-created subdir of /tmp),
        // and FileHelper.createDir requires parent to exist.
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
                Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
                if (index < 0 || index >= Integer.MAX_VALUE) {
                    throw new InvalidWalletIndexException("index outside wallet range");
                }
                keyPairGenerator.init(new Ed25519KeyGenerationParameters(new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, index + 1)));
                signKeys.put(index, keyPairGenerator.generateKeyPair());
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
                    Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) aPublic;
                    UrlBase64.encode(publicKey.getEncoded(), baos);
                    hexPublicKeys.put(index, baos.toString());
                    baos.close();
                } catch (IOException ex) {
                    log.error("Wallet can not serialize public key", ex);
                    throw new PublicKeySerializzationException(ex);
                }

            }
        }
        return hexPublicKeys.get(index);
    }

    /**
     * Retrieve the public key at a specific index in the wallet in byte format.
     *
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
                    Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) aPublic;
                    //UrlBase64.encode(publicKey.getEncoded(), baos);
                    bytePublicKeys.put(index, publicKey.getEncoded());
                    baos.close();
                } catch (IOException ex) {
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
