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

import io.takamaka.wallet.exceptions.WalletException;
import io.takamaka.wallet.utils.KeyContexts;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.KeyAgreement;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

/**
 *
 * @author giovanni.antino@h2tcoin.com
 */
@Slf4j
public class TkmKeyExchangeDHBC {

    public static final byte[] generateKeySecret(byte[] recievedPublicKey, InstanceWalletKeystoreInterface iwk, int index) throws NoSuchAlgorithmException, WalletException, InvalidKeyException, NoSuchProviderException {
//        KeyAgreement keyAgAlg = KeyAgreement.getInstance("DH");
//        keyAgAlg.init((Key) iwk.getKeyPairAtIndex(index).getPrivate());
        AsymmetricCipherKeyPair kpA = iwk.getKeyPairAtIndex(index);
        X25519Agreement agreeA = new X25519Agreement();
        agreeA.init(kpA.getPrivate());
        byte[] secretA = new byte[agreeA.getAgreementSize()];
        log.info("size: " + secretA.length);
        agreeA.calculateAgreement((new X25519PrivateKeyParameters(recievedPublicKey)).generatePublicKey(), secretA, 0);
        return secretA;
        //keyAgAlg.doPhase(new , true)
    }
}
