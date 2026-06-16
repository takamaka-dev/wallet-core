/*
 * STAKE_UNDO Transaction Cross-Platform Test Vector Generator
 *
 * This test class generates valid and invalid STAKE_UNDO transactions for
 * cross-platform validation between Java (reference) and Flutter (derived).
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
 * Generates STAKE_UNDO transaction test vectors for Flutter cross-platform validation.
 *
 * STAKE_UNDO Validation Rules (from TransactionUtils.isInternalTransactionBeanValid):
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
public class StakeUndoTransactionTestVectorGenerator {

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

        System.out.println("=== STAKE_UNDO Transaction Test Vector Generator ===");
        System.out.println("Public Key (index 0): " + publicKeyUrl64);
        System.out.println();
    }

    // ========================================================================
    // STAKE_UNDO VALID CASES
    // ========================================================================

    @Test
    void generateValidStakeUndoTransactions() throws Exception {
        System.out.println("=== VALID STAKE_UNDO TRANSACTIONS ===\n");

        List<Map<String, Object>> validCases = new ArrayList<>();

        // Case 1: Standard valid STAKE_UNDO with message
        validCases.add(createStakeUndoTestCase(
            "STAKE_UNDO_VALID_001",
            "Standard STAKE_UNDO with message",
            publicKeyUrl64,  // from
            "Unstaking request",  // message
            true  // expected valid
        ));

        // Case 2: STAKE_UNDO with empty message
        validCases.add(createStakeUndoTestCase(
            "STAKE_UNDO_VALID_002",
            "STAKE_UNDO with empty message",
            publicKeyUrl64,
            "",  // empty message
            true
        ));

        // Case 3: STAKE_UNDO with null message
        validCases.add(createStakeUndoTestCase(
            "STAKE_UNDO_VALID_003",
            "STAKE_UNDO with null message",
            publicKeyUrl64,
            null,  // null message
            true
        ));

        // Case 4: STAKE_UNDO with long message
        validCases.add(createStakeUndoTestCase(
            "STAKE_UNDO_VALID_004",
            "STAKE_UNDO with long message",
            publicKeyUrl64,
            "This is a longer unstaking message with additional details about why the stake is being removed from the network validator.",
            true
        ));

        // Output
        for (Map<String, Object> testCase : validCases) {
            System.out.println(mapper.writeValueAsString(testCase));
            System.out.println();
        }
    }

    // ========================================================================
    // STAKE_UNDO INVALID CASES
    // ========================================================================

    @Test
    void generateInvalidStakeUndoTransactions() throws Exception {
        System.out.println("=== INVALID STAKE_UNDO TRANSACTIONS ===\n");

        List<Map<String, Object>> invalidCases = new ArrayList<>();

        // Case 1: STAKE_UNDO with null from
        invalidCases.add(createStakeUndoTestCase(
            "STAKE_UNDO_INVALID_001",
            "STAKE_UNDO with null from address",
            null,  // from is null
            "Test message",
            false
        ));

        // Case 2: STAKE_UNDO with empty from
        invalidCases.add(createStakeUndoTestCase(
            "STAKE_UNDO_INVALID_002",
            "STAKE_UNDO with empty from address",
            "",  // from is empty
            "Test message",
            false
        ));

        // Case 3: STAKE_UNDO with blank from (spaces)
        invalidCases.add(createStakeUndoTestCase(
            "STAKE_UNDO_INVALID_003",
            "STAKE_UNDO with blank from address (spaces only)",
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
    // CROSS-PLATFORM SIGNATURE TESTS
    // ========================================================================

    @Test
    void outputFlutterTestVectors() throws Exception {
        System.out.println("=== STAKE_UNDO TEST VECTORS FOR FLUTTER ===\n");

        // Create STAKE_UNDO ITB using BuilderITB
        InternalTransactionBean itb = BuilderITB.stakeUndo(publicKeyUrl64, "Cross-platform test", new Date(FIXED_TIMESTAMP));

        String itbJson = TkmTextUtils.toJson(itb);
        String randomSeed = "cross_platform_test_seed";
        String walletCypher = KeyContexts.WalletCypher.Ed25519BC.name();

        // Sign the transaction (message + randomSeed + walletCypher)
        String messageToSign = itbJson + randomSeed + walletCypher;
        String signature = signMessage(messageToSign, keyPair);

        Map<String, Object> allVectors = new LinkedHashMap<>();
        allVectors.put("testType", "STAKE_UNDO_CROSS_PLATFORM");
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
    void validateFlutterGeneratedStakeUndo() throws Exception {
        System.out.println("=== VALIDATE FLUTTER-GENERATED STAKE_UNDO TRANSACTION ===\n");

        // Flutter-generated values from stake_undo_transaction_cross_platform_test.dart
        String flutterPublicKey = "4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.";
        String flutterSignature = "35jSzGMnznnt-a5o48oaJwOSPV7aitF9eJ5HZxFtTOXDGVN2w0-04HX6eT-pD_5iaWjRRcxS9zntIW17r0MCAg..";
        String flutterMessage = "{\"from\":\"4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.\",\"to\":null,\"message\":\"Flutter-generated STAKE_UNDO for Java validation\",\"notBefore\":1736942400000,\"redValue\":null,\"greenValue\":null,\"transactionType\":\"STAKE_UNDO\",\"transactionHash\":\"flutter_generated_hash\",\"epoch\":null,\"slot\":null}";
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

        assertTrue(validation.isValidSyntax(), "Flutter-generated STAKE_UNDO ITB should have valid syntax");
        assertEquals(KeyContexts.TransactionType.STAKE_UNDO, parsedItb.getTransactionType());

        System.out.println("\n=== FLUTTER STAKE_UNDO TRANSACTION VALIDATION PASSED ===");
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Creates a test case for STAKE_UNDO transaction.
     */
    private Map<String, Object> createStakeUndoTestCase(String testId, String description,
            String from, String message, boolean expectedValid) {

        Map<String, Object> testCase = new LinkedHashMap<>();
        testCase.put("testId", testId);
        testCase.put("description", description);
        testCase.put("from", from);
        testCase.put("message", message);
        testCase.put("expectedValid", expectedValid);

        // Create InternalTransactionBean and validate
        InternalTransactionBean itb = new InternalTransactionBean();
        itb.setTransactionType(KeyContexts.TransactionType.STAKE_UNDO);
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
        StakeUndoTransactionTestVectorGenerator generator = new StakeUndoTransactionTestVectorGenerator();
        setUp();

        if (args.length > 0 && args[0].equals("--validate-flutter")) {
            generator.validateFlutterGeneratedStakeUndo();
        } else {
            generator.generateValidStakeUndoTransactions();
            generator.generateInvalidStakeUndoTransactions();
            generator.outputFlutterTestVectors();
        }
    }
}
