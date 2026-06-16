/*
 * Simple key derivation utility for cross-platform testing
 */
package io.takamaka.wallet;

import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.SeedGenerator;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.util.encoders.UrlBase64;

/**
 * Derives Ed25519 public keys for test users.
 */
public class DeriveTestUserKeys {

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

    public static void main(String[] args) throws Exception {
        // Initialize dictionary
        SeedGenerator.init();

        System.out.println("=== Cross-Platform User Key Derivation ===");
        System.out.println();

        // Verify mnemonics
        System.out.println("--- Mnemonic Validation ---");
        boolean shellValid = SeedGenerator.verifySeedWords(SHELL_MNEMONIC);
        boolean flutterValid = SeedGenerator.verifySeedWords(FLUTTER_MNEMONIC);
        System.out.println("Shell mnemonic valid: " + shellValid);
        System.out.println("Flutter mnemonic valid: " + flutterValid);
        System.out.println();

        if (!shellValid || !flutterValid) {
            System.err.println("ERROR: Invalid mnemonic checksum!");
            System.exit(1);
        }

        // Derive seeds
        String shellSeed = SeedGenerator.generateSeedPWH(SHELL_MNEMONIC);
        String flutterSeed = SeedGenerator.generateSeedPWH(FLUTTER_MNEMONIC);

        // Derive public keys
        String shellPubKey = deriveEd25519PublicKey(shellSeed, SHELL_KEY_INDEX);
        String flutterPubKey = deriveEd25519PublicKey(flutterSeed, FLUTTER_KEY_INDEX);

        System.out.println("=== DERIVED PUBLIC KEYS ===");
        System.out.println();
        System.out.println("SHELL USER (pollocrypt2, index " + SHELL_KEY_INDEX + "):");
        System.out.println("  Ed25519 Public Key: " + shellPubKey);
        System.out.println();
        System.out.println("FLUTTER USER (index " + FLUTTER_KEY_INDEX + "):");
        System.out.println("  Ed25519 Public Key: " + flutterPubKey);
        System.out.println();

        System.out.println("=== DATABASE VERIFICATION QUERY ===");
        System.out.println();
        System.out.println("Run this command to check encryption types:");
        System.out.println();
        System.out.println("PGPASSWORD=chatsqlpassword psql -h 127.0.0.1 -p 5432 -U chatsqluser -d chatsqldb \\");
        System.out.println("  -c \"SELECT identity_public_key, encryption_key_type FROM chatsqlschema.registered_users \\");
        System.out.println("       WHERE identity_public_key IN ('" + shellPubKey + "', '" + flutterPubKey + "');\"");
        System.out.println();

        System.out.println("=== COMPATIBILITY REQUIREMENTS ===");
        System.out.println();
        System.out.println("For full cross-platform (Java <-> Flutter) compatibility:");
        System.out.println("  - Both users need: encryption_key_type = 'RSA_4096_ECB_OAEP_SHA256'");
        System.out.println("  - 'RSA_4096_ECB_OAEP' (legacy) = partial compatibility (SHA-256/SHA-1 for MGF1)");
        System.out.println("  - 'RSA' (old) = NOT compatible with Flutter PointyCastle");
    }

    private static String deriveEd25519PublicKey(String seed, int index) throws Exception {
        // Note: InstanceWalletKeyStoreBCED25519 uses index + 1
        SeededRandom seededRandom = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, index + 1);

        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(seededRandom));

        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();
        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();

        byte[] pubKeyBytes = pubKey.getEncoded();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UrlBase64.encode(pubKeyBytes, baos);
        return baos.toString();
    }
}
