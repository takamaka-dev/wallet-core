/*
 * BLOB Transaction Cross-Platform Test Vector Generator
 *
 * This test class generates valid and invalid BLOB transactions for
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
import io.takamaka.wallet.beans.TransactionBean;
import io.takamaka.wallet.exceptions.TransactionCanNotBeCreatedException;
import io.takamaka.wallet.utils.BuilderITB;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.TkmTextUtils;
import io.takamaka.wallet.utils.TkmWallet;
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
 * Generates BLOB transaction test vectors for Flutter cross-platform validation.
 *
 * BLOB Transaction Validation Rules (from TransactionUtils.isInternalTransactionBeanValid):
 * - from: Required, not null or blank
 * - message: Required, not null or blank
 * - transactionType: Must be BLOB
 * - notBefore: Must not be null
 * - transactionHash: Must not be null or blank
 *
 * @author Generated for cross-platform testing
 */
public class BlobTransactionTestVectorGenerator {

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

        System.out.println("=== BLOB Transaction Test Vector Generator ===");
        System.out.println("Public Key (index 0): " + publicKeyUrl64);
        System.out.println();
    }

    @Test
    void generateValidBlobTransactions() throws Exception {
        System.out.println("=== VALID BLOB TRANSACTIONS ===\n");

        List<Map<String, Object>> validCases = new ArrayList<>();

        // Case 1: Standard valid BLOB
        validCases.add(createTestCase(
            "BLOB_VALID_001",
            "Standard valid BLOB transaction",
            publicKeyUrl64,  // from
            "Hello, this is a valid blob message",  // message
            true  // expected valid
        ));

        // Case 2: BLOB with JSON message
        validCases.add(createTestCase(
            "BLOB_VALID_002",
            "BLOB with JSON message content",
            publicKeyUrl64,
            "{\"data\":\"some_value\",\"timestamp\":1234567890}",
            true
        ));

        // Case 3: BLOB with minimal message (single character)
        validCases.add(createTestCase(
            "BLOB_VALID_003",
            "BLOB with minimal message (single char)",
            publicKeyUrl64,
            "X",
            true
        ));

        // Case 4: BLOB with long message
        validCases.add(createTestCase(
            "BLOB_VALID_004",
            "BLOB with long message (1000 chars)",
            publicKeyUrl64,
            generateLongMessage(1000),
            true
        ));

        // Case 5: BLOB with special characters
        validCases.add(createTestCase(
            "BLOB_VALID_005",
            "BLOB with special characters",
            publicKeyUrl64,
            "Special chars: àéïöü ñ 中文 emoji",
            true
        ));

        // Case 6: BLOB with Base64-like content
        validCases.add(createTestCase(
            "BLOB_VALID_006",
            "BLOB with Base64 encoded content",
            publicKeyUrl64,
            "SGVsbG8gV29ybGQhIFRoaXMgaXMgYmFzZTY0IGVuY29kZWQ.",
            true
        ));

        // Output as JSON for Flutter test
        System.out.println("// Valid BLOB test vectors for Flutter");
        System.out.println("final validBlobTestVectors = ");
        System.out.println(mapper.writeValueAsString(validCases) + ";");
        System.out.println();

        // Verify all are actually valid
        for (Map<String, Object> testCase : validCases) {
            Boolean expectedValid = (Boolean) testCase.get("expectedValid");
            Boolean actualValid = (Boolean) testCase.get("actualValid");
            assertEquals(expectedValid, actualValid,
                "Test case " + testCase.get("testId") + " should be valid");
        }
    }

    @Test
    void generateInvalidBlobTransactions() throws Exception {
        System.out.println("=== INVALID BLOB TRANSACTIONS ===\n");

        List<Map<String, Object>> invalidCases = new ArrayList<>();

        // Case 1: Missing from (null)
        invalidCases.add(createTestCase(
            "BLOB_INVALID_001",
            "Missing from field (null)",
            null,  // from is null
            "Valid message but no from",
            false
        ));

        // Case 2: Empty from
        invalidCases.add(createTestCase(
            "BLOB_INVALID_002",
            "Empty from field",
            "",  // from is empty
            "Valid message but empty from",
            false
        ));

        // Case 3: Blank from (whitespace only)
        invalidCases.add(createTestCase(
            "BLOB_INVALID_003",
            "Blank from field (whitespace)",
            "   ",  // from is blank
            "Valid message but blank from",
            false
        ));

        // Case 4: Missing message (null)
        invalidCases.add(createTestCase(
            "BLOB_INVALID_004",
            "Missing message field (null)",
            publicKeyUrl64,
            null,  // message is null
            false
        ));

        // Case 5: Empty message
        invalidCases.add(createTestCase(
            "BLOB_INVALID_005",
            "Empty message field",
            publicKeyUrl64,
            "",  // message is empty
            false
        ));

        // Case 6: Blank message (whitespace only)
        invalidCases.add(createTestCase(
            "BLOB_INVALID_006",
            "Blank message field (whitespace)",
            publicKeyUrl64,
            "   ",  // message is blank (whitespace)
            false
        ));

        // Case 7: Both from and message null
        invalidCases.add(createTestCase(
            "BLOB_INVALID_007",
            "Both from and message null",
            null,
            null,
            false
        ));

        // Case 8: Both from and message empty
        invalidCases.add(createTestCase(
            "BLOB_INVALID_008",
            "Both from and message empty",
            "",
            "",
            false
        ));

        // Output as JSON for Flutter test
        System.out.println("// Invalid BLOB test vectors for Flutter");
        System.out.println("final invalidBlobTestVectors = ");
        System.out.println(mapper.writeValueAsString(invalidCases) + ";");
        System.out.println();

        // Verify all are actually invalid
        for (Map<String, Object> testCase : invalidCases) {
            Boolean expectedValid = (Boolean) testCase.get("expectedValid");
            Boolean actualValid = (Boolean) testCase.get("actualValid");
            assertEquals(expectedValid, actualValid,
                "Test case " + testCase.get("testId") + " should be invalid");
        }
    }

    @Test
    void generateSignedBlobTransaction() throws Exception {
        System.out.println("=== SIGNED BLOB TRANSACTION ===\n");

        // Create a valid BLOB InternalTransactionBean
        InternalTransactionBean itb = BuilderITB.blob(
            publicKeyUrl64,  // from
            "Signed blob message for cross-platform test",  // message
            new Date(FIXED_TIMESTAMP)  // notBefore
        );

        // Serialize the ITB to JSON
        String itbJson = TkmTextUtils.toJson(itb);

        // Create random seed for the transaction
        String randomSeed = TkmTextUtils.generateWalletRandomString();

        // Create the message to sign: message + randomSeed + walletCypher
        String messageToSign = itbJson + randomSeed + KeyContexts.WalletCypher.Ed25519BC.name();

        // Sign using our key pair
        String signature = signMessage(messageToSign, keyPair);

        // Create TransactionBean manually
        TransactionBean tb = new TransactionBean();
        tb.setPublicKey(publicKeyUrl64);
        tb.setMessage(itbJson);
        tb.setRandomSeed(randomSeed);
        tb.setSignature(signature);
        tb.setWalletCypher(KeyContexts.WalletCypher.Ed25519BC);

        // Output the complete signed transaction
        Map<String, Object> signedTx = new LinkedHashMap<>();
        signedTx.put("testId", "BLOB_SIGNED_001");
        signedTx.put("description", "Complete signed BLOB transaction from Java");
        signedTx.put("publicKey", tb.getPublicKey());
        signedTx.put("signature", tb.getSignature());
        signedTx.put("message", tb.getMessage());  // JSON of InternalTransactionBean
        signedTx.put("randomSeed", tb.getRandomSeed());
        signedTx.put("walletCypher", tb.getWalletCypher().name());

        // Parse the internal transaction for reference
        InternalTransactionBean parsedItb = TkmTextUtils.internalTransactionBeanFromJson(tb.getMessage());
        signedTx.put("internalTransaction", Map.of(
            "transactionType", parsedItb.getTransactionType().name(),
            "from", parsedItb.getFrom(),
            "message", parsedItb.getMessage(),
            "notBefore", parsedItb.getNotBefore().getTime(),
            "transactionHash", parsedItb.getTransactionHash()
        ));

        // Verify the transaction is valid
        InternalTransactionSyntaxBean validation = TransactionUtils.isInternalTransactionBeanValid(parsedItb);
        signedTx.put("isValid", validation.isValidSyntax());

        System.out.println("// Signed BLOB transaction for Flutter signature verification");
        System.out.println("final signedBlobTransaction = ");
        System.out.println(mapper.writeValueAsString(signedTx) + ";");
        System.out.println();

        assertTrue(validation.isValidSyntax(), "Signed transaction should be valid");
    }

    @Test
    void outputFlutterTestVectorsJson() throws Exception {
        System.out.println("\n=== COMPLETE FLUTTER TEST VECTORS (JSON) ===\n");

        Map<String, Object> allVectors = new LinkedHashMap<>();

        // Metadata
        allVectors.put("generatedBy", "Java wallet-core (reference implementation)");
        allVectors.put("generatedAt", new Date().toString());
        allVectors.put("testMnemonic", TEST_WORDS);
        allVectors.put("publicKey", publicKeyUrl64);
        allVectors.put("fixedTimestamp", FIXED_TIMESTAMP);

        // Valid cases
        List<Map<String, Object>> validCases = new ArrayList<>();
        validCases.add(createTestCase("BLOB_VALID_001", "Standard valid BLOB", publicKeyUrl64, "Test message", true));
        validCases.add(createTestCase("BLOB_VALID_002", "JSON content", publicKeyUrl64, "{\"key\":\"value\"}", true));
        validCases.add(createTestCase("BLOB_VALID_003", "Single char", publicKeyUrl64, "X", true));
        validCases.add(createTestCase("BLOB_VALID_004", "Unicode content", publicKeyUrl64, "Unicode: äöü 中文", true));
        allVectors.put("validCases", validCases);

        // Invalid cases
        List<Map<String, Object>> invalidCases = new ArrayList<>();
        invalidCases.add(createTestCase("BLOB_INVALID_001", "Null from", null, "Message", false));
        invalidCases.add(createTestCase("BLOB_INVALID_002", "Empty from", "", "Message", false));
        invalidCases.add(createTestCase("BLOB_INVALID_003", "Blank from", "   ", "Message", false));
        invalidCases.add(createTestCase("BLOB_INVALID_004", "Null message", publicKeyUrl64, null, false));
        invalidCases.add(createTestCase("BLOB_INVALID_005", "Empty message", publicKeyUrl64, "", false));
        invalidCases.add(createTestCase("BLOB_INVALID_006", "Blank message", publicKeyUrl64, "   ", false));
        allVectors.put("invalidCases", invalidCases);

        // Signed transaction
        InternalTransactionBean itb = BuilderITB.blob(publicKeyUrl64, "Cross-platform test blob", new Date(FIXED_TIMESTAMP));
        String itbJson = TkmTextUtils.toJson(itb);
        String randomSeed = "fixed_random_seed_for_deterministic_test";
        String messageToSign = itbJson + randomSeed + KeyContexts.WalletCypher.Ed25519BC.name();
        String signature = signMessage(messageToSign, keyPair);

        Map<String, Object> signedTx = new LinkedHashMap<>();
        signedTx.put("publicKey", publicKeyUrl64);
        signedTx.put("signature", signature);
        signedTx.put("message", itbJson);
        signedTx.put("randomSeed", randomSeed);
        signedTx.put("walletCypher", KeyContexts.WalletCypher.Ed25519BC.name());
        allVectors.put("signedTransaction", signedTx);

        String json = mapper.writeValueAsString(allVectors);
        System.out.println(json);
    }

    /**
     * Creates a test case with validation result.
     */
    private Map<String, Object> createTestCase(String testId, String description,
            String from, String message, boolean expectedValid) {

        Map<String, Object> testCase = new LinkedHashMap<>();
        testCase.put("testId", testId);
        testCase.put("description", description);
        testCase.put("from", from);
        testCase.put("message", message);
        testCase.put("expectedValid", expectedValid);

        // Create InternalTransactionBean and validate
        InternalTransactionBean itb = new InternalTransactionBean();
        itb.setTransactionType(KeyContexts.TransactionType.BLOB);
        itb.setFrom(from);
        itb.setMessage(message);
        itb.setNotBefore(new Date(FIXED_TIMESTAMP));

        // Set transaction hash if we have valid from/message
        try {
            if (from != null && !from.isBlank() && message != null && !message.isBlank()) {
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

    private String generateLongMessage(int length) {
        StringBuilder sb = new StringBuilder();
        String pattern = "ABCDEFGHIJ";
        while (sb.length() < length) {
            sb.append(pattern);
        }
        return sb.substring(0, length);
    }

    @Test
    void validateFlutterGeneratedTransaction() throws Exception {
        System.out.println("=== VALIDATE FLUTTER-GENERATED TRANSACTION ===\n");

        // Flutter-generated transaction (from blob_transaction_cross_platform_test.dart)
        // This is the transaction generated by Flutter that Java must be able to validate
        String flutterPublicKey = "4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.";
        String flutterSignature = "bEyFYsn46PmyFnFf1rHbUUio-LJ2cygwPFDdxkiJ2OeYd_G9t9zLy9lajylTKLJhFYtAfyz0qm4ECYbVScYkAw..";
        String flutterMessage = "{\"from\":\"4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.\",\"to\":null,\"message\":\"Flutter-generated BLOB for Java validation\",\"notBefore\":1736942400000,\"redValue\":null,\"greenValue\":null,\"transactionType\":\"BLOB\",\"transactionHash\":\"flutter_generated_hash\",\"epoch\":null,\"slot\":null}";
        String flutterRandomSeed = "flutter_test_random_seed";
        String flutterWalletCypher = "Ed25519BC";

        // Reconstruct the message that was signed
        String messageToVerify = flutterMessage + flutterRandomSeed + flutterWalletCypher;

        // Verify the signature using Java
        boolean isValid = verifySignature(messageToVerify, flutterSignature, flutterPublicKey);

        System.out.println("Flutter Public Key: " + flutterPublicKey);
        System.out.println("Flutter Signature: " + flutterSignature);
        System.out.println("Message to verify length: " + messageToVerify.length());
        System.out.println("Signature valid: " + isValid);

        assertTrue(isValid, "Java must be able to verify Flutter-generated Ed25519 signature");

        // Also validate the InternalTransactionBean
        InternalTransactionBean parsedItb = TkmTextUtils.internalTransactionBeanFromJson(flutterMessage);
        InternalTransactionSyntaxBean validation = TransactionUtils.isInternalTransactionBeanValid(parsedItb);

        System.out.println("ITB transaction type: " + parsedItb.getTransactionType());
        System.out.println("ITB from: " + parsedItb.getFrom());
        System.out.println("ITB message: " + parsedItb.getMessage());
        System.out.println("ITB valid syntax: " + validation.isValidSyntax());

        assertTrue(validation.isValidSyntax(), "Flutter-generated BLOB ITB should have valid syntax");
        assertEquals(KeyContexts.TransactionType.BLOB, parsedItb.getTransactionType());
        assertEquals(flutterPublicKey, parsedItb.getFrom());
        assertEquals("Flutter-generated BLOB for Java validation", parsedItb.getMessage());

        System.out.println("\n=== FLUTTER TRANSACTION VALIDATION PASSED ===");
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
     * Main method for direct execution (bypasses surefire limitations).
     */
    public static void main(String[] args) throws Exception {
        BlobTransactionTestVectorGenerator generator = new BlobTransactionTestVectorGenerator();
        setUp();

        if (args.length > 0 && args[0].equals("--validate-flutter")) {
            generator.validateFlutterGeneratedTransaction();
        } else {
            generator.outputFlutterTestVectorsJson();
        }
    }
}
