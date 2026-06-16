/*
 * Cross-Platform Test Vector Generator V2
 *
 * Generates comprehensive JSON test vectors for validating Flutter port
 * compatibility with Java wallet-core reference implementation.
 *
 * Outputs to: ../wallet-core-flutter/test/fixtures/java_wallet_vectors.json
 * Also copies to: ../tkmChat/test/fixtures/java_wallet_vectors.json
 *
 * Run with: mvn test -Dtest=CrossPlatformTestVectorGeneratorV2
 */
package io.takamaka.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.takamaka.wallet.utils.FixedParameters;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.TkmSignUtils;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced test vector generator with structured JSON output.
 *
 * Run with: mvn test -Dtest=CrossPlatformTestVectorGeneratorV2
 */
public class CrossPlatformTestVectorGeneratorV2 {

    // Known valid mnemonics for testing
    private static final List<String> SHELL_MNEMONIC = Arrays.asList(
        "sample", "half", "mammal", "radar", "hold", "fit", "era", "dilemma",
        "manage", "movie", "razor", "saddle", "point", "dial", "sadness", "north",
        "item", "naive", "gate", "hockey", "sample", "script", "embark", "purse", "myth"
    );
    private static final int SHELL_KEY_INDEX = 4;

    private static final List<String> FLUTTER_MNEMONIC = Arrays.asList(
        "regret", "venture", "boring", "settle", "beach", "animal", "arm", "lyrics",
        "robot", "hold", "attack", "salon", "crazy", "mercy", "slim", "exhibit",
        "stairs", "victory", "truth", "differ", "isolate", "smile", "jazz", "aunt", "park"
    );
    private static final int FLUTTER_KEY_INDEX = 0;

    private static String shellSeed;
    private static String flutterSeed;

    private static final ObjectMapper mapper = new ObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true);

    @BeforeAll
    static void setup() throws Exception {
        SeedGenerator.init();
        assertTrue(SeedGenerator.verifySeedWords(SHELL_MNEMONIC), "Shell mnemonic must be valid");
        assertTrue(SeedGenerator.verifySeedWords(FLUTTER_MNEMONIC), "Flutter mnemonic must be valid");
        shellSeed = SeedGenerator.generateSeedPWH(SHELL_MNEMONIC);
        flutterSeed = SeedGenerator.generateSeedPWH(FLUTTER_MNEMONIC);
    }

    @Test
    void generateJsonVectors() throws Exception {
        ObjectNode root = mapper.createObjectNode();
        root.put("version", "2.0.0");
        root.put("generated_by", "CrossPlatformTestVectorGeneratorV2.java");
        root.put("generated_at", Instant.now().toString());
        root.put("source", "wallet-core (Java Reference Implementation)");

        // PBKDF2 vectors
        ArrayNode pbkdf2 = root.putArray("pbkdf2_vectors");
        addPbkdf2Vector(pbkdf2, "PBKDF2-001", "test", "salt", 1, 256);
        addPbkdf2Vector(pbkdf2, "PBKDF2-002", "test-seed", "__WKCH__", 1, 256);
        addPbkdf2Vector(pbkdf2, "PBKDF2-003", "test", "salt", 1, 768);
        addPbkdf2Vector(pbkdf2, "PBKDF2-004", "password", "TakamakaWalletWords", 1, 4096);
        addPbkdf2Vector(pbkdf2, "PBKDF2-005", "test", "salt", 20000, 256);

        // SeededRandom vectors
        ArrayNode seededRandom = root.putArray("seeded_random_vectors");
        addSeededRandomVector(seededRandom, "SR-001", "test-seed-12345", "__WKCH__", 1, 32, 0);
        addSeededRandomVector(seededRandom, "SR-002", "test-seed-12345", "__WKCH__", 1, 32, 1);

        // Ed25519 key vectors from seed
        ArrayNode ed25519Keys = root.putArray("ed25519_key_vectors");
        addEd25519KeyVector(ed25519Keys, "ED-001", "test-seed-12345", "__WKCH__", 1);

        // Ed25519 key vectors from mnemonic
        addEd25519MnemonicKeyVector(ed25519Keys, "ED-SHELL-4",
            "Shell user (pollocrypt2) at index 4",
            String.join(" ", SHELL_MNEMONIC), SHELL_KEY_INDEX, shellSeed);
        addEd25519MnemonicKeyVector(ed25519Keys, "ED-FLUTTER-0",
            "Flutter user at index 0",
            String.join(" ", FLUTTER_MNEMONIC), FLUTTER_KEY_INDEX, flutterSeed);

        // Multi-index vectors for shell mnemonic
        int[] indices = {0, 3, 4, 7, 10, 15, 20};
        for (int idx : indices) {
            addEd25519MnemonicKeyVector(ed25519Keys,
                "ED-SHELL-" + idx,
                "Shell mnemonic at index " + idx,
                String.join(" ", SHELL_MNEMONIC), idx, shellSeed);
        }

        // Ed25519 signature vectors
        ArrayNode signatures = root.putArray("ed25519_signature_vectors");
        addSignatureVector(signatures, "SIG-001", shellSeed, SHELL_KEY_INDEX,
            "{\"action\":\"test\",\"timestamp\":1706000000000}");

        // Mnemonic seed vectors
        ArrayNode mnemonics = root.putArray("mnemonic_seed_vectors");
        List<String> testWords = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            testWords.add(SeedGenerator.words[i]);
        }
        addMnemonicVector(mnemonics, "MN-001", testWords);

        // Encoding details
        ObjectNode encoding = root.putObject("encoding_details");
        encoding.put("note", "BouncyCastle UrlBase64 uses '.' as padding, Dart base64Url uses '='");

        // Write to files
        String json = mapper.writeValueAsString(root);

        String[] outputPaths = {
            "../wallet-core-flutter/test/fixtures/java_wallet_vectors.json",
            "../tkmChat/test/fixtures/java_wallet_vectors.json"
        };

        for (String path : outputPaths) {
            File file = new File(path);
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
            System.out.println("Written to: " + file.getAbsolutePath());
        }

        System.out.println("\n=== JSON output ===\n" + json);
    }

    private void addPbkdf2Vector(ArrayNode array, String id, String password, String salt,
                                  int iterations, int keyLengthBits) throws Exception {
        byte[] hash = TkmSignUtils.PWHash(password, salt, iterations, keyLengthBits);
        ObjectNode v = array.addObject();
        v.put("id", id);
        v.put("password", password);
        v.put("salt", salt);
        v.put("iterations", iterations);
        v.put("keyLengthBits", keyLengthBits);
        v.put("expectedHex", bytesToHex(hash));
        v.put("expectedB64Url", TkmSignUtils.fromByteArrayToB64URL(hash));
    }

    private void addSeededRandomVector(ArrayNode array, String id, String seed, String scope,
                                        int keyNumber, int byteLength, int iteration) throws Exception {
        SeededRandom sr = new SeededRandom(seed, scope, keyNumber);
        byte[] bytes = null;
        for (int i = 0; i <= iteration; i++) {
            bytes = new byte[byteLength];
            sr.nextBytes(bytes);
        }
        ObjectNode v = array.addObject();
        v.put("id", id);
        v.put("seed", seed);
        v.put("scope", scope);
        v.put("keyNumber", keyNumber);
        v.put("byteLength", byteLength);
        if (iteration > 0) v.put("rsaIterationsInSameInstance", iteration);
        v.put("expectedHex", bytesToHex(bytes));
        v.put("expectedB64Url", TkmSignUtils.fromByteArrayToB64URL(bytes));
    }

    private void addEd25519KeyVector(ArrayNode array, String id, String seed, String scope,
                                      int keyNumber) throws Exception {
        SeededRandom sr = new SeededRandom(seed, scope, keyNumber);
        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(sr));
        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();
        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();
        byte[] pubKeyBytes = pubKey.getEncoded();

        ObjectNode v = array.addObject();
        v.put("id", id);
        v.put("seed", seed);
        v.put("scope", scope);
        v.put("keyNumber", keyNumber);
        v.put("expectedPublicKeyHex", bytesToHex(pubKeyBytes));
        v.put("expectedPublicKeyB64Url", TkmSignUtils.fromByteArrayToB64URL(pubKeyBytes));
    }

    private void addEd25519MnemonicKeyVector(ArrayNode array, String id, String description,
                                              String mnemonic, int keyIndex, String seed) throws Exception {
        // InstanceWalletKeyStoreBCED25519 uses index + 1
        SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, keyIndex + 1);
        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(sr));
        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();
        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();

        ObjectNode v = array.addObject();
        v.put("id", id);
        v.put("description", description);
        v.put("mnemonic", mnemonic);
        v.put("keyIndex", keyIndex);
        v.put("expectedPublicKeyB64Url", TkmSignUtils.fromByteArrayToB64URL(pubKey.getEncoded()));
    }

    private void addSignatureVector(ArrayNode array, String id, String seed, int keyIndex,
                                     String message) throws Exception {
        SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, keyIndex + 1);
        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(sr));
        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();

        Ed25519PrivateKeyParameters privKey = (Ed25519PrivateKeyParameters) keyPair.getPrivate();
        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();

        byte[] messageBytes = message.getBytes("UTF-8");
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, privKey);
        signer.update(messageBytes, 0, messageBytes.length);
        byte[] signature = signer.generateSignature();

        ObjectNode v = array.addObject();
        v.put("id", id);
        v.put("message", message);
        v.put("signerPublicKeyB64Url", TkmSignUtils.fromByteArrayToB64URL(pubKey.getEncoded()));
        v.put("signatureB64Url", TkmSignUtils.fromByteArrayToB64URL(signature));
        v.put("signatureHex", bytesToHex(signature));
        v.put("signatureLength", signature.length);
    }

    private void addMnemonicVector(ArrayNode array, String id, List<String> words) throws Exception {
        String seed = SeedGenerator.generateSeedPWH(words);
        ObjectNode v = array.addObject();
        v.put("id", id);
        ArrayNode wordsArray = v.putArray("words");
        for (String w : words) wordsArray.add(w);
        v.put("expectedSeed", seed);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
