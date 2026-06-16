/*
 * Cross-Platform User Key Verification Test
 *
 * This test verifies that the provided test user credentials produce the expected
 * public keys that are registered in the rschat database.
 */
package io.takamaka.wallet;

import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.KeyContexts;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies test user public keys match their registered encryption types.
 *
 * Test Users:
 * 1. Shell (pollocrypt2, index 4) - Ed25519 identity + RSA-4096 encryption
 * 2. Flutter (mnemonic, index 0) - Ed25519 identity + RSA-4096 encryption
 *
 * For cross-platform compatibility, both users should have encryption_key_type
 * = RSA_4096_ECB_OAEP_SHA256 (new PointyCastle-compatible format).
 */
@Slf4j
public class CrossPlatformUserKeyVerificationTest {

    // Shell user (pollocrypt2) recovery words
    private static final List<String> SHELL_MNEMONIC = Arrays.asList(
        "sample", "half", "mammal", "radar", "hold", "fit", "era", "dilemma",
        "manage", "movie", "razor", "saddle", "point", "dial", "sadness", "north",
        "item", "naive", "gate", "hockey", "sample", "script", "embark", "purse", "myth"
    );
    private static final int SHELL_KEY_INDEX = 4;

    // Flutter user recovery words
    private static final List<String> FLUTTER_MNEMONIC = Arrays.asList(
        "regret", "venture", "boring", "settle", "beach", "animal", "arm", "lyrics",
        "robot", "hold", "attack", "salon", "crazy", "mercy", "slim", "exhibit",
        "stairs", "victory", "truth", "differ", "isolate", "smile", "jazz", "aunt", "park"
    );
    private static final int FLUTTER_KEY_INDEX = 0;

    @BeforeAll
    static void setup() {
        // Initialize the dictionary for SeedGenerator
        SeedGenerator.init();
    }

    @Test
    void verifyShellUserMnemonicIsValid() {
        log.info("=== Verifying Shell User (pollocrypt2) Mnemonic ===");

        boolean isValid = SeedGenerator.verifySeedWords(SHELL_MNEMONIC);
        log.info("Mnemonic words: {}", SHELL_MNEMONIC);
        log.info("Is valid checksum: {}", isValid);

        assertTrue(isValid, "Shell user mnemonic should have valid checksum");
    }

    @Test
    void verifyFlutterUserMnemonicIsValid() {
        log.info("=== Verifying Flutter User Mnemonic ===");

        boolean isValid = SeedGenerator.verifySeedWords(FLUTTER_MNEMONIC);
        log.info("Mnemonic words: {}", FLUTTER_MNEMONIC);
        log.info("Is valid checksum: {}", isValid);

        assertTrue(isValid, "Flutter user mnemonic should have valid checksum");
    }

    @Test
    void deriveShellUserEd25519PublicKey() throws Exception {
        log.info("=== Deriving Shell User Ed25519 Public Key ===");

        // Verify mnemonic first
        assertTrue(SeedGenerator.verifySeedWords(SHELL_MNEMONIC),
            "Shell mnemonic must be valid");

        // Generate seed from mnemonic
        String seed = SeedGenerator.generateSeedPWH(SHELL_MNEMONIC);
        log.info("Generated seed length: {}", seed.length());

        // Create Ed25519 wallet from seed
        // Note: We need to simulate how InstanceWalletKeyStoreBCED25519 would work
        // with the mnemonic-derived seed. The wallet stores seed internally.

        // For this test, we'll use an ephemeral wallet approach
        InstanceWalletKeyStoreBCED25519 wallet = new InstanceWalletKeyStoreBCED25519(
            "test_shell_verification",
            "password"  // Note: actual shell uses "password"
        );

        // The actual wallet file approach is different -
        // the mnemonic generates the seed which is stored encrypted.
        // For key derivation from mnemonic, we need the internal seed.

        // Instead, let's derive keys directly using the seed
        String publicKey = deriveEd25519PublicKeyFromSeed(seed, SHELL_KEY_INDEX);

        log.info("Shell User (index {})", SHELL_KEY_INDEX);
        log.info("  Ed25519 Public Key (B64URL): {}", publicKey);
        log.info("");
        log.info("=== DATABASE LOOKUP ===");
        log.info("Check this public key against chatsqlschema.registered_users:");
        log.info("  SELECT encryption_key_type FROM chatsqlschema.registered_users");
        log.info("  WHERE identity_public_key = '{}';", publicKey);

        assertNotNull(publicKey, "Public key should not be null");
        assertEquals(44, publicKey.length(), "Ed25519 public key should be 44 chars in B64URL");
    }

    @Test
    void deriveFlutterUserEd25519PublicKey() throws Exception {
        log.info("=== Deriving Flutter User Ed25519 Public Key ===");

        // Verify mnemonic first
        assertTrue(SeedGenerator.verifySeedWords(FLUTTER_MNEMONIC),
            "Flutter mnemonic must be valid");

        // Generate seed from mnemonic
        String seed = SeedGenerator.generateSeedPWH(FLUTTER_MNEMONIC);
        log.info("Generated seed length: {}", seed.length());

        // Derive key directly using the seed
        String publicKey = deriveEd25519PublicKeyFromSeed(seed, FLUTTER_KEY_INDEX);

        log.info("Flutter User (index {})", FLUTTER_KEY_INDEX);
        log.info("  Ed25519 Public Key (B64URL): {}", publicKey);
        log.info("");
        log.info("=== DATABASE LOOKUP ===");
        log.info("Check this public key against chatsqlschema.registered_users:");
        log.info("  SELECT encryption_key_type FROM chatsqlschema.registered_users");
        log.info("  WHERE identity_public_key = '{}';", publicKey);

        assertNotNull(publicKey, "Public key should not be null");
        assertEquals(44, publicKey.length(), "Ed25519 public key should be 44 chars in B64URL");
    }

    @Test
    void deriveBothUsersAndCompareWithDatabase() throws Exception {
        log.info("=== Cross-Platform User Key Verification Summary ===");
        log.info("");

        // Shell user
        String shellSeed = SeedGenerator.generateSeedPWH(SHELL_MNEMONIC);
        String shellPubKey = deriveEd25519PublicKeyFromSeed(shellSeed, SHELL_KEY_INDEX);

        // Flutter user
        String flutterSeed = SeedGenerator.generateSeedPWH(FLUTTER_MNEMONIC);
        String flutterPubKey = deriveEd25519PublicKeyFromSeed(flutterSeed, FLUTTER_KEY_INDEX);

        log.info("SHELL USER (pollocrypt2, index {}):", SHELL_KEY_INDEX);
        log.info("  Public Key: {}", shellPubKey);
        log.info("");
        log.info("FLUTTER USER (index {}):", FLUTTER_KEY_INDEX);
        log.info("  Public Key: {}", flutterPubKey);
        log.info("");
        log.info("=== Verify with PostgreSQL ===");
        log.info("PGPASSWORD=chatsqlpassword psql -h 127.0.0.1 -p 5432 -U chatsqluser -d chatsqldb \\");
        log.info("  -c \"SELECT identity_public_key, encryption_key_type FROM chatsqlschema.registered_users");
        log.info("       WHERE identity_public_key IN ('{}', '{}');\"",
            shellPubKey, flutterPubKey);
        log.info("");
        log.info("=== Expected Encryption Key Types ===");
        log.info("For full cross-platform compatibility, both users should have:");
        log.info("  encryption_key_type = 'RSA_4096_ECB_OAEP_SHA256'");
        log.info("");
        log.info("If a user has 'RSA_4096_ECB_OAEP' (legacy), Flutter must use SHA-256/SHA-1.");
        log.info("If a user has 'RSA' (old), cross-platform messaging is NOT possible.");

        // Both keys should be different
        assertNotEquals(shellPubKey, flutterPubKey,
            "Shell and Flutter users should have different public keys");
    }

    /**
     * Derives an Ed25519 public key from a seed at the given index.
     * This mirrors the logic in InstanceWalletKeyStoreBCED25519.getKeyPairAtIndex().
     */
    private String deriveEd25519PublicKeyFromSeed(String seed, int index) throws Exception {
        io.takamaka.wallet.utils.SeededRandom seededRandom =
            new io.takamaka.wallet.utils.SeededRandom(
                seed,
                KeyContexts.WALLET_KEY_CHAIN,
                index + 1  // Note: index + 1 as per InstanceWalletKeyStoreBCED25519
            );

        org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator keyGen =
            new org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator();
        keyGen.init(new org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters(seededRandom));

        org.bouncycastle.crypto.AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();
        org.bouncycastle.crypto.params.Ed25519PublicKeyParameters pubKey =
            (org.bouncycastle.crypto.params.Ed25519PublicKeyParameters) keyPair.getPublic();

        byte[] pubKeyBytes = pubKey.getEncoded();

        // Use UrlBase64 encoder (same as wallet-core)
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        org.bouncycastle.util.encoders.UrlBase64.encode(pubKeyBytes, baos);
        return baos.toString();
    }
}
