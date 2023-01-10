/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet;

import io.takamaka.crypto.tkmsecurityprovider.util.adaptor.r1.QTR1KeyPairGenerator;
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
import io.takamaka.wallet.utils.TkmTextUtils;
import io.takamaka.wallet.utils.WalletHelper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
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
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

/**
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
 */
@Slf4j
public class InstanceWalletKeyStoreBCQTESLAPSSC1Round1 implements InstanceWalletKeystoreInterface {

    private Map<Integer, AsymmetricCipherKeyPair> signKeys;
    private Map<Integer, String> hexPublicKeys;
    private Map<Integer, byte[]> bytePublicKeys;
    private String seed;
    private String currentWalletName;
    private boolean isInitialized; //default to false
    private final static KeyContexts.WalletCypher walletCypher = KeyContexts.WalletCypher.BCQTESLA_PS_1;
    private final Object constructorLock = new Object();
    private final Object getKeyPairAtIndexLock = new Object();
    private final Object getPublicKeyAtIndexHexLock = new Object();
    private final Object getPublicKeyAtIndexByteLock = new Object();

    @Override
    public KeyContexts.WalletCypher getWalletCypher() {
        return walletCypher;
    }

    public InstanceWalletKeyStoreBCQTESLAPSSC1Round1(String walletName) throws UnlockWalletException {
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
                }
                isInitialized = true;
            }
        }
    }

    public InstanceWalletKeyStoreBCQTESLAPSSC1Round1(String walletName, String password) throws UnlockWalletException {
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
                }
                isInitialized = true;
            }
        }
    }

    public InstanceWalletKeyStoreBCQTESLAPSSC1Round1(String walletName, int nCharSeed) throws UnlockWalletException, WalletBurnedException, WalletEmptySeedException {
        synchronized (constructorLock) {
            if (!isInitialized) {
                try {
                    currentWalletName = walletName + DefaultInitParameters.WALLET_EXTENSION;
                    signKeys = Collections.synchronizedMap(new HashMap<Integer, AsymmetricCipherKeyPair>());
                    hexPublicKeys = Collections.synchronizedMap(new HashMap<Integer, String>());
                    bytePublicKeys = Collections.synchronizedMap(new HashMap<Integer, byte[]>());
                    initWallet(nCharSeed);
                } catch (IOException | NoSuchAlgorithmException | HashEncodeException | InvalidKeySpecException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
                    log.error("instance error seed", ex);
                }
                isInitialized = true;
            }
        }
    }

    private void initWallet(int nCharSeed) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException, WalletBurnedException, WalletEmptySeedException {
        if (!FileHelper.walletDirExists()) {

            try {
                FileHelper.createDir(FileHelper.getEphemeralWalletDirectoryPath());
            } catch (IOException e) {
                log.error("Error creating dir", e);
            }

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
            }
        }
    }

    private void initWallet(String password) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException {
        if (!FileHelper.walletDirExists()) {
            FileHelper.createDir(FileHelper.getDefaultWalletDirectoryPath());
        }
        if (!FileHelper.fileExists(Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), currentWalletName))) {

            List<String> words = SeedGenerator.generateWords();
            seed = SeedGenerator.generateSeedPWH(words);

            String concat = words.get(0);
            for (int i = 1; i < words.size(); i++) {
                concat += " " + words.get(i);
            }

            KeyBean kb = new KeyBean("POWSEED", KeyContexts.WalletCypher.BCQTESLA_PS_1, seed, concat);
            try {
                WalletHelper.writeKeyFile(FileHelper.getDefaultWalletDirectoryPath(), currentWalletName, kb, password);
            } catch (NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                log.error("instance error password", ex);
            }
        }
        Path currentWalletPath = Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), currentWalletName);

        if (FileHelper.fileExists(currentWalletPath)) {
            try {
                seed = WalletHelper.readKeyFile(currentWalletPath, password).getSeed();
            } catch (InvalidAlgorithmParameterException | FileNotFoundException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException ex) {
                log.error("initWallet unreadable file?", ex);
            }
        }
    }

    @Override
    public AsymmetricCipherKeyPair getKeyPairAtIndex(int i) throws InvalidWalletIndexException {
        if (Security.getProvider("BCPQC") == null) {
            Security.addProvider(new BouncyCastlePQCProvider());
        }
        if (!signKeys.containsKey(i)) {
            synchronized (getKeyPairAtIndexLock) {
                if (i < 0 || i >= Integer.MAX_VALUE) {
                    throw new InvalidWalletIndexException("index outside wallet range");
                }

                signKeys.put(i, QTR1KeyPairGenerator.getKeyPair(new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, i + 1)));
            }
        }
        return signKeys.get(i);
    }

    @Override
    public String getPublicKeyAtIndexURL64(int i) throws InvalidWalletIndexException, PublicKeySerializzationException {
        if (!hexPublicKeys.containsKey(i)) {
            synchronized (getPublicKeyAtIndexHexLock) {
                try {

                    hexPublicKeys.put(i, QTR1KeyPairGenerator.getStringPublicKey(getKeyPairAtIndex(i)));
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

                    bytePublicKeys.put(i, QTR1KeyPairGenerator.getBytePublicKey(getKeyPairAtIndex(i)));
                } catch (IOException ex) {
                    log.error("Wallet can not serialize public key", ex);
                    throw new PublicKeySerializzationException(ex);
                }

            }
        }
        return bytePublicKeys.get(i);
    }

    @Override
    public String getCurrentWalletID() {
        return currentWalletName + walletCypher.name();
    }

    @Override
    public int compareTo(InstanceWalletKeystoreInterface t) {
        return getCurrentWalletID().compareTo(t.getCurrentWalletID());
    }

}
