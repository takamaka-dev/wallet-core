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
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.util.encoders.UrlBase64;

/**
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
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

    @Override
    public KeyContexts.WalletCypher getWalletCypher() {
        return walletCypher;
    }

    @Override
    public String getCurrentWalletID() {
        return currentWalletName + walletCypher.name();
    }

    /**
     * Initialize the wallet by creating both the list of words and the seed.
     *
     * @param walletName
     * @throws UnlockWalletException
     */
    public InstanceWalletKeyStoreBCED25519(String walletName) throws UnlockWalletException {
        synchronized (constructorLock) {
            if (!isInitialized) {
                try {
                    currentWalletName = walletName + DefaultInitParameters.WALLET_EXTENSION;
                    signKeys = Collections.synchronizedMap(new HashMap<Integer, AsymmetricCipherKeyPair>());
                    hexPublicKeys = Collections.synchronizedMap(new HashMap<Integer, String>());
                    bytePublicKeys = Collections.synchronizedMap(new HashMap<Integer, byte[]>());
                    initWallet("Password");
                } catch (IOException | NoSuchAlgorithmException | HashEncodeException | InvalidKeySpecException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
                    log.error("instance error name", ex);
                    throw new UnlockWalletException("Error initializing wallet", ex);
                }
                isInitialized = true;
            }
        }

    }

    /**
     * Initialize the wallet by creating both the list of words and the seed.
     *
     * @param walletName
     * @param password
     * @throws UnlockWalletException
     */
    public InstanceWalletKeyStoreBCED25519(String walletName, String password) throws UnlockWalletException {
        synchronized (constructorLock) {
            if (!isInitialized) {
                try {
                    currentWalletName = walletName + DefaultInitParameters.WALLET_EXTENSION;
                    signKeys = Collections.synchronizedMap(new HashMap<Integer, AsymmetricCipherKeyPair>());
                    hexPublicKeys = Collections.synchronizedMap(new HashMap<Integer, String>());
                    bytePublicKeys = Collections.synchronizedMap(new HashMap<Integer, byte[]>());
                    initWallet(password);
                } catch (IOException | NoSuchAlgorithmException | HashEncodeException | InvalidKeySpecException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
                    log.error("instance error name password", ex);
                    throw new UnlockWalletException("Error opening wallet, wrong password", ex);
                }
                isInitialized = true;
            }
        }

    }

    /**
     * Initialize the wallet by creating both the list of words and the seed.
     *
     * @param walletName
     * @param nCharForSeed
     * @throws UnlockWalletException
     * @throws WalletEmptySeedException
     * @throws WalletBurnedException
     */
    public InstanceWalletKeyStoreBCED25519(String walletName, int nCharForSeed) throws UnlockWalletException, WalletEmptySeedException, WalletBurnedException {
        synchronized (constructorLock) {
            if (!isInitialized) {
                try {
                    currentWalletName = walletName + DefaultInitParameters.WALLET_EXTENSION;
                    signKeys = Collections.synchronizedMap(new HashMap<Integer, AsymmetricCipherKeyPair>());
                    hexPublicKeys = Collections.synchronizedMap(new HashMap<Integer, String>());
                    bytePublicKeys = Collections.synchronizedMap(new HashMap<Integer, byte[]>());
                    initWallet(nCharForSeed);
                } catch (IOException | NoSuchAlgorithmException | HashEncodeException | InvalidKeySpecException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
                    log.error("instance error seed", ex);
                    throw new UnlockWalletException("Error intializing wallet", ex);
                }
                isInitialized = true;
            }
        }

    }

    private void initWallet(String password) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException {
        if (!FileHelper.walletDirExists()) {
            //FileHelper.createDir(FileHelper.getDefaultWalletDirectoryPath());
            try {
                FileHelper.createDir(FileHelper.getEphemeralWalletDirectoryPath());
            } catch (IOException ex) {
                log.error("Error creating directory");
                throw new IOException("Error creating directory", ex);
            }
        }
        if (!FileHelper.fileExists(Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), currentWalletName))) {
            List<String> words = SeedGenerator.generateWords();
            seed = SeedGenerator.generateSeedPWH(words);

            String concat = words.get(0);
            for (int i = 1; i < words.size(); i++) {
                concat += " " + words.get(i);
            }

            KeyBean kb = new KeyBean("POWSEED", KeyContexts.WalletCypher.Ed25519BC, seed, concat);
            try {
                WalletHelper.writeKeyFile(FileHelper.getDefaultWalletDirectoryPath(), currentWalletName, kb, password);

            } catch (NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                log.error("instance error password", ex);
                throw new InvalidKeySpecException("Error initializing wallet.", ex);
            }
        }
        Path currentWalletPath = Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), currentWalletName);

        if (FileHelper.fileExists(currentWalletPath)) {
            try {
                //System.out.println("loading " + currentWalletName + " wallet seed...");
                seed = WalletHelper.readKeyFile(currentWalletPath, password).getSeed();
                //System.out.println("seed loaded");
                //System.out.println(seed);
            } catch (InvalidAlgorithmParameterException | FileNotFoundException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException ex) {
                log.error("initWallet unreadable file?", ex);
                throw new InvalidKeySpecException("Error reading wallet file.", ex);
            }
        }
    }

    private void initWallet(int nCharSeed) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException, WalletBurnedException, WalletEmptySeedException {
        if (!FileHelper.walletDirExists()) {
            FileHelper.createDir(FileHelper.getEphemeralWalletDirectoryPath());
        }
        if (!FileHelper.fileExists(Paths.get(FileHelper.getEphemeralWalletDirectoryPath().toString(), currentWalletName))) {
            seed = RandomStringUtils.randomAlphabetic(nCharSeed);
            FileHelper.writeStringToFile(FileHelper.getEphemeralWalletDirectoryPath(), currentWalletName, seed, false);
        }
        Path currentWalletPath = Paths.get(FileHelper.getEphemeralWalletDirectoryPath().toString(), currentWalletName);

        if (FileHelper.fileExists(currentWalletPath)) {
            try {
                seed = FileHelper.readStringFromFile(Paths.get(FileHelper.getEphemeralWalletDirectoryPath().toString(), currentWalletName));
                if ("burned".equals(seed)) {
                    throw new WalletBurnedException("WALLET IS BURNED");
                }
                if (TkmTextUtils.isNullOrBlank(seed)) {
                    throw new WalletEmptySeedException("WALLET SEED IS EMPTY");
                }
            } catch (FileNotFoundException ex) {
                log.error("instance error nseed", ex);
                throw new FileNotFoundException("instance error nseed");
            }
        }
    }

    @Override
    public AsymmetricCipherKeyPair getKeyPairAtIndex(int i) throws InvalidWalletIndexException {
        if (!signKeys.containsKey(i)) {
            synchronized (getKeyPairAtIndexLock) {
                //call key creation
                Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
                if (i < 0 || i >= Integer.MAX_VALUE) {
                    throw new InvalidWalletIndexException("index outside wallet range");
                }
                keyPairGenerator.init(new Ed25519KeyGenerationParameters(new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, i + 1)));
                signKeys.put(i, keyPairGenerator.generateKeyPair());
            }
        }
        return signKeys.get(i);
    }

    @Override
    public String getPublicKeyAtIndexURL64(int i) throws InvalidWalletIndexException, PublicKeySerializzationException {
        if (!hexPublicKeys.containsKey(i)) {
            synchronized (getPublicKeyAtIndexHexLock) {
                try {
                    AsymmetricCipherKeyPair keyPairAtIndex = getKeyPairAtIndex(i);
                    UrlBase64 b64e = new UrlBase64();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    AsymmetricKeyParameter aPublic = keyPairAtIndex.getPublic();
                    Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) aPublic;
                    b64e.encode(publicKey.getEncoded(), baos);
                    hexPublicKeys.put(i, baos.toString());
                    baos.close();
                } catch (IOException ex) {
                    log.error("Wallet can not serialize public key", ex);
                    throw new PublicKeySerializzationException(ex);
                }

            }
        }
        return hexPublicKeys.get(i);
    }

    @Override
    public byte[] getPublicKeyAtIndexByte(int i) throws InvalidWalletIndexException, PublicKeySerializzationException {
        if (!bytePublicKeys.containsKey(i)) {
            synchronized (getPublicKeyAtIndexByteLock) {
                try {
                    AsymmetricCipherKeyPair keyPairAtIndex = getKeyPairAtIndex(i);
                    UrlBase64 b64e = new UrlBase64();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    AsymmetricKeyParameter aPublic = keyPairAtIndex.getPublic();
                    Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) aPublic;
                    b64e.encode(publicKey.getEncoded(), baos);
                    bytePublicKeys.put(i, baos.toByteArray());
                    baos.close();
                } catch (IOException ex) {
                    log.error("Wallet can not serialize public key", ex);
                    throw new PublicKeySerializzationException(ex);
                }

            }
        }
        return bytePublicKeys.get(i);
    }

    @Override
    public int compareTo(InstanceWalletKeystoreInterface t) {
        return getCurrentWalletID().compareTo(t.getCurrentWalletID());
    }

}
