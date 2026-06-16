/*
 * Cross-Platform Test Vector Generator
 * Generates test vectors for validating Flutter port compatibility
 */
package io.takamaka.wallet;

import io.takamaka.wallet.utils.FixedParameters;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.TkmSignUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.util.Strings;

/**
 * Generates test vectors for cross-platform validation between Java wallet-core
 * and Flutter takamaka_wallet_core.
 *
 * Run this class to generate JSON test vectors that can be used to validate
 * the Flutter implementation produces identical output.
 */
public class CrossPlatformTestVectorGenerator {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Cross-Platform Test Vector Generator ===\n");

        // Initialize dictionary
        SeedGenerator.init();

        generatePbkdf2Vectors();
        generateSeedGenerationVectors();
        generateSeededRandomVectors();
        generateEd25519KeyVectors();

        System.out.println("\n=== JSON Test Vectors ===\n");
        generateJsonOutput();
    }

    private static void generatePbkdf2Vectors() throws Exception {
        System.out.println("--- PBKDF2 Test Vectors ---");

        // Vector 1: Simple test
        byte[] hash1 = TkmSignUtils.PWHash("test", "salt", 1, 256);
        System.out.println("PBKDF2-001:");
        System.out.println("  Password: \"test\"");
        System.out.println("  Salt: \"salt\"");
        System.out.println("  Iterations: 1");
        System.out.println("  KeyLength: 256 bits");
        System.out.println("  Output (hex): " + bytesToHex(hash1));
        System.out.println("  Output (b64url): " + TkmSignUtils.fromByteArrayToB64URL(hash1));

        // Vector 2: Wallet key chain scope
        byte[] hash2 = TkmSignUtils.PWHash("test-seed", "__WKCH__", 1, 256);
        System.out.println("\nPBKDF2-002:");
        System.out.println("  Password: \"test-seed\"");
        System.out.println("  Salt: \"__WKCH__\"");
        System.out.println("  Iterations: 1");
        System.out.println("  KeyLength: 256 bits");
        System.out.println("  Output (hex): " + bytesToHex(hash2));
        System.out.println("  Output (b64url): " + TkmSignUtils.fromByteArrayToB64URL(hash2));

        // Vector 3: Seed checksum parameters (as used in generateWords)
        byte[] hash3 = TkmSignUtils.PWHash("abandon", "TakamakaWalletWords", 1, 4096);
        System.out.println("\nPBKDF2-003:");
        System.out.println("  Password: \"abandon\"");
        System.out.println("  Salt: \"TakamakaWalletWords\"");
        System.out.println("  Iterations: 1");
        System.out.println("  KeyLength: 4096 bits");
        System.out.println("  Output (hex): " + bytesToHex(hash3));
        System.out.println("  BigInt value mod 2048: " + new BigInteger(hash3).mod(BigInteger.valueOf(2048)).intValue());

        // Vector 4: PWHashB64 (Base64URL output)
        String hash4 = TkmSignUtils.PWHashB64("test", "salt", 1, 768);
        System.out.println("\nPBKDF2-004 (PWHashB64):");
        System.out.println("  Password: \"test\"");
        System.out.println("  Salt: \"salt\"");
        System.out.println("  Iterations: 1");
        System.out.println("  KeyLength: 768 bits");
        System.out.println("  Output (b64url): " + hash4);
    }

    private static void generateSeedGenerationVectors() throws Exception {
        System.out.println("\n--- Seed Generation Test Vectors ---");

        // Use first 25 dictionary words for reproducible test
        List<String> testWords = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            testWords.add(SeedGenerator.words[i]);
        }

        System.out.println("\nMNEMONIC-001:");
        System.out.println("  Words: " + testWords);

        // Verify the mnemonic is NOT valid (checksums won't match)
        boolean isValid = SeedGenerator.verifySeedWords(testWords);
        System.out.println("  Is valid mnemonic: " + isValid);

        // Generate seed anyway to show the algorithm
        String seed = SeedGenerator.generateSeedPWH(testWords);
        System.out.println("  Generated seed: " + seed);

        // Now generate a VALID mnemonic and show its seed
        System.out.println("\n--- Generating VALID mnemonic for testing ---");
        List<String> validWords = SeedGenerator.generateWords();
        System.out.println("\nMNEMONIC-002 (Valid):");
        System.out.println("  Words: " + validWords);
        System.out.println("  Is valid: " + SeedGenerator.verifySeedWords(validWords));
        String validSeed = SeedGenerator.generateSeedPWH(validWords);
        System.out.println("  Generated seed: " + validSeed);

        // Show intermediate values for debugging
        System.out.println("\n--- Intermediate Values for MNEMONIC-001 ---");
        showIntermediateSeedGeneration(testWords);
    }

    private static void showIntermediateSeedGeneration(List<String> words) throws Exception {
        int saltIndex = 0;
        for (int i = 0; i < SeedGenerator.words.length; i++) {
            if (words.get(0).equals(SeedGenerator.words[i])) {
                saltIndex = i;
                break;
            }
        }

        System.out.println("Initial salt (index of first word '" + words.get(0) + "'): " + saltIndex);

        String salt = String.valueOf(saltIndex);
        String tempWord = "";

        // Only show first 2 words to keep output manageable
        for (int w = 0; w < Math.min(2, words.size()); w++) {
            String word = words.get(w);
            System.out.println("\nProcessing word " + w + ": \"" + word + "\"");
            System.out.println("  Current salt: \"" + salt.substring(0, Math.min(50, salt.length())) + "...\"");

            List<String> hashTable = new ArrayList<>();
            tempWord = word;

            // Show first 3 and last iteration
            for (int i = 0; i < FixedParameters.WALLET_DICTIONARY_LENGTH; i++) {
                tempWord = TkmSignUtils.PWHashB64(tempWord, salt, 1, 768);
                hashTable.add(tempWord);

                if (i < 3 || i == FixedParameters.WALLET_DICTIONARY_LENGTH - 1) {
                    System.out.println("    hashTable[" + i + "]: " + tempWord.substring(0, Math.min(30, tempWord.length())) + "...");
                } else if (i == 3) {
                    System.out.println("    ... (iterations 3-2046 omitted) ...");
                }
            }

            int modIndex = new BigInteger(salt.getBytes()).abs().mod(new BigInteger("2048")).intValue();
            System.out.println("  modIndex (BigInt(salt.getBytes()).abs().mod(2048)): " + modIndex);
            System.out.println("  hashTable[modIndex]: " + hashTable.get(modIndex).substring(0, Math.min(30, hashTable.get(modIndex).length())) + "...");
            salt += hashTable.get(modIndex);
        }

        System.out.println("\nFinal tempWord (seed): " + tempWord);
    }

    private static void generateSeededRandomVectors() throws Exception {
        System.out.println("\n--- SeededRandom Test Vectors ---");

        // Vector 1: Simple test with known seed
        String seed1 = "test-seed-12345";
        String scope1 = "__WKCH__";
        int keyNumber1 = 1;

        SeededRandom sr1 = new SeededRandom(seed1, scope1, keyNumber1);
        byte[] bytes1 = new byte[32];
        sr1.nextBytes(bytes1);

        System.out.println("\nSR-001:");
        System.out.println("  Seed: \"" + seed1 + "\"");
        System.out.println("  Scope: \"" + scope1 + "\"");
        System.out.println("  KeyNumber: " + keyNumber1);
        System.out.println("  Length: 32 bytes");
        System.out.println("  Output (hex): " + bytesToHex(bytes1));
        System.out.println("  Output (b64url): " + TkmSignUtils.fromByteArrayToB64URL(bytes1));

        // Vector 2: Second call to same instance (tests rsaIterationsInSameInstance)
        byte[] bytes2 = new byte[32];
        sr1.nextBytes(bytes2);

        System.out.println("\nSR-002 (second call, same instance):");
        System.out.println("  Seed: \"" + seed1 + "\" + \"1\" = \"" + seed1 + "1\"");
        System.out.println("  Scope: \"" + scope1 + "\"");
        System.out.println("  KeyNumber: " + keyNumber1);
        System.out.println("  Length: 32 bytes");
        System.out.println("  Output (hex): " + bytesToHex(bytes2));
        System.out.println("  Output (b64url): " + TkmSignUtils.fromByteArrayToB64URL(bytes2));

        // Vector 3: Different key number
        SeededRandom sr3 = new SeededRandom(seed1, scope1, 2);
        byte[] bytes3 = new byte[32];
        sr3.nextBytes(bytes3);

        System.out.println("\nSR-003:");
        System.out.println("  Seed: \"" + seed1 + "\"");
        System.out.println("  Scope: \"" + scope1 + "\"");
        System.out.println("  KeyNumber: 2");
        System.out.println("  Length: 32 bytes");
        System.out.println("  Output (hex): " + bytesToHex(bytes3));
        System.out.println("  Output (b64url): " + TkmSignUtils.fromByteArrayToB64URL(bytes3));

        // Show how Strings.toByteArray and Strings.asCharArray work
        System.out.println("\n--- String Encoding Details ---");
        System.out.println("Strings.toByteArray(\"" + seed1 + "\"): " + bytesToHex(Strings.toByteArray(seed1)));
        System.out.println("Strings.toByteArray(\"" + scope1 + "\"): " + bytesToHex(Strings.toByteArray(scope1)));
    }

    private static void generateEd25519KeyVectors() throws Exception {
        System.out.println("\n--- Ed25519 Key Test Vectors ---");

        // Use SeededRandom to generate deterministic Ed25519 key
        String seed = "test-seed-12345";
        String scope = "__WKCH__";
        int keyNumber = 1;

        SeededRandom seededRandom = new SeededRandom(seed, scope, keyNumber);

        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(seededRandom));
        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();

        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();
        byte[] pubKeyBytes = pubKey.getEncoded();

        System.out.println("\nED-001:");
        System.out.println("  Seed: \"" + seed + "\"");
        System.out.println("  Scope: \"" + scope + "\"");
        System.out.println("  KeyNumber: " + keyNumber);
        System.out.println("  Public key (hex): " + bytesToHex(pubKeyBytes));
        System.out.println("  Public key (b64url): " + TkmSignUtils.fromByteArrayToB64URL(pubKeyBytes));

        // Generate key at index 2
        SeededRandom seededRandom2 = new SeededRandom(seed, scope, 2);
        Ed25519KeyPairGenerator keyGen2 = new Ed25519KeyPairGenerator();
        keyGen2.init(new Ed25519KeyGenerationParameters(seededRandom2));
        AsymmetricCipherKeyPair keyPair2 = keyGen2.generateKeyPair();

        Ed25519PublicKeyParameters pubKey2 = (Ed25519PublicKeyParameters) keyPair2.getPublic();
        byte[] pubKeyBytes2 = pubKey2.getEncoded();

        System.out.println("\nED-002:");
        System.out.println("  Seed: \"" + seed + "\"");
        System.out.println("  Scope: \"" + scope + "\"");
        System.out.println("  KeyNumber: 2");
        System.out.println("  Public key (hex): " + bytesToHex(pubKeyBytes2));
        System.out.println("  Public key (b64url): " + TkmSignUtils.fromByteArrayToB64URL(pubKeyBytes2));
    }

    private static void generateJsonOutput() throws Exception {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"version\": \"1.0.0\",\n");
        json.append("  \"generated\": \"2024-12-12\",\n");
        json.append("  \"source\": \"wallet-core (Java)\",\n");
        json.append("  \"pbkdf2_vectors\": [\n");

        // PBKDF2-001
        byte[] hash1 = TkmSignUtils.PWHash("test", "salt", 1, 256);
        json.append("    {\n");
        json.append("      \"id\": \"PBKDF2-001\",\n");
        json.append("      \"password\": \"test\",\n");
        json.append("      \"salt\": \"salt\",\n");
        json.append("      \"iterations\": 1,\n");
        json.append("      \"keyLengthBits\": 256,\n");
        json.append("      \"expectedHex\": \"" + bytesToHex(hash1) + "\",\n");
        json.append("      \"expectedB64Url\": \"" + TkmSignUtils.fromByteArrayToB64URL(hash1) + "\"\n");
        json.append("    },\n");

        // PBKDF2-002
        byte[] hash2 = TkmSignUtils.PWHash("test-seed", "__WKCH__", 1, 256);
        json.append("    {\n");
        json.append("      \"id\": \"PBKDF2-002\",\n");
        json.append("      \"password\": \"test-seed\",\n");
        json.append("      \"salt\": \"__WKCH__\",\n");
        json.append("      \"iterations\": 1,\n");
        json.append("      \"keyLengthBits\": 256,\n");
        json.append("      \"expectedHex\": \"" + bytesToHex(hash2) + "\",\n");
        json.append("      \"expectedB64Url\": \"" + TkmSignUtils.fromByteArrayToB64URL(hash2) + "\"\n");
        json.append("    },\n");

        // PBKDF2-003 (PWHashB64)
        String hash3 = TkmSignUtils.PWHashB64("test", "salt", 1, 768);
        json.append("    {\n");
        json.append("      \"id\": \"PBKDF2-003\",\n");
        json.append("      \"password\": \"test\",\n");
        json.append("      \"salt\": \"salt\",\n");
        json.append("      \"iterations\": 1,\n");
        json.append("      \"keyLengthBits\": 768,\n");
        json.append("      \"expectedB64Url\": \"" + hash3 + "\"\n");
        json.append("    }\n");
        json.append("  ],\n");

        // SeededRandom vectors
        json.append("  \"seeded_random_vectors\": [\n");

        String seed1 = "test-seed-12345";
        String scope1 = "__WKCH__";

        SeededRandom sr1 = new SeededRandom(seed1, scope1, 1);
        byte[] bytes1 = new byte[32];
        sr1.nextBytes(bytes1);

        json.append("    {\n");
        json.append("      \"id\": \"SR-001\",\n");
        json.append("      \"seed\": \"" + seed1 + "\",\n");
        json.append("      \"scope\": \"" + scope1 + "\",\n");
        json.append("      \"keyNumber\": 1,\n");
        json.append("      \"byteLength\": 32,\n");
        json.append("      \"expectedHex\": \"" + bytesToHex(bytes1) + "\",\n");
        json.append("      \"expectedB64Url\": \"" + TkmSignUtils.fromByteArrayToB64URL(bytes1) + "\"\n");
        json.append("    },\n");

        // Second call
        byte[] bytes2 = new byte[32];
        sr1.nextBytes(bytes2);

        json.append("    {\n");
        json.append("      \"id\": \"SR-002\",\n");
        json.append("      \"seed\": \"" + seed1 + "\",\n");
        json.append("      \"scope\": \"" + scope1 + "\",\n");
        json.append("      \"keyNumber\": 1,\n");
        json.append("      \"rsaIterationsInSameInstance\": 1,\n");
        json.append("      \"byteLength\": 32,\n");
        json.append("      \"expectedHex\": \"" + bytesToHex(bytes2) + "\",\n");
        json.append("      \"expectedB64Url\": \"" + TkmSignUtils.fromByteArrayToB64URL(bytes2) + "\"\n");
        json.append("    }\n");
        json.append("  ],\n");

        // Ed25519 key vectors
        json.append("  \"ed25519_key_vectors\": [\n");

        SeededRandom srEd = new SeededRandom(seed1, scope1, 1);
        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(srEd));
        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();
        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();
        byte[] pubKeyBytes = pubKey.getEncoded();

        json.append("    {\n");
        json.append("      \"id\": \"ED-001\",\n");
        json.append("      \"seed\": \"" + seed1 + "\",\n");
        json.append("      \"scope\": \"" + scope1 + "\",\n");
        json.append("      \"keyNumber\": 1,\n");
        json.append("      \"expectedPublicKeyHex\": \"" + bytesToHex(pubKeyBytes) + "\",\n");
        json.append("      \"expectedPublicKeyB64Url\": \"" + TkmSignUtils.fromByteArrayToB64URL(pubKeyBytes) + "\"\n");
        json.append("    }\n");
        json.append("  ],\n");

        // Mnemonic/seed vectors
        json.append("  \"mnemonic_seed_vectors\": [\n");

        List<String> testWords = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            testWords.add(SeedGenerator.words[i]);
        }
        String testSeed = SeedGenerator.generateSeedPWH(testWords);

        json.append("    {\n");
        json.append("      \"id\": \"MN-001\",\n");
        json.append("      \"words\": " + listToJsonArray(testWords) + ",\n");
        json.append("      \"expectedSeed\": \"" + testSeed + "\"\n");
        json.append("    }\n");
        json.append("  ]\n");

        json.append("}\n");

        System.out.println(json.toString());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String listToJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i)).append("\"");
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
