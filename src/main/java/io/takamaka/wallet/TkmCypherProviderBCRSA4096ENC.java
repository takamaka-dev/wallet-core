/*
 * Copyright 2024 AiliA SA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.takamaka.wallet;

import io.takamaka.wallet.exceptions.KeyDecodeException;
import io.takamaka.wallet.exceptions.WalletException;
import io.takamaka.wallet.utils.FixedParameters;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.TkmSignUtils;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class TkmCypherProviderBCRSA4096ENC {

    /**
     * Creates OAEPParameterSpec with SHA-256 OAEP hash and SHA-1 MGF1.
     * This is the legacy format (JCE default MGF1 behavior).
     *
     * @return OAEPParameterSpec configured for legacy compatibility
     */
    private static OAEPParameterSpec createOAEPParams() {
        return new OAEPParameterSpec(
                "SHA-256",                      // OAEP hash algorithm
                "MGF1",                         // Mask generation function
                MGF1ParameterSpec.SHA1,         // MGF1 hash algorithm (legacy)
                PSource.PSpecified.DEFAULT      // Empty label (P parameter)
        );
    }

    /**
     * Encrypts plaintext using RSA-4096 OAEP with SHA-256 hash and SHA-1 MGF1 (legacy).
     *
     * @param rsaPublicKey the RSA public key in URL-safe Base64 format
     * @param plaintext the plaintext to encrypt
     * @return Base64-encoded ciphertext
     * @throws WalletException if encryption fails
     */
    public static final String encrypt(String rsaPublicKey, String plaintext) throws WalletException {
        try {
            Cipher cipher = Cipher.getInstance(KeyContexts.BC_RSA_4096ENC_SHA256_FORMAT);
            cipher.init(Cipher.ENCRYPT_MODE,
                    TkmSignUtils.stringPublicKeyToBCRSA4096ENCKey(rsaPublicKey),
                    createOAEPParams());
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(FixedParameters.CHARSET));
            return Base64.toBase64String(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException | KeyDecodeException |
                 NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException ex) {
            log.error("Error during encryption process", ex);
            throw new WalletException(ex);
        }
    }

    /**
     * Decrypts ciphertext using RSA-4096 OAEP with SHA-256 hash and SHA-1 MGF1 (legacy).
     *
     * @param iwk the wallet keystore interface
     * @param index the key index
     * @param cyphertext the Base64-encoded ciphertext
     * @return decrypted plaintext
     * @throws WalletException if decryption fails
     */
    public static final String decrypt(InstanceWalletKeystoreInterface iwk, int index, String cyphertext) throws WalletException {
        try {
            AsymmetricKeyParameter aPrivate = iwk.getKeyPairAtIndex(index).getPrivate();
            RSAPrivateKey asymmetricKeyParameterToRSAPrivateKey = TkmSignUtils.asymmetricKeyParameterToRSAPrivateKey(aPrivate);
            return decryptToString(asymmetricKeyParameterToRSAPrivateKey, cyphertext);
        } catch (KeyDecodeException | IllegalBlockSizeException | BadPaddingException |
                 NoSuchAlgorithmException | NoSuchPaddingException | WalletException |
                 InvalidKeyException | InvalidAlgorithmParameterException ex) {
            log.error("Error during decryption process", ex);
            throw new WalletException(ex);
        }
    }

    /**
     * Decrypts ciphertext using RSA-4096 OAEP with SHA-256 hash and SHA-1 MGF1 (legacy).
     *
     * @param pkcs8encodedB64URL the PKCS8-encoded private key in URL-safe Base64 format
     * @param cyphertext the Base64-encoded ciphertext
     * @return decrypted plaintext
     * @throws WalletException if decryption fails
     */
    public static final String decrypt(String pkcs8encodedB64URL, String cyphertext) throws WalletException {
        try {
            RSAPrivateKey fromPKCS8EncodedKeyB64URLToRSAPrivateKey = TkmSignUtils.fromPKCS8EncodedKeyB64URLToRSAPrivateKey(pkcs8encodedB64URL);
            return decryptToString(fromPKCS8EncodedKeyB64URLToRSAPrivateKey, cyphertext);
        } catch (KeyDecodeException | IllegalBlockSizeException | InvalidKeyException |
                 BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidAlgorithmParameterException ex) {
            log.error("Error during decryption process", ex);
            throw new WalletException(ex);
        }
    }

    /**
     * Decrypts ciphertext to byte array using RSA-4096 OAEP with SHA-256 hash and SHA-1 MGF1 (legacy).
     *
     * @param asymmetricKeyParameterToRSAPrivateKey the RSA private key
     * @param cyphertext the Base64-encoded ciphertext
     * @return decrypted bytes
     * @throws IllegalBlockSizeException if block size is invalid
     * @throws InvalidKeyException if key is invalid
     * @throws BadPaddingException if padding is invalid
     * @throws NoSuchAlgorithmException if algorithm is not found
     * @throws NoSuchPaddingException if padding scheme is not found
     * @throws InvalidAlgorithmParameterException if algorithm parameters are invalid
     */
    public static byte[] decryptToByte(RSAPrivateKey asymmetricKeyParameterToRSAPrivateKey, String cyphertext)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
                   NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(KeyContexts.BC_RSA_4096ENC_SHA256_FORMAT);
        cipher.init(Cipher.DECRYPT_MODE, asymmetricKeyParameterToRSAPrivateKey, createOAEPParams());
        byte[] decrypted = cipher.doFinal(Base64.decode(cyphertext));
        return decrypted;
    }

    /**
     * Decrypts ciphertext to String using RSA-4096 OAEP with SHA-256 hash and SHA-1 MGF1 (legacy).
     *
     * @param asymmetricKeyParameterToRSAPrivateKey the RSA private key
     * @param cyphertext the Base64-encoded ciphertext
     * @return decrypted string
     * @throws IllegalBlockSizeException if block size is invalid
     * @throws InvalidKeyException if key is invalid
     * @throws BadPaddingException if padding is invalid
     * @throws NoSuchAlgorithmException if algorithm is not found
     * @throws NoSuchPaddingException if padding scheme is not found
     * @throws InvalidAlgorithmParameterException if algorithm parameters are invalid
     */
    public static String decryptToString(RSAPrivateKey asymmetricKeyParameterToRSAPrivateKey, String cyphertext)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
                   NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] decryptToByte = decryptToByte(asymmetricKeyParameterToRSAPrivateKey, cyphertext);
        return new String(decryptToByte, FixedParameters.CHARSET);
    }

}
