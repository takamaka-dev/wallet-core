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

import io.takamaka.wallet.exceptions.UnlockWalletException;
import io.takamaka.wallet.exceptions.WalletException;
import io.takamaka.wallet.exceptions.InvalidWalletIndexException;
import io.takamaka.wallet.exceptions.PublicKeySerializzationException;
import io.takamaka.wallet.utils.TkmSignUtils;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RSA-4096 OAEP encryption with SHA-256 for both hash and MGF1.
 * These tests verify cross-platform compatibility with PointyCastle (Dart/Flutter).
 *
 * <p>The implementation uses:</p>
 * <ul>
 *   <li>OAEP Hash: SHA-256</li>
 *   <li>MGF1 Hash: SHA-256 (explicitly set via OAEPParameterSpec)</li>
 * </ul>
 *
 * <p>This matches PointyCastle's OAEPEncoding.withSHA256(RSAEngine()) in Dart.</p>
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class RSAOaepSHA256CrossPlatformTest {

    private static final String TEST_SEED = "crossplatform_rsa_test";
    private static final String TEST_PASSWORD = "testpassword";

    /**
     * Tests basic encrypt/decrypt round-trip with wallet keystore.
     */
    @Test
    public void testBasicEncryptDecryptWithKeystore() throws UnlockWalletException, WalletException {
        InstanceWalletKeyStoreBCRSA4096ENC iwk = new InstanceWalletKeyStoreBCRSA4096ENC(TEST_SEED, TEST_PASSWORD);
        String publicKey = iwk.getPublicKeyAtIndexURL64(0);
        String plaintext = "cross-platform-test-message-12345";

        // Encrypt
        String encrypted = TkmCypherProviderBCRSA4096ENC.encrypt(publicKey, plaintext);
        assertNotNull(encrypted, "Encrypted text should not be null");
        assertNotEquals(plaintext, encrypted, "Encrypted should differ from plaintext");

        // Decrypt
        String decrypted = TkmCypherProviderBCRSA4096ENC.decrypt(iwk, 0, encrypted);
        assertEquals(plaintext, decrypted, "Decrypted text should match original plaintext");

        log.info("Basic encrypt/decrypt test passed");
        log.info("Public Key: {}", publicKey);
        log.info("Plaintext: {}", plaintext);
        log.info("Ciphertext: {}", encrypted);
    }

    /**
     * Tests encrypt/decrypt with PKCS8-encoded private key.
     */
    @Test
    public void testEncryptDecryptWithPKCS8Key() throws UnlockWalletException, WalletException,
            InvalidWalletIndexException, PublicKeySerializzationException,
            io.takamaka.wallet.exceptions.KeyDecodeException {
        InstanceWalletKeyStoreBCRSA4096ENC iwk = new InstanceWalletKeyStoreBCRSA4096ENC(TEST_SEED, TEST_PASSWORD);
        String publicKey = iwk.getPublicKeyAtIndexURL64(0);

        AsymmetricKeyParameter aPrivate = iwk.getKeyPairAtIndex(0).getPrivate();
        String privateKeyPkcs8 = TkmSignUtils.fromRSAPrivateKeyToPKCS8EncodedKeyB64URL(aPrivate);

        String plaintext = "test-symmetric-key-for-topic-encryption";

        // Encrypt with public key
        String encrypted = TkmCypherProviderBCRSA4096ENC.encrypt(publicKey, plaintext);

        // Decrypt with PKCS8-encoded private key
        String decrypted = TkmCypherProviderBCRSA4096ENC.decrypt(privateKeyPkcs8, encrypted);
        assertEquals(plaintext, decrypted, "Decrypted text should match original plaintext");

        log.info("PKCS8 key encrypt/decrypt test passed");
    }

    /**
     * Tests encryption with multiple key indices.
     */
    @Test
    public void testMultipleKeyIndices() throws UnlockWalletException, WalletException {
        InstanceWalletKeyStoreBCRSA4096ENC iwk = new InstanceWalletKeyStoreBCRSA4096ENC(TEST_SEED, TEST_PASSWORD);

        for (int i = 0; i < 5; i++) {
            String publicKey = iwk.getPublicKeyAtIndexURL64(i);
            String plaintext = "message-for-key-index-" + i;

            String encrypted = TkmCypherProviderBCRSA4096ENC.encrypt(publicKey, plaintext);
            String decrypted = TkmCypherProviderBCRSA4096ENC.decrypt(iwk, i, encrypted);

            assertEquals(plaintext, decrypted, "Decrypted text should match for key index " + i);
        }

        log.info("Multiple key indices test passed");
    }

    /**
     * Tests that decryption with wrong key fails.
     */
    @Test
    public void testDecryptionWithWrongKeyFails() throws UnlockWalletException, WalletException {
        InstanceWalletKeyStoreBCRSA4096ENC iwk = new InstanceWalletKeyStoreBCRSA4096ENC(TEST_SEED, TEST_PASSWORD);

        String publicKey0 = iwk.getPublicKeyAtIndexURL64(0);
        String plaintext = "secret-message";

        // Encrypt with key index 0
        String encrypted = TkmCypherProviderBCRSA4096ENC.encrypt(publicKey0, plaintext);

        // Try to decrypt with key index 1 - should fail
        assertThrows(WalletException.class, () -> {
            TkmCypherProviderBCRSA4096ENC.decrypt(iwk, 1, encrypted);
        }, "Decryption with wrong key should throw WalletException");

        log.info("Wrong key decryption failure test passed");
    }

    /**
     * Tests encryption of typical symmetric key (400 characters as used in chat).
     */
    @Test
    public void testSymmetricKeyEncryption() throws UnlockWalletException, WalletException {
        InstanceWalletKeyStoreBCRSA4096ENC iwk = new InstanceWalletKeyStoreBCRSA4096ENC(TEST_SEED, TEST_PASSWORD);
        String publicKey = iwk.getPublicKeyAtIndexURL64(0);

        // Generate a 400-character symmetric key (typical for chat)
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 400; i++) {
            sb.append(chars.charAt(i % chars.length()));
        }
        String symmetricKey = sb.toString();

        String encrypted = TkmCypherProviderBCRSA4096ENC.encrypt(publicKey, symmetricKey);
        String decrypted = TkmCypherProviderBCRSA4096ENC.decrypt(iwk, 0, encrypted);

        assertEquals(symmetricKey, decrypted, "Symmetric key should be correctly encrypted/decrypted");
        assertEquals(400, decrypted.length(), "Decrypted key should have 400 characters");

        log.info("Symmetric key encryption test passed");
    }

    /**
     * Tests encryption of empty string.
     */
    @Test
    public void testEmptyStringEncryption() throws UnlockWalletException, WalletException {
        InstanceWalletKeyStoreBCRSA4096ENC iwk = new InstanceWalletKeyStoreBCRSA4096ENC(TEST_SEED, TEST_PASSWORD);
        String publicKey = iwk.getPublicKeyAtIndexURL64(0);

        String plaintext = "";

        String encrypted = TkmCypherProviderBCRSA4096ENC.encrypt(publicKey, plaintext);
        String decrypted = TkmCypherProviderBCRSA4096ENC.decrypt(iwk, 0, encrypted);

        assertEquals(plaintext, decrypted, "Empty string should be correctly encrypted/decrypted");

        log.info("Empty string encryption test passed");
    }

    /**
     * Tests encryption of Unicode characters.
     */
    @Test
    public void testUnicodeEncryption() throws UnlockWalletException, WalletException {
        InstanceWalletKeyStoreBCRSA4096ENC iwk = new InstanceWalletKeyStoreBCRSA4096ENC(TEST_SEED, TEST_PASSWORD);
        String publicKey = iwk.getPublicKeyAtIndexURL64(0);

        String plaintext = "Hello \u4e16\u754c! \u0421\u043f\u0430\u0441\u0438\u0431\u043e \u2764\ufe0f";

        String encrypted = TkmCypherProviderBCRSA4096ENC.encrypt(publicKey, plaintext);
        String decrypted = TkmCypherProviderBCRSA4096ENC.decrypt(iwk, 0, encrypted);

        assertEquals(plaintext, decrypted, "Unicode text should be correctly encrypted/decrypted");

        log.info("Unicode encryption test passed");
    }

    /**
     * Generates cross-platform test vectors for Dart validation.
     * Run this test and copy output to Dart test file.
     */
    @Test
    public void generateCrossPlatformTestVectors() throws UnlockWalletException, WalletException,
            InvalidWalletIndexException, PublicKeySerializzationException,
            io.takamaka.wallet.exceptions.KeyDecodeException {
        InstanceWalletKeyStoreBCRSA4096ENC iwk = new InstanceWalletKeyStoreBCRSA4096ENC(TEST_SEED, TEST_PASSWORD);

        String publicKey = iwk.getPublicKeyAtIndexURL64(0);
        AsymmetricKeyParameter aPrivate = iwk.getKeyPairAtIndex(0).getPrivate();
        String privateKeyPkcs8 = TkmSignUtils.fromRSAPrivateKeyToPKCS8EncodedKeyB64URL(aPrivate);

        String plaintext = "CrossPlatformTestMessage123";
        String encrypted = TkmCypherProviderBCRSA4096ENC.encrypt(publicKey, plaintext);

        log.info("=== CROSS-PLATFORM TEST VECTOR (RSA-OAEP SHA-256/SHA-256) ===");
        log.info("Seed: {}", TEST_SEED);
        log.info("Password: {}", TEST_PASSWORD);
        log.info("Key Index: 0");
        log.info("Public Key (URL64): {}", publicKey);
        log.info("Private Key (PKCS8 URL64): {}", privateKeyPkcs8);
        log.info("Plaintext: {}", plaintext);
        log.info("Ciphertext (Base64): {}", encrypted);
        log.info("=== END TEST VECTOR ===");

        // Verify the vector is valid by decrypting
        String decrypted = TkmCypherProviderBCRSA4096ENC.decrypt(iwk, 0, encrypted);
        assertEquals(plaintext, decrypted, "Test vector validation failed");

        log.info("Cross-platform test vector generated and validated");
    }
}
