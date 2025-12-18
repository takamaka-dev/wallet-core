/*
 * DEREGISTER_MAIN and DEREGISTER_OVERFLOW Transaction Cross-Platform Test Vector Generator
 *
 * This test class generates valid and invalid DEREGISTER_MAIN and DEREGISTER_OVERFLOW
 * transactions for cross-platform validation between Java (reference) and Flutter (derived).
 *
 * Java is the REFERENCE implementation.
 * Flutter must produce identical validation results.
 */
package io.takamaka.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.takamaka.wallet.beans.InternalTransactionBean;
import io.takamaka.wallet.beans.InternalTransactionSyntaxBean;
import io.takamaka.wallet.utils.BuilderITB;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.TkmTextUtils;
import io.takamaka.wallet.utils.TransactionUtils;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.util.encoders.UrlBase64;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Generates DEREGISTER_MAIN and DEREGISTER_OVERFLOW transaction test vectors for
 * Flutter cross-platform validation.
 *
 * DEREGISTER_MAIN/DEREGISTER_OVERFLOW Validation Rules (from TransactionUtils.isInternalTransactionBeanValid):
 * - from: Required, must NOT be null or blank
 * - to: Not used (null)
 * - greenValue: Not used (null)
 * - redValue: Not used (null)
 * - message: Optional
 *
 * Invalid combinations:
 * - `from` is null
 * - `from` is empty string
 * - `from` is blank (whitespace only)
 *
 * @author Generated for cross-platform testing
 */
public class DeregisterTransactionTestVectorGenerator {

    // Use the same mnemonic as multi_index_cross_platform_test.dart for consistency
    private static final String[] TEST_WORDS = {
        "layer", "moon", "flash", "vault", "seed", "video", "cactus", "pepper",
        "exile", "game", "hollow", "dirt", "horror", "wine", "list", "stairs",
        "wrestle", "frozen", "skull", "subject", "frequent", "become", "sort",
        "kite", "genre"
    };

    // Fixed timestamp for deterministic tests (2025-01-15 12:00:00 UTC)
    private static final long FIXED_TIMESTAMP = 1736942400000L;

    private static String seed;
    private static String publicKeyUrl64;
    private static AsymmetricCipherKeyPair keyPair;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() throws Exception {
        // Initialize dictionary and generate seed from test mnemonic
        SeedGenerator.init();
        List<String> wordsList = List.of(TEST_WORDS);

        // Verify mnemonic
        boolean isValid = SeedGenerator.verifySeedWords(wordsList);
        assertTrue(isValid, "Test mnemonic should be valid");

        seed = SeedGenerator.generateSeedPWH(wordsList);

        // Generate key pair at index 0 (same as Flutter)
        keyPair = generateKeyPairAtIndex(seed, 0);
        publicKeyUrl64 = getPublicKeyUrl64(keyPair);

        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        System.out.println("=== DEREGISTER Transaction Test Vector Generator ===");
        System.out.println("Public Key (index 0): " + publicKeyUrl64);
        System.out.println();
    }

    // ========================================================================
    // DEREGISTER_MAIN VALID CASES
    // ========================================================================

    @Test
    void generateValidDeregisterMainTransactions() throws Exception {
        System.out.println("=== VALID DEREGISTER_MAIN TRANSACTIONS ===\n");

        List<Map<String, Object>> validCases = new ArrayList<>();

        // Case 1: Standard valid DEREGISTER_MAIN with message
        validCases.add(createDeregisterMainTestCase(
            "DEREG_MAIN_VALID_001",
            "Standard DEREGISTER_MAIN with message",
            publicKeyUrl64,  // from
            "Node deregistration",  // message
            true  // expected valid
        ));

        // Case 2: DEREGISTER_MAIN with empty message
        validCases.add(createDeregisterMainTestCase(
            "DEREG_MAIN_VALID_002",
            "DEREGISTER_MAIN with empty message",
            publicKeyUrl64,
            "",  // empty message
            true
        ));

        // Case 3: DEREGISTER_MAIN with null message
        validCases.add(createDeregisterMainTestCase(
            "DEREG_MAIN_VALID_003",
            "DEREGISTER_MAIN with null message",
            publicKeyUrl64,
            null,  // null message
            true
        ));

        // Case 4: DEREGISTER_MAIN with long message
        validCases.add(createDeregisterMainTestCase(
            "DEREG_MAIN_VALID_004",
            "DEREGISTER_MAIN with long message",
            publicKeyUrl64,
            "This is a longer deregistration message with additional details about why the node is being removed from the network.",
            true
        ));

        // Output
        for (Map<String, Object> testCase : validCases) {
            System.out.println(mapper.writeValueAsString(testCase));
            System.out.println();
        }
    }

    // ========================================================================
    // DEREGISTER_MAIN INVALID CASES
    // ========================================================================

    @Test
    void generateInvalidDeregisterMainTransactions() throws Exception {
        System.out.println("=== INVALID DEREGISTER_MAIN TRANSACTIONS ===\n");

        List<Map<String, Object>> invalidCases = new ArrayList<>();

        // Case 1: DEREGISTER_MAIN with null from
        invalidCases.add(createDeregisterMainTestCase(
            "DEREG_MAIN_INVALID_001",
            "DEREGISTER_MAIN with null from address",
            null,  // from is null
            "Test message",
            false
        ));

        // Case 2: DEREGISTER_MAIN with empty from
        invalidCases.add(createDeregisterMainTestCase(
            "DEREG_MAIN_INVALID_002",
            "DEREGISTER_MAIN with empty from address",
            "",  // from is empty
            "Test message",
            false
        ));

        // Case 3: DEREGISTER_MAIN with blank from (spaces)
        invalidCases.add(createDeregisterMainTestCase(
            "DEREG_MAIN_INVALID_003",
            "DEREGISTER_MAIN with blank from address (spaces only)",
            "   ",  // from is blank
            "Test message",
            false
        ));

        // Output
        for (Map<String, Object> testCase : invalidCases) {
            System.out.println(mapper.writeValueAsString(testCase));
            System.out.println();
        }
    }

    // ========================================================================
    // DEREGISTER_OVERFLOW VALID CASES
    // ========================================================================

    @Test
    void generateValidDeregisterOverflowTransactions() throws Exception {
        System.out.println("=== VALID DEREGISTER_OVERFLOW TRANSACTIONS ===\n");

        List<Map<String, Object>> validCases = new ArrayList<>();

        // Case 1: Standard valid DEREGISTER_OVERFLOW with message
        validCases.add(createDeregisterOverflowTestCase(
            "DEREG_OVERFLOW_VALID_001",
            "Standard DEREGISTER_OVERFLOW with message",
            publicKeyUrl64,  // from
            "Overflow node deregistration",  // message
            true  // expected valid
        ));

        // Case 2: DEREGISTER_OVERFLOW with empty message
        validCases.add(createDeregisterOverflowTestCase(
            "DEREG_OVERFLOW_VALID_002",
            "DEREGISTER_OVERFLOW with empty message",
            publicKeyUrl64,
            "",
            true
        ));

        // Case 3: DEREGISTER_OVERFLOW with null message
        validCases.add(createDeregisterOverflowTestCase(
            "DEREG_OVERFLOW_VALID_003",
            "DEREGISTER_OVERFLOW with null message",
            publicKeyUrl64,
            null,
            true
        ));

        // Output
        for (Map<String, Object> testCase : validCases) {
            System.out.println(mapper.writeValueAsString(testCase));
            System.out.println();
        }
    }

    // ========================================================================
    // DEREGISTER_OVERFLOW INVALID CASES
    // ========================================================================

    @Test
    void generateInvalidDeregisterOverflowTransactions() throws Exception {
        System.out.println("=== INVALID DEREGISTER_OVERFLOW TRANSACTIONS ===\n");

        List<Map<String, Object>> invalidCases = new ArrayList<>();

        // Case 1: DEREGISTER_OVERFLOW with null from
        invalidCases.add(createDeregisterOverflowTestCase(
            "DEREG_OVERFLOW_INVALID_001",
            "DEREGISTER_OVERFLOW with null from address",
            null,
            "Test message",
            false
        ));

        // Case 2: DEREGISTER_OVERFLOW with empty from
        invalidCases.add(createDeregisterOverflowTestCase(
            "DEREG_OVERFLOW_INVALID_002",
            "DEREGISTER_OVERFLOW with empty from address",
            "",
            "Test message",
            false
        ));

        // Case 3: DEREGISTER_OVERFLOW with blank from
        invalidCases.add(createDeregisterOverflowTestCase(
            "DEREG_OVERFLOW_INVALID_003",
            "DEREGISTER_OVERFLOW with blank from address (spaces only)",
            "   ",
            "Test message",
            false
        ));

        // Output
        for (Map<String, Object> testCase : invalidCases) {
            System.out.println(mapper.writeValueAsString(testCase));
            System.out.println();
        }
    }

    // ========================================================================
    // CROSS-PLATFORM SIGNATURE TESTS
    // ========================================================================

    @Test
    void outputFlutterTestVectorsDeregisterMain() throws Exception {
        System.out.println("=== DEREGISTER_MAIN TEST VECTORS FOR FLUTTER ===\n");

        // Create DEREGISTER_MAIN ITB
        InternalTransactionBean itb = BuilderITB.deregisterMain(publicKeyUrl64, "Cross-platform test", new Date(FIXED_TIMESTAMP));

        String itbJson = TkmTextUtils.toJson(itb);
        String randomSeed = "cross_platform_test_seed";
        String walletCypher = KeyContexts.WalletCypher.Ed25519BC.name();

        // Sign the transaction (message + randomSeed + walletCypher)
        String messageToSign = itbJson + randomSeed + walletCypher;
        String signature = signMessage(messageToSign, keyPair);

        Map<String, Object> allVectors = new LinkedHashMap<>();
        allVectors.put("testType", "DEREGISTER_MAIN_CROSS_PLATFORM");
        allVectors.put("timestamp", FIXED_TIMESTAMP);

        Map<String, Object> signedTx = new LinkedHashMap<>();
        signedTx.put("publicKey", publicKeyUrl64);
        signedTx.put("signature", signature);
        signedTx.put("message", itbJson);
        signedTx.put("randomSeed", randomSeed);
        signedTx.put("walletCypher", walletCypher);
        signedTx.put("transactionHash", itb.getTransactionHash());
        allVectors.put("signedTransaction", signedTx);

        String json = mapper.writeValueAsString(allVectors);
        System.out.println(json);
    }

    @Test
    void outputFlutterTestVectorsDeregisterOverflow() throws Exception {
        System.out.println("=== DEREGISTER_OVERFLOW TEST VECTORS FOR FLUTTER ===\n");

        // Create DEREGISTER_OVERFLOW ITB
        InternalTransactionBean itb = BuilderITB.deregisterOverflow(publicKeyUrl64, "Cross-platform test", new Date(FIXED_TIMESTAMP));

        String itbJson = TkmTextUtils.toJson(itb);
        String randomSeed = "cross_platform_test_seed";
        String walletCypher = KeyContexts.WalletCypher.Ed25519BC.name();

        // Sign the transaction (message + randomSeed + walletCypher)
        String messageToSign = itbJson + randomSeed + walletCypher;
        String signature = signMessage(messageToSign, keyPair);

        Map<String, Object> allVectors = new LinkedHashMap<>();
        allVectors.put("testType", "DEREGISTER_OVERFLOW_CROSS_PLATFORM");
        allVectors.put("timestamp", FIXED_TIMESTAMP);

        Map<String, Object> signedTx = new LinkedHashMap<>();
        signedTx.put("publicKey", publicKeyUrl64);
        signedTx.put("signature", signature);
        signedTx.put("message", itbJson);
        signedTx.put("randomSeed", randomSeed);
        signedTx.put("walletCypher", walletCypher);
        signedTx.put("transactionHash", itb.getTransactionHash());
        allVectors.put("signedTransaction", signedTx);

        String json = mapper.writeValueAsString(allVectors);
        System.out.println(json);
    }

    // ========================================================================
    // FLUTTER VALIDATION - TO BE UPDATED WITH FLUTTER VALUES
    // ========================================================================

    @Test
    void validateFlutterGeneratedDeregisterMain() throws Exception {
        System.out.println("=== VALIDATE FLUTTER-GENERATED DEREGISTER_MAIN TRANSACTION ===\n");

        // Flutter-generated values from deregister_transaction_cross_platform_test.dart
        String flutterPublicKey = "4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.";
        String flutterSignature = "-ysuFVAbbfcZH_JnfpjydXt4RMTjMpYZgr_EjDXHMKp2iPlaITNgV1iiG4wYZ-I5JDs-GaFgSPLmrtM2PTAZBQ..";
        String flutterMessage = "{\"from\":\"4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.\",\"to\":null,\"message\":\"Flutter-generated DEREGISTER_MAIN for Java validation\",\"notBefore\":1736942400000,\"redValue\":null,\"greenValue\":null,\"transactionType\":\"DEREGISTER_MAIN\",\"transactionHash\":\"flutter_generated_hash\",\"epoch\":null,\"slot\":null}";
        String flutterRandomSeed = "flutter_test_random_seed";
        String flutterWalletCypher = "Ed25519BC";

        System.out.println("Flutter Public Key: " + flutterPublicKey);
        System.out.println("Flutter Signature: " + flutterSignature);

        // Reconstruct the message that was signed
        String messageToVerify = flutterMessage + flutterRandomSeed + flutterWalletCypher;

        // Verify the signature using Java
        boolean isValid = verifySignature(messageToVerify, flutterSignature, flutterPublicKey);

        System.out.println("Message to verify length: " + messageToVerify.length());
        System.out.println("Signature valid: " + isValid);

        assertTrue(isValid, "Java must be able to verify Flutter-generated Ed25519 signature");

        // Also validate the InternalTransactionBean
        InternalTransactionBean parsedItb = TkmTextUtils.internalTransactionBeanFromJson(flutterMessage);
        InternalTransactionSyntaxBean validation = TransactionUtils.isInternalTransactionBeanValid(parsedItb);

        System.out.println("ITB transaction type: " + parsedItb.getTransactionType());
        System.out.println("ITB from: " + parsedItb.getFrom());
        System.out.println("ITB valid syntax: " + validation.isValidSyntax());

        assertTrue(validation.isValidSyntax(), "Flutter-generated DEREGISTER_MAIN ITB should have valid syntax");
        assertEquals(KeyContexts.TransactionType.DEREGISTER_MAIN, parsedItb.getTransactionType());

        System.out.println("\n=== FLUTTER DEREGISTER_MAIN TRANSACTION VALIDATION PASSED ===");
    }

    @Test
    void validateFlutterGeneratedDeregisterOverflow() throws Exception {
        System.out.println("=== VALIDATE FLUTTER-GENERATED DEREGISTER_OVERFLOW TRANSACTION ===\n");

        // Flutter-generated values from deregister_transaction_cross_platform_test.dart
        String flutterPublicKey = "4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.";
        String flutterSignature = "vs19nq-UXJqmtwTpI-m3RKb4TM7glwpjYesGVmeSK3EKRfBy4UE0ATbLRhphtjOE62Tx8Xv-gOTDouzNGK3DCw..";
        String flutterMessage = "{\"from\":\"4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.\",\"to\":null,\"message\":\"Flutter-generated DEREGISTER_OVERFLOW for Java validation\",\"notBefore\":1736942400000,\"redValue\":null,\"greenValue\":null,\"transactionType\":\"DEREGISTER_OVERFLOW\",\"transactionHash\":\"flutter_generated_hash\",\"epoch\":null,\"slot\":null}";
        String flutterRandomSeed = "flutter_test_random_seed";
        String flutterWalletCypher = "Ed25519BC";

        System.out.println("Flutter Public Key: " + flutterPublicKey);
        System.out.println("Flutter Signature: " + flutterSignature);

        String messageToVerify = flutterMessage + flutterRandomSeed + flutterWalletCypher;
        boolean isValid = verifySignature(messageToVerify, flutterSignature, flutterPublicKey);

        System.out.println("Message to verify length: " + messageToVerify.length());
        System.out.println("Signature valid: " + isValid);

        assertTrue(isValid, "Java must be able to verify Flutter-generated Ed25519 signature");

        InternalTransactionBean parsedItb = TkmTextUtils.internalTransactionBeanFromJson(flutterMessage);
        InternalTransactionSyntaxBean validation = TransactionUtils.isInternalTransactionBeanValid(parsedItb);

        System.out.println("ITB transaction type: " + parsedItb.getTransactionType());
        System.out.println("ITB from: " + parsedItb.getFrom());
        System.out.println("ITB valid syntax: " + validation.isValidSyntax());

        assertTrue(validation.isValidSyntax(), "Flutter-generated DEREGISTER_OVERFLOW ITB should have valid syntax");
        assertEquals(KeyContexts.TransactionType.DEREGISTER_OVERFLOW, parsedItb.getTransactionType());

        System.out.println("\n=== FLUTTER DEREGISTER_OVERFLOW TRANSACTION VALIDATION PASSED ===");
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Creates a test case for DEREGISTER_MAIN transaction.
     */
    private Map<String, Object> createDeregisterMainTestCase(String testId, String description,
            String from, String message, boolean expectedValid) {

        Map<String, Object> testCase = new LinkedHashMap<>();
        testCase.put("testId", testId);
        testCase.put("description", description);
        testCase.put("from", from);
        testCase.put("message", message);
        testCase.put("expectedValid", expectedValid);

        // Create InternalTransactionBean and validate
        InternalTransactionBean itb = new InternalTransactionBean();
        itb.setTransactionType(KeyContexts.TransactionType.DEREGISTER_MAIN);
        itb.setFrom(from);
        itb.setMessage(message);
        itb.setNotBefore(new Date(FIXED_TIMESTAMP));

        // Set transaction hash if we have valid fields
        try {
            if (from != null && !from.isBlank()) {
                itb.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(itb));
            } else {
                itb.setTransactionHash("placeholder_hash_for_invalid_tx");
            }
        } catch (Exception e) {
            itb.setTransactionHash("error_computing_hash");
        }

        // Validate
        InternalTransactionSyntaxBean validation = TransactionUtils.isInternalTransactionBeanValid(itb);
        testCase.put("actualValid", validation.isValidSyntax());

        // Include serialized ITB for Flutter to parse
        testCase.put("serializedItb", TkmTextUtils.toJson(itb));

        return testCase;
    }

    /**
     * Creates a test case for DEREGISTER_OVERFLOW transaction.
     */
    private Map<String, Object> createDeregisterOverflowTestCase(String testId, String description,
            String from, String message, boolean expectedValid) {

        Map<String, Object> testCase = new LinkedHashMap<>();
        testCase.put("testId", testId);
        testCase.put("description", description);
        testCase.put("from", from);
        testCase.put("message", message);
        testCase.put("expectedValid", expectedValid);

        // Create InternalTransactionBean and validate
        InternalTransactionBean itb = new InternalTransactionBean();
        itb.setTransactionType(KeyContexts.TransactionType.DEREGISTER_OVERFLOW);
        itb.setFrom(from);
        itb.setMessage(message);
        itb.setNotBefore(new Date(FIXED_TIMESTAMP));

        // Set transaction hash if we have valid fields
        try {
            if (from != null && !from.isBlank()) {
                itb.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(itb));
            } else {
                itb.setTransactionHash("placeholder_hash_for_invalid_tx");
            }
        } catch (Exception e) {
            itb.setTransactionHash("error_computing_hash");
        }

        // Validate
        InternalTransactionSyntaxBean validation = TransactionUtils.isInternalTransactionBeanValid(itb);
        testCase.put("actualValid", validation.isValidSyntax());

        // Include serialized ITB for Flutter to parse
        testCase.put("serializedItb", TkmTextUtils.toJson(itb));

        return testCase;
    }

    /**
     * Generate Ed25519 key pair at given index.
     */
    private static AsymmetricCipherKeyPair generateKeyPairAtIndex(String seed, int index) {
        SeededRandom seededRandom = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, index + 1);
        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(seededRandom));
        return keyGen.generateKeyPair();
    }

    /**
     * Get public key as URL-safe Base64 string.
     */
    private static String getPublicKeyUrl64(AsymmetricCipherKeyPair keyPair) throws Exception {
        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();
        byte[] pubKeyBytes = pubKey.getEncoded();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UrlBase64.encode(pubKeyBytes, baos);
        return baos.toString();
    }

    /**
     * Sign a message using Ed25519.
     */
    private static String signMessage(String message, AsymmetricCipherKeyPair keyPair) throws Exception {
        Ed25519PrivateKeyParameters privKey = (Ed25519PrivateKeyParameters) keyPair.getPrivate();
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, privKey);
        byte[] messageBytes = message.getBytes("UTF-8");
        signer.update(messageBytes, 0, messageBytes.length);
        byte[] signatureBytes = signer.generateSignature();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UrlBase64.encode(signatureBytes, baos);
        return baos.toString();
    }

    /**
     * Verify Ed25519 signature.
     */
    private static boolean verifySignature(String message, String signature, String publicKeyUrl64) {
        try {
            byte[] messageBytes = message.getBytes("UTF-8");
            byte[] signatureBytes = UrlBase64.decode(signature);
            byte[] publicKeyBytes = UrlBase64.decode(publicKeyUrl64);

            Ed25519PublicKeyParameters pubKey = new Ed25519PublicKeyParameters(publicKeyBytes, 0);
            Ed25519Signer verifier = new Ed25519Signer();
            verifier.init(false, pubKey);
            verifier.update(messageBytes, 0, messageBytes.length);
            return verifier.verifySignature(signatureBytes);
        } catch (Exception e) {
            System.err.println("Verification error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Main method for direct execution.
     */
    public static void main(String[] args) throws Exception {
        DeregisterTransactionTestVectorGenerator generator = new DeregisterTransactionTestVectorGenerator();
        setUp();

        if (args.length > 0 && args[0].equals("--validate-flutter")) {
            generator.validateFlutterGeneratedDeregisterMain();
            generator.validateFlutterGeneratedDeregisterOverflow();
        } else {
            generator.generateValidDeregisterMainTransactions();
            generator.generateInvalidDeregisterMainTransactions();
            generator.generateValidDeregisterOverflowTransactions();
            generator.generateInvalidDeregisterOverflowTransactions();
            generator.outputFlutterTestVectorsDeregisterMain();
            generator.outputFlutterTestVectorsDeregisterOverflow();
        }
    }
}
