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
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.util.encoders.UrlBase64;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class InstanceWalletKeyStoreBCECIES implements InstanceWalletKeystoreInterface {

    private Map<Integer, AsymmetricCipherKeyPair> signKeys;
    private Map<Integer, String> hexPublicKeys;
    private Map<Integer, byte[]> bytePublicKeys;
    private String seed;
    private String currentWalletName;
    private boolean isInitialized; //default to false
    private final static KeyContexts.WalletCypher walletCypher = KeyContexts.WalletCypher.ECIESBCsecp521r1;
    private final Object constructorLock = new Object();
    private final Object getKeyPairAtIndexLock = new Object();
    private final Object getPublicKeyAtIndexHexLock = new Object();
    private final Object getPublicKeyAtIndexByteLock = new Object();

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
     * It initializes the collections for key pairs and public keys, and calls
     * initWallet method to initialize or load an existing wallet using a
     * default hardcoded password
     *
     * @param walletName the name of the wallet file
     * @throws UnlockWalletException if there is an error with unlocking the
     * wallet
     */
    public InstanceWalletKeyStoreBCECIES(String walletName) throws UnlockWalletException {
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
                    throw new UnlockWalletException(ex);
                }
                isInitialized = true;
            }
        }

    }

    /**
     * It initializes the collections for key pairs and public keys, and calls
     * initWallet method to initialize or load an existing wallet
     *
     * @param walletName the name of the wallet file
     * @param password the password used to encrypt the keyfile
     * @throws UnlockWalletException if there is an error with unlocking the
     * wallet
     */
    public InstanceWalletKeyStoreBCECIES(String walletName, String password) throws UnlockWalletException {
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
                    throw new UnlockWalletException("instance error name password", ex);
                }
                isInitialized = true;
            }
        }

    }

    /**
          * It initializes the collections for key pairs and public keys, and calls
     * initWallet method to initialize or load an existing wallet
     *
     * @param walletName the name of the wallet file
     * @param nCharForSeed the number of characters for the seed
     * @throws UnlockWalletException if there is an error with unlocking the
     * wallet
     * @throws WalletEmptySeedException if the seed is empty
     * @throws WalletBurnedException if the seed is "burned"
     */
    public InstanceWalletKeyStoreBCECIES(String walletName, int nCharForSeed) throws UnlockWalletException, WalletEmptySeedException, WalletBurnedException {
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
        if (!FileHelper.walletDirExists()) {
            //FileHelper.createDir(FileHelper.getDefaultWalletDirectoryPath());
            FileHelper.createDir(FileHelper.getEphemeralWalletDirectoryPath());
        }
        if (!FileHelper.fileExists(Paths.get(FileHelper.getDefaultWalletDirectoryPath().toString(), currentWalletName))) {
            List<String> words = SeedGenerator.generateWords();
            seed = SeedGenerator.generateSeedPWH(words);

            String concat = words.get(0);
            for (int i = 1; i < words.size(); i++) {
                concat += " " + words.get(i);
            }

            KeyBean kb = new KeyBean("POWSEED", KeyContexts.WalletCypher.ECIESBCsecp521r1, seed, concat);
            try {
                WalletHelper.writeKeyFile(FileHelper.getDefaultWalletDirectoryPath(), currentWalletName, kb, password);

            } catch (NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                log.error("instance error password", ex);
                throw new UnlockWalletException("instance error password", ex);
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
    private void initWallet(int nCharSeed) throws IOException, NoSuchAlgorithmException, HashEncodeException, InvalidKeySpecException, HashAlgorithmNotFoundException, HashProviderNotFoundException, UnlockWalletException, WalletBurnedException, WalletEmptySeedException {
        if (!FileHelper.walletDirExists()) {
            FileHelper.createDir(FileHelper.getEphemeralWalletDirectoryPath());
        }
        if (!FileHelper.fileExists(Paths.get(FileHelper.getEphemeralWalletDirectoryPath().toString(), currentWalletName))) {
            RandomStringGenerator generator = new RandomStringGenerator.Builder()
                    .withinRange('0', 'z')
                    .filteredBy(Character::isLetterOrDigit)
                    .get();
            seed = generator.generate(nCharSeed);
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
                throw new IOException("instance error nseed", ex);
            }
        }
    }

    /**
     * Retrieve the keypair at a specific index in the wallet.
     *
     * If the keypair is not yet stored in the signKeys collection, it will be
     * generated using ECsecp521r1, initialized with a seed and an
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
                //ECKeyPairGenerator ecKeyPairGenerator = new ECKeyPairGenerator();
                if (index < 0 || index >= Integer.MAX_VALUE) {
                    throw new InvalidWalletIndexException("index outside wallet range");
                }
                X9ECParameters spec = SECNamedCurves.getByName("secp521r1");
                ECDomainParameters ecDomain = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN());
                ECKeyPairGenerator ecGen = new ECKeyPairGenerator();
                //ECIESKeyEncapsulation eciesKeyEncapsulation = new ECIESKeyEncapsulation(new KDF1BytesGenerator(new SHA256Digest()), new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, index + 1));
                ecGen.init(new ECKeyGenerationParameters(ecDomain, new SeededRandom(seed, KeyContexts.ECsecp521r1KEM, index + 1)));
                
                signKeys.put(index, ecGen.generateKeyPair());
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
                    
                    ECPublicKeyParameters publicKey = (ECPublicKeyParameters) aPublic;
                    UrlBase64.encode(publicKey.getQ().getEncoded(false), baos);
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
                    ECPublicKeyParameters publicKey = (ECPublicKeyParameters) aPublic;
                    //UrlBase64.encode(publicKey.getEncoded(), baos);
                    bytePublicKeys.put(index, publicKey.getQ().getEncoded(false));
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
