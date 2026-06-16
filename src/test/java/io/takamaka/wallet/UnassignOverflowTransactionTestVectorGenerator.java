/*
 * UNASSIGN_OVERFLOW Transaction Cross-Platform Test Vector Generator
 *
 * This test class generates valid and invalid UNASSIGN_OVERFLOW transactions
 * for cross-platform validation between Java (reference) and Flutter (derived).
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
 * Generates UNASSIGN_OVERFLOW transaction test vectors for Flutter cross-platform validation.
 *
 * UNASSIGN_OVERFLOW Validation Rules (from TransactionUtils.isInternalTransactionBeanValid):
 * - from: Required, must NOT be null or blank
 * - to: Required, must NOT be null or blank
 * - to: Must have valid length (44 for ED25519, 19840 for QTESLA)
 * - to: Must be decodable from Base64URL to HEX
 * - greenValue: Not used (null)
 * - redValue: Not used (null)
 * - message: Optional
 *
 * Invalid combinations:
 * - `from` is null
 * - `from` is empty string
 * - `from` is blank (whitespace only)
 * - `to` is null
 * - `to` is empty string
 * - `to` is blank (whitespace only)
 * - `to` has invalid length (not 44 or 19840)
 * - `to` is not valid Base64URL
 *
 * @author Generated for cross-platform testing
 */
public class UnassignOverflowTransactionTestVectorGenerator {

    // Use the same mnemonic as multi_index_cross_platform_test.dart for consistency
    private static final String[] TEST_WORDS = {
        "layer", "moon", "flash", "vault", "seed", "video", "cactus", "pepper",
        "exile", "game", "hollow", "dirt", "horror", "wine", "list", "stairs",
        "wrestle", "frozen", "skull", "subject", "frequent", "become", "sort",
        "kite", "genre"
    };

    // Fixed timestamp for deterministic tests (2025-01-15 12:00:00 UTC)
    private static final long FIXED_TIMESTAMP = 1736942400000L;

    // Valid address lengths
    private static final int ED25519_ADDR_LEN = 44;
    private static final int QTESLA_ADDR_LEN = 19840;

    private static String seed;
    private static String publicKeyUrl64;
    private static String secondPublicKeyUrl64;
    private static AsymmetricCipherKeyPair keyPair;
    private static AsymmetricCipherKeyPair secondKeyPair;
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

        // Generate key pair at index 0 (for from address)
        keyPair = generateKeyPairAtIndex(seed, 0);
        publicKeyUrl64 = getPublicKeyUrl64(keyPair);

        // Generate second key pair at index 1 (for to address - the overflow address to unassign)
        secondKeyPair = generateKeyPairAtIndex(seed, 1);
        secondPublicKeyUrl64 = getPublicKeyUrl64(secondKeyPair);

        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        System.out.println("=== UNASSIGN_OVERFLOW Transaction Test Vector Generator ===");
        System.out.println("Public Key (index 0, from): " + publicKeyUrl64);
        System.out.println("Public Key (index 1, to):   " + secondPublicKeyUrl64);
        System.out.println("Expected from length: " + publicKeyUrl64.length() + " (should be " + ED25519_ADDR_LEN + ")");
        System.out.println("Expected to length: " + secondPublicKeyUrl64.length() + " (should be " + ED25519_ADDR_LEN + ")");
        System.out.println();
    }

    // ========================================================================
    // UNASSIGN_OVERFLOW VALID CASES
    // ========================================================================

    @Test
    void generateValidUnassignOverflowTransactions() throws Exception {
        System.out.println("=== VALID UNASSIGN_OVERFLOW TRANSACTIONS ===\n");

        List<Map<String, Object>> validCases = new ArrayList<>();

        // Case 1: Standard valid UNASSIGN_OVERFLOW with message
        validCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_VALID_001",
            "Standard UNASSIGN_OVERFLOW with message",
            publicKeyUrl64,      // from (main address)
            secondPublicKeyUrl64, // to (overflow address to unassign)
            "Unassigning overflow node",  // message
            true  // expected valid
        ));

        // Case 2: UNASSIGN_OVERFLOW with empty message
        validCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_VALID_002",
            "UNASSIGN_OVERFLOW with empty message",
            publicKeyUrl64,
            secondPublicKeyUrl64,
            "",  // empty message
            true
        ));

        // Case 3: UNASSIGN_OVERFLOW with null message
        validCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_VALID_003",
            "UNASSIGN_OVERFLOW with null message",
            publicKeyUrl64,
            secondPublicKeyUrl64,
            null,  // null message
            true
        ));

        // Case 4: UNASSIGN_OVERFLOW with long message
        validCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_VALID_004",
            "UNASSIGN_OVERFLOW with long message",
            publicKeyUrl64,
            secondPublicKeyUrl64,
            "This is a longer unassignment message with additional details about the overflow node removal.",
            true
        ));

        // Output
        for (Map<String, Object> testCase : validCases) {
            System.out.println(mapper.writeValueAsString(testCase));
            System.out.println();
        }
    }

    // ========================================================================
    // UNASSIGN_OVERFLOW INVALID CASES
    // ========================================================================

    @Test
    void generateInvalidUnassignOverflowTransactions() throws Exception {
        System.out.println("=== INVALID UNASSIGN_OVERFLOW TRANSACTIONS ===\n");

        List<Map<String, Object>> invalidCases = new ArrayList<>();

        // Case 1: UNASSIGN_OVERFLOW with null from
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_001",
            "UNASSIGN_OVERFLOW with null from address",
            null,  // from is null
            secondPublicKeyUrl64,
            "Test message",
            false
        ));

        // Case 2: UNASSIGN_OVERFLOW with empty from
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_002",
            "UNASSIGN_OVERFLOW with empty from address",
            "",  // from is empty
            secondPublicKeyUrl64,
            "Test message",
            false
        ));

        // Case 3: UNASSIGN_OVERFLOW with blank from (spaces)
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_003",
            "UNASSIGN_OVERFLOW with blank from address (spaces only)",
            "   ",  // from is blank
            secondPublicKeyUrl64,
            "Test message",
            false
        ));

        // Case 4: UNASSIGN_OVERFLOW with null to
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_004",
            "UNASSIGN_OVERFLOW with null to address",
            publicKeyUrl64,
            null,  // to is null
            "Test message",
            false
        ));

        // Case 5: UNASSIGN_OVERFLOW with empty to
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_005",
            "UNASSIGN_OVERFLOW with empty to address",
            publicKeyUrl64,
            "",  // to is empty
            "Test message",
            false
        ));

        // Case 6: UNASSIGN_OVERFLOW with blank to (spaces)
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_006",
            "UNASSIGN_OVERFLOW with blank to address (spaces only)",
            publicKeyUrl64,
            "   ",  // to is blank
            "Test message",
            false
        ));

        // Case 7: UNASSIGN_OVERFLOW with invalid to length (too short)
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_007",
            "UNASSIGN_OVERFLOW with invalid to length (too short)",
            publicKeyUrl64,
            "abc123",  // invalid length
            "Test message",
            false
        ));

        // Case 8: UNASSIGN_OVERFLOW with invalid to length (wrong length, not 44 or 19840)
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_008",
            "UNASSIGN_OVERFLOW with invalid to length (50 chars)",
            publicKeyUrl64,
            "12345678901234567890123456789012345678901234567890",  // 50 chars
            "Test message",
            false
        ));

        // Case 9: UNASSIGN_OVERFLOW with both null
        invalidCases.add(createUnassignOverflowTestCase(
            "UNASSIGN_OVERFLOW_INVALID_009",
            "UNASSIGN_OVERFLOW with both from and to null",
            null,
            null,
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
    void outputFlutterTestVectorsUnassignOverflow() throws Exception {
        System.out.println("=== UNASSIGN_OVERFLOW TEST VECTORS FOR FLUTTER ===\n");

        // Create UNASSIGN_OVERFLOW ITB
        InternalTransactionBean itb = BuilderITB.unassignOverflow(
            publicKeyUrl64,       // from (main address)
            secondPublicKeyUrl64, // to (overflow address to unassign)
            "Cross-platform test",
            new Date(FIXED_TIMESTAMP)
        );

        String itbJson = TkmTextUtils.toJson(itb);
        String randomSeed = "cross_platform_test_seed";
        String walletCypher = KeyContexts.WalletCypher.Ed25519BC.name();

        // Sign the transaction (message + randomSeed + walletCypher)
        String messageToSign = itbJson + randomSeed + walletCypher;
        String signature = signMessage(messageToSign, keyPair);

        Map<String, Object> allVectors = new LinkedHashMap<>();
        allVectors.put("testType", "UNASSIGN_OVERFLOW_CROSS_PLATFORM");
        allVectors.put("timestamp", FIXED_TIMESTAMP);

        Map<String, Object> signedTx = new LinkedHashMap<>();
        signedTx.put("publicKey", publicKeyUrl64);
        signedTx.put("toAddress", secondPublicKeyUrl64);
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
    void validateFlutterGeneratedUnassignOverflow() throws Exception {
        System.out.println("=== VALIDATE FLUTTER-GENERATED UNASSIGN_OVERFLOW TRANSACTION ===\n");

        // Flutter-generated values from unassign_overflow_transaction_cross_platform_test.dart
        String flutterPublicKey = "4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.";
        String flutterToAddress = "lnVYK3e51TYQPVEHQIQl1s6amHvS7__4U01p8vFSGfU.";
        String flutterSignature = "r_JJIR6AvfLrUXHoy2RAl-3QRgdzhDEAlQJus3wa0I5CDrRj0UrHO6XO8gcKZ9Vlh1w0KIPYfzRHP2PTCEV8Cg..";
        String flutterMessage = "{\"from\":\"4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.\",\"to\":\"lnVYK3e51TYQPVEHQIQl1s6amHvS7__4U01p8vFSGfU.\",\"message\":\"Flutter-generated UNASSIGN_OVERFLOW for Java validation\",\"notBefore\":1736942400000,\"redValue\":null,\"greenValue\":null,\"transactionType\":\"UNASSIGN_OVERFLOW\",\"transactionHash\":\"flutter_generated_hash\",\"epoch\":null,\"slot\":null}";
        String flutterRandomSeed = "flutter_test_random_seed";
        String flutterWalletCypher = "Ed25519BC";

        System.out.println("Flutter Public Key: " + flutterPublicKey);
        System.out.println("Flutter To Address: " + flutterToAddress);
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
        System.out.println("ITB to: " + parsedItb.getTo());
        System.out.println("ITB valid syntax: " + validation.isValidSyntax());

        assertTrue(validation.isValidSyntax(), "Flutter-generated UNASSIGN_OVERFLOW ITB should have valid syntax");
        assertEquals(KeyContexts.TransactionType.UNASSIGN_OVERFLOW, parsedItb.getTransactionType());

        System.out.println("\n=== FLUTTER UNASSIGN_OVERFLOW TRANSACTION VALIDATION PASSED ===");
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Creates a test case for UNASSIGN_OVERFLOW transaction.
     */
    private Map<String, Object> createUnassignOverflowTestCase(String testId, String description,
            String from, String to, String message, boolean expectedValid) {

        Map<String, Object> testCase = new LinkedHashMap<>();
        testCase.put("testId", testId);
        testCase.put("description", description);
        testCase.put("from", from);
        testCase.put("to", to);
        testCase.put("message", message);
        testCase.put("expectedValid", expectedValid);

        // Create InternalTransactionBean and validate
        InternalTransactionBean itb = new InternalTransactionBean();
        itb.setTransactionType(KeyContexts.TransactionType.UNASSIGN_OVERFLOW);
        itb.setFrom(from);
        itb.setTo(to);
        itb.setMessage(message);
        itb.setNotBefore(new Date(FIXED_TIMESTAMP));

        // Set transaction hash if we have valid fields
        try {
            if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
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
        UnassignOverflowTransactionTestVectorGenerator generator = new UnassignOverflowTransactionTestVectorGenerator();
        setUp();

        if (args.length > 0 && args[0].equals("--validate-flutter")) {
            generator.validateFlutterGeneratedUnassignOverflow();
        } else {
            generator.generateValidUnassignOverflowTransactions();
            generator.generateInvalidUnassignOverflowTransactions();
            generator.outputFlutterTestVectorsUnassignOverflow();
        }
    }
}
