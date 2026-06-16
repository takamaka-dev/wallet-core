/*
 * Multi-Index Test Vector Generator
 * Generates test vectors for validating Flutter port key derivation at multiple indices
 *
 * Run: mvn test -Dtest=MultiIndexTestVectorGenerator
 */
package io.takamaka.wallet;

import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.TkmSignUtils;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.util.encoders.UrlBase64;
import org.junit.jupiter.api.Test;

/**
 * Generates test vectors for cross-platform validation between Java wallet-core
 * and Flutter takamaka_wallet_core.
 *
 * This generates a valid 25-word mnemonic and outputs Ed25519 public keys
 * at multiple indices (0, 3, 7, 10, 15, 20) for Flutter test validation.
 */
public class MultiIndexTestVectorGenerator {

    public static void main(String[] args) throws Exception {
        new MultiIndexTestVectorGenerator().generateTestVectors();
    }

    @Test
    public void generateTestVectors() throws Exception {
        System.out.println("=== MULTI-INDEX TEST VECTOR GENERATOR ===\n");

        // Initialize dictionary
        SeedGenerator.init();

        // Generate a VALID 25-word mnemonic
        System.out.println("Generating valid 25-word mnemonic...\n");
        List<String> words = SeedGenerator.generateWords();

        // Verify it's valid
        boolean isValid = SeedGenerator.verifySeedWords(words);
        System.out.println("Mnemonic is valid: " + isValid);
        if (!isValid) {
            System.err.println("ERROR: Generated mnemonic failed verification!");
            return;
        }

        // Generate seed from words
        String seed = SeedGenerator.generateSeedPWH(words);
        System.out.println("Seed generated successfully.\n");

        // Test indices
        int[] indices = {0, 3, 7, 10, 15, 20};

        // Output in Dart test format
        System.out.println("=== FLUTTER TEST CODE ===\n");

        System.out.println("// Test mnemonic generated from Java wallet-core (reference implementation)");
        System.out.println("// Generated: " + java.time.LocalDate.now());
        System.out.println("// This is a VALID mnemonic (23 random words + 2 checksum words)");
        System.out.println("final words = [");
        for (int i = 0; i < words.size(); i++) {
            String comma = (i < words.size() - 1) ? "," : "";
            System.out.println("  '" + words.get(i) + "'" + comma);
        }
        System.out.println("];\n");

        System.out.println("// Expected Ed25519 public keys at each index (Base64URL encoded):");
        for (int index : indices) {
            String publicKey = getPublicKeyAtIndex(seed, index);
            System.out.println("// Index " + index + ":");
            System.out.println("'" + publicKey + "',");
            System.out.println();
        }

        // Output in assertion format
        System.out.println("\n=== FLUTTER TEST ASSERTIONS ===\n");

        System.out.println("// Map of index -> expected public key");
        System.out.println("final expectedKeys = {");
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            String publicKey = getPublicKeyAtIndex(seed, index);
            String comma = (i < indices.length - 1) ? "," : "";
            System.out.println("  " + index + ": '" + publicKey + "'" + comma);
        }
        System.out.println("};");

        System.out.println("\n// Individual test assertions:");
        for (int index : indices) {
            String publicKey = getPublicKeyAtIndex(seed, index);
            System.out.println("test('MIDX-" + String.format("%03d", index) + ": public key at index " + index + " matches Java', () {");
            System.out.println("  expect(");
            System.out.println("    wallet.getSigningPublicKeyUrl64(" + index + "),");
            System.out.println("    equals('" + publicKey + "'),");
            System.out.println("  );");
            System.out.println("});");
            System.out.println();
        }

        System.out.println("\n=== RAW VALUES FOR DEBUGGING ===\n");
        System.out.println("Seed: " + seed);
        System.out.println("Words: " + String.join(" ", words));
    }

    /**
     * Generates Ed25519 public key at given index using the same algorithm as
     * InstanceWalletKeyStoreBCED25519.getPublicKeyAtIndexURL64()
     */
    private String getPublicKeyAtIndex(String seed, int index) throws Exception {
        // Create SeededRandom with seed, wallet key chain scope, and keyNumber = index + 1
        SeededRandom seededRandom = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, index + 1);

        // Generate Ed25519 key pair
        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(seededRandom));
        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();

        // Get public key bytes
        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();
        byte[] pubKeyBytes = pubKey.getEncoded();

        // Encode as URL-safe Base64 (using BouncyCastle UrlBase64 like the wallet does)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UrlBase64.encode(pubKeyBytes, baos);
        return baos.toString();
    }
}
