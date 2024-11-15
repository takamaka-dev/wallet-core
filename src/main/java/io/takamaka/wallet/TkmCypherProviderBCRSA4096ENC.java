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
import io.takamaka.wallet.exceptions.PublicKeySerializzationException;
import io.takamaka.wallet.exceptions.WalletException;
import io.takamaka.wallet.utils.FixedParameters;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.TkmSignUtils;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class TkmCypherProviderBCRSA4096ENC {

    public static final String encrypt(String rsaPublicKey, String plaintext) throws WalletException {
        try {
            Cipher cipher = Cipher.getInstance(KeyContexts.BC_RSA_4096ENC_FORMAT);
            cipher.init(Cipher.ENCRYPT_MODE, TkmSignUtils.stringPublicKeyToBCRSA4096ENCKey(rsaPublicKey));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(FixedParameters.CHARSET));
            return Base64.toBase64String(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException | KeyDecodeException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            log.error("Wallet can not serialize public key", ex);
            throw new WalletException(ex);
        }
    }

    public static final String decrypt(InstanceWalletKeystoreInterface iwk, int index, String cyphertext) throws WalletException {
        try {

            AsymmetricKeyParameter aPrivate = iwk.getKeyPairAtIndex(index).getPrivate();
            RSAPrivateKey asymmetricKeyParameterToRSAPrivateKey = TkmSignUtils.asymmetricKeyParameterToRSAPrivateKey(aPrivate);
            //byte[] decrypted = decryptToByte(asymmetricKeyParameterToRSAPrivateKey, cyphertext);
            return decryptToString(asymmetricKeyParameterToRSAPrivateKey, cyphertext);
        } catch (KeyDecodeException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | WalletException | InvalidKeyException ex) {
            log.error("Wallet can not serialize public key", ex);
            throw new WalletException(ex);
        }
    }

    public static final String decrypt(String pkcs8encodedB64URL, String cyphertext) throws WalletException {
        try {
            RSAPrivateKey fromPKCS8EncodedKeyB64URLToRSAPrivateKey = TkmSignUtils.fromPKCS8EncodedKeyB64URLToRSAPrivateKey(pkcs8encodedB64URL);
            return decryptToString(fromPKCS8EncodedKeyB64URLToRSAPrivateKey, cyphertext);
        } catch (KeyDecodeException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            log.error("Wallet can not serialize public key", ex);
            throw new WalletException(ex);
        }
    }

    public static byte[] decryptToByte(RSAPrivateKey asymmetricKeyParameterToRSAPrivateKey, String cyphertext) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance(KeyContexts.BC_RSA_4096ENC_FORMAT);
        cipher.init(Cipher.DECRYPT_MODE, asymmetricKeyParameterToRSAPrivateKey);
        byte[] decrypted = cipher.doFinal(Base64.decode(cyphertext));
        return decrypted;
    }

    public static String decryptToString(RSAPrivateKey asymmetricKeyParameterToRSAPrivateKey, String cyphertext) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] decryptToByte = decryptToByte(asymmetricKeyParameterToRSAPrivateKey, cyphertext);
        return new String(decryptToByte, FixedParameters.CHARSET);
    }

}
