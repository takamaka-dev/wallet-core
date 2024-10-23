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
import io.takamaka.wallet.utils.TkmSignUtils;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class TkmCypherProviderBCX25519 {

    public static final byte[] calculateAgreement(CipherParameters privateKey, CipherParameters otherPublicKey) {
        X25519Agreement agree = new X25519Agreement();
        agree.init(privateKey);
        byte[] secret = new byte[agree.getAgreementSize()];
        agree.calculateAgreement(otherPublicKey, secret, 0);
        return secret;
    }

    public static final byte[] calculateAgreement(CipherParameters privateKey, String otherPublicKey) throws KeyDecodeException {
        X25519Agreement agree = new X25519Agreement();
        agree.init(privateKey);
        byte[] secret = new byte[agree.getAgreementSize()];
        X25519PublicKeyParameters publicKeyParamX25519 = TkmSignUtils.getPublicKeyParamX25519(otherPublicKey);
        agree.calculateAgreement(publicKeyParamX25519, secret, 0);
        return secret;
    }
}
