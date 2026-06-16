/*
 * DECLARATION Transaction Cross-Platform Test Vector Generator
 *
 * This test class generates valid and invalid DECLARATION transactions for
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
import io.takamaka.wallet.utils.BuilderITB;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.TkmTextUtils;
import io.takamaka.wallet.utils.TransactionUtils;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
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
 * Generates DECLARATION transaction test vectors for Flutter cross-platform validation.
 *
 * DECLARATION Transaction Validation Rules (from TransactionUtils.isInternalTransactionBeanValid):
 * - to: Required, must NOT be null or blank, must be valid Base64URL (decodable),
 *       must have length 44 (Ed25519) or 19840 (QTESLA)
 * - greenValue/redValue: At least one must be non-null
 * - Combined rule: If BOTH greenValue and redValue are present, they can't BOTH be <= 0
 *
 * Key differences from PAY validation:
 * - DECLARATION doesn't check for negative values when only one color is present
 * - DECLARATION allows green=0 with red=null (or vice versa)
 * - DECLARATION only rejects when BOTH values are present AND BOTH are <= 0
 *
 * Invalid combinations:
 * - `to` is null or blank
 * - Both greenValue and redValue are null
 * - Both greenValue and redValue are present AND both are <= 0
 * - `to` has invalid length (not 44 or 19840)
 * - `to` is not decodable Base64URL
 *
 * @author Generated for cross-platform testing
 */
public class DeclarationTransactionTestVectorGenerator {

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

        System.out.println("=== DECLARATION Transaction Test Vector Generator ===");
        System.out.println("Public Key (index 0): " + publicKeyUrl64);
        System.out.println();
    }

    @Test
    void generateValidDeclarationTransactions() throws Exception {
        System.out.println("=== VALID DECLARATION TRANSACTIONS ===\n");

        List<Map<String, Object>> validCases = new ArrayList<>();

        // Case 1: Standard valid DECLARATION with green only
        validCases.add(createTestCase(
            "DECL_VALID_001",
            "Standard DECLARATION with green value only",
            publicKeyUrl64,  // to
            BigInteger.valueOf(1000),  // greenValue (1000 nanoTK)
            null,  // redValue
            "Token declaration",  // message
            true  // expected valid
        ));

        // Case 2: DECLARATION with red only
        validCases.add(createTestCase(
            "DECL_VALID_002",
            "DECLARATION with red value only",
            publicKeyUrl64,
            null,  // greenValue
            BigInteger.valueOf(500),  // redValue
            "Red token declaration",
            true
        ));

        // Case 3: DECLARATION with both green and red
        validCases.add(createTestCase(
            "DECL_VALID_003",
            "DECLARATION with both green and red values",
            publicKeyUrl64,
            BigInteger.valueOf(1000),  // greenValue
            BigInteger.valueOf(500),   // redValue
            "Mixed declaration",
            true
        ));

        // Case 4: DECLARATION with zero green and positive red
        validCases.add(createTestCase(
            "DECL_VALID_004",
            "DECLARATION with zero green and positive red",
            publicKeyUrl64,
            BigInteger.ZERO,  // greenValue = 0
            BigInteger.valueOf(100),  // redValue > 0
            "Red declaration with zero green",
            true
        ));

        // Case 5: DECLARATION with positive green and zero red
        validCases.add(createTestCase(
            "DECL_VALID_005",
            "DECLARATION with positive green and zero red",
            publicKeyUrl64,
            BigInteger.valueOf(100),  // greenValue > 0
            BigInteger.ZERO,  // redValue = 0
            "Green declaration with zero red",
            true
        ));

        // Case 6: DECLARATION with large values
        validCases.add(createTestCase(
            "DECL_VALID_006",
            "DECLARATION with large values",
            publicKeyUrl64,
            new BigInteger("1000000000000000000"),  // 1 quintillion nanoTK
            new BigInteger("500000000000000000"),   // 500 quadrillion nanoTK
            "Large declaration",
            true
        ));

        // Case 7: DECLARATION with green=0 only (different from PAY - this is VALID for DECLARATION)
        validCases.add(createTestCase(
            "DECL_VALID_007",
            "DECLARATION with zero green only (valid for DECLARATION, unlike PAY)",
            publicKeyUrl64,
            BigInteger.ZERO,  // greenValue = 0
            null,  // redValue null
            "Zero green only declaration",
            true
        ));

        // Case 8: DECLARATION with red=0 only (different from PAY - this is VALID for DECLARATION)
        validCases.add(createTestCase(
            "DECL_VALID_008",
            "DECLARATION with zero red only (valid for DECLARATION, unlike PAY)",
            publicKeyUrl64,
            null,  // greenValue null
            BigInteger.ZERO,  // redValue = 0
            "Zero red only declaration",
            true
        ));

        // Case 9: DECLARATION with negative green only (valid! only both <= 0 is checked)
        validCases.add(createTestCase(
            "DECL_VALID_009",
            "DECLARATION with negative green only (valid - no single negative check)",
            publicKeyUrl64,
            BigInteger.valueOf(-100),  // greenValue < 0
            null,  // redValue null
            "Negative green only",
            true
        ));

        // Case 10: DECLARATION with negative red only (valid! only both <= 0 is checked)
        validCases.add(createTestCase(
            "DECL_VALID_010",
            "DECLARATION with negative red only (valid - no single negative check)",
            publicKeyUrl64,
            null,  // greenValue null
            BigInteger.valueOf(-50),  // redValue < 0
            "Negative red only",
            true
        ));

        // Case 11: DECLARATION with positive green and negative red (valid - green > 0)
        validCases.add(createTestCase(
            "DECL_VALID_011",
            "DECLARATION with positive green and negative red",
            publicKeyUrl64,
            BigInteger.valueOf(100),  // greenValue > 0
            BigInteger.valueOf(-50),  // redValue < 0
            "Mixed positive and negative",
            true
        ));

        // Case 12: DECLARATION with negative green and positive red (valid - red > 0)
        validCases.add(createTestCase(
            "DECL_VALID_012",
            "DECLARATION with negative green and positive red",
            publicKeyUrl64,
            BigInteger.valueOf(-100),  // greenValue < 0
            BigInteger.valueOf(50),    // redValue > 0
            "Negative green positive red",
            true
        ));

        // Output as JSON for Flutter test
        System.out.println("// Valid DECLARATION test vectors for Flutter");
        System.out.println("final validDeclarationTestVectors = ");
        System.out.println(mapper.writeValueAsString(validCases) + ";");
        System.out.println();

        // Verify all are actually valid
        for (Map<String, Object> testCase : validCases) {
            Boolean expectedValid = (Boolean) testCase.get("expectedValid");
            Boolean actualValid = (Boolean) testCase.get("actualValid");
            assertEquals(expectedValid, actualValid,
                "Test case " + testCase.get("testId") + " should be valid but was " + actualValid);
        }
    }

    @Test
    void generateInvalidDeclarationTransactions() throws Exception {
        System.out.println("=== INVALID DECLARATION TRANSACTIONS ===\n");

        List<Map<String, Object>> invalidCases = new ArrayList<>();

        // Case 1: Both green and red are null
        invalidCases.add(createTestCase(
            "DECL_INVALID_001",
            "Both green and red values null",
            publicKeyUrl64,
            null,  // greenValue
            null,  // redValue
            "Declaration with no values",
            false
        ));

        // Case 2: Both provided and both are zero
        invalidCases.add(createTestCase(
            "DECL_INVALID_002",
            "Both values zero",
            publicKeyUrl64,
            BigInteger.ZERO,  // greenValue = 0
            BigInteger.ZERO,  // redValue = 0
            "Both zero",
            false
        ));

        // Case 3: Both provided and both negative
        invalidCases.add(createTestCase(
            "DECL_INVALID_003",
            "Both values negative",
            publicKeyUrl64,
            BigInteger.valueOf(-100),  // greenValue < 0
            BigInteger.valueOf(-50),   // redValue < 0
            "Both negative",
            false
        ));

        // Case 4: Green=0 and Red=-1 (both present, both <= 0)
        invalidCases.add(createTestCase(
            "DECL_INVALID_004",
            "Green zero and red negative (both <= 0)",
            publicKeyUrl64,
            BigInteger.ZERO,           // greenValue = 0
            BigInteger.valueOf(-50),   // redValue < 0
            "Zero and negative",
            false
        ));

        // Case 5: Green=-1 and Red=0 (both present, both <= 0)
        invalidCases.add(createTestCase(
            "DECL_INVALID_005",
            "Green negative and red zero (both <= 0)",
            publicKeyUrl64,
            BigInteger.valueOf(-100),  // greenValue < 0
            BigInteger.ZERO,           // redValue = 0
            "Negative and zero",
            false
        ));

        // Case 6: Empty 'to' address
        invalidCases.add(createTestCase(
            "DECL_INVALID_006",
            "Empty to address",
            "",  // empty to
            BigInteger.valueOf(1000),
            null,
            "Empty recipient",
            false
        ));

        // Case 7: Null 'to' address
        invalidCases.add(createTestCase(
            "DECL_INVALID_007",
            "Null to address",
            null,  // null to
            BigInteger.valueOf(1000),
            null,
            "Null recipient",
            false
        ));

        // Case 8: Invalid 'to' address (wrong length - 45 chars instead of 44)
        invalidCases.add(createTestCase(
            "DECL_INVALID_008",
            "Invalid to address length (45 chars)",
            publicKeyUrl64 + "X",  // 45 characters
            BigInteger.valueOf(1000),
            null,
            "Invalid length recipient",
            false
        ));

        // Case 9: Invalid 'to' address (too short - 43 chars)
        invalidCases.add(createTestCase(
            "DECL_INVALID_009",
            "Invalid to address length (43 chars)",
            publicKeyUrl64.substring(0, 43),  // 43 characters
            BigInteger.valueOf(1000),
            null,
            "Too short recipient",
            false
        ));

        // Case 10: Invalid 'to' address (not valid Base64URL characters)
        invalidCases.add(createTestCase(
            "DECL_INVALID_010",
            "Invalid to address (non-Base64URL chars)",
            "not-valid-base64-address!!!@#$%^&*()_+=<>?",  // 44 chars but invalid
            BigInteger.valueOf(1000),
            null,
            "Invalid encoding",
            false
        ));

        // Output as JSON for Flutter test
        System.out.println("// Invalid DECLARATION test vectors for Flutter");
        System.out.println("final invalidDeclarationTestVectors = ");
        System.out.println(mapper.writeValueAsString(invalidCases) + ";");
        System.out.println();

        // Verify all are actually invalid
        for (Map<String, Object> testCase : invalidCases) {
            Boolean expectedValid = (Boolean) testCase.get("expectedValid");
            Boolean actualValid = (Boolean) testCase.get("actualValid");
            assertEquals(expectedValid, actualValid,
                "Test case " + testCase.get("testId") + " should be invalid but was " + actualValid);
        }
    }

    @Test
    void generateSignedDeclarationTransaction() throws Exception {
        System.out.println("=== SIGNED DECLARATION TRANSACTION ===\n");

        // Create a valid DECLARATION InternalTransactionBean
        InternalTransactionBean itb = BuilderITB.declaration(
            publicKeyUrl64,  // to
            BigInteger.valueOf(1000000000L),  // greenValue (1 TKG in nanoTK)
            null,  // redValue
            "Cross-platform test declaration",  // message
            new Date(FIXED_TIMESTAMP)  // notBefore
        );

        // Serialize the ITB to JSON
        String itbJson = TkmTextUtils.toJson(itb);

        // Create random seed for the transaction
        String randomSeed = "fixed_random_seed_for_deterministic_test";

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
        signedTx.put("testId", "DECL_SIGNED_001");
        signedTx.put("description", "Complete signed DECLARATION transaction from Java");
        signedTx.put("publicKey", tb.getPublicKey());
        signedTx.put("signature", tb.getSignature());
        signedTx.put("message", tb.getMessage());  // JSON of InternalTransactionBean
        signedTx.put("randomSeed", tb.getRandomSeed());
        signedTx.put("walletCypher", tb.getWalletCypher().name());

        // Parse the internal transaction for reference
        InternalTransactionBean parsedItb = TkmTextUtils.internalTransactionBeanFromJson(tb.getMessage());
        Map<String, Object> itbMap = new LinkedHashMap<>();
        itbMap.put("transactionType", parsedItb.getTransactionType().name());
        itbMap.put("from", parsedItb.getFrom());
        itbMap.put("to", parsedItb.getTo());
        itbMap.put("greenValue", parsedItb.getGreenValue() != null ? parsedItb.getGreenValue().toString() : null);
        itbMap.put("redValue", parsedItb.getRedValue() != null ? parsedItb.getRedValue().toString() : null);
        itbMap.put("message", parsedItb.getMessage());
        itbMap.put("notBefore", parsedItb.getNotBefore().getTime());
        itbMap.put("transactionHash", parsedItb.getTransactionHash());
        signedTx.put("internalTransaction", itbMap);

        // Verify the transaction is valid
        InternalTransactionSyntaxBean validation = TransactionUtils.isInternalTransactionBeanValid(parsedItb);
        signedTx.put("isValid", validation.isValidSyntax());

        System.out.println("// Signed DECLARATION transaction for Flutter signature verification");
        System.out.println("final signedDeclarationTransaction = ");
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
        validCases.add(createTestCase("DECL_VALID_001", "Green only", publicKeyUrl64, BigInteger.valueOf(1000), null, "Test", true));
        validCases.add(createTestCase("DECL_VALID_002", "Red only", publicKeyUrl64, null, BigInteger.valueOf(500), "Test", true));
        validCases.add(createTestCase("DECL_VALID_003", "Both colors", publicKeyUrl64, BigInteger.valueOf(1000), BigInteger.valueOf(500), "Test", true));
        validCases.add(createTestCase("DECL_VALID_004", "Zero green, positive red", publicKeyUrl64, BigInteger.ZERO, BigInteger.valueOf(100), "Test", true));
        validCases.add(createTestCase("DECL_VALID_005", "Positive green, zero red", publicKeyUrl64, BigInteger.valueOf(100), BigInteger.ZERO, "Test", true));
        validCases.add(createTestCase("DECL_VALID_006", "Zero green only", publicKeyUrl64, BigInteger.ZERO, null, "Test", true));
        validCases.add(createTestCase("DECL_VALID_007", "Zero red only", publicKeyUrl64, null, BigInteger.ZERO, "Test", true));
        validCases.add(createTestCase("DECL_VALID_008", "Negative green only", publicKeyUrl64, BigInteger.valueOf(-100), null, "Test", true));
        validCases.add(createTestCase("DECL_VALID_009", "Negative red only", publicKeyUrl64, null, BigInteger.valueOf(-50), "Test", true));
        allVectors.put("validCases", validCases);

        // Invalid cases
        List<Map<String, Object>> invalidCases = new ArrayList<>();
        invalidCases.add(createTestCase("DECL_INVALID_001", "Both null", publicKeyUrl64, null, null, "Test", false));
        invalidCases.add(createTestCase("DECL_INVALID_002", "Both zero", publicKeyUrl64, BigInteger.ZERO, BigInteger.ZERO, "Test", false));
        invalidCases.add(createTestCase("DECL_INVALID_003", "Both negative", publicKeyUrl64, BigInteger.valueOf(-100), BigInteger.valueOf(-50), "Test", false));
        invalidCases.add(createTestCase("DECL_INVALID_004", "Zero and negative", publicKeyUrl64, BigInteger.ZERO, BigInteger.valueOf(-50), "Test", false));
        invalidCases.add(createTestCase("DECL_INVALID_005", "Empty to", "", BigInteger.valueOf(1000), null, "Test", false));
        invalidCases.add(createTestCase("DECL_INVALID_006", "Invalid to length", publicKeyUrl64 + "X", BigInteger.valueOf(1000), null, "Test", false));
        allVectors.put("invalidCases", invalidCases);

        // Signed transaction
        InternalTransactionBean itb = BuilderITB.declaration(publicKeyUrl64, BigInteger.valueOf(1000000000L), null, "Cross-platform test", new Date(FIXED_TIMESTAMP));
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
     * Creates a test case with validation result for DECLARATION transaction.
     */
    private Map<String, Object> createTestCase(String testId, String description,
            String to, BigInteger greenValue, BigInteger redValue,
            String message, boolean expectedValid) {

        Map<String, Object> testCase = new LinkedHashMap<>();
        testCase.put("testId", testId);
        testCase.put("description", description);
        testCase.put("to", to);
        testCase.put("greenValue", greenValue != null ? greenValue.toString() : null);
        testCase.put("redValue", redValue != null ? redValue.toString() : null);
        testCase.put("message", message);
        testCase.put("expectedValid", expectedValid);

        // Create InternalTransactionBean and validate
        InternalTransactionBean itb = new InternalTransactionBean();
        itb.setTransactionType(KeyContexts.TransactionType.DECLARATION);
        itb.setTo(to);
        itb.setGreenValue(greenValue);
        itb.setRedValue(redValue);
        itb.setMessage(message);
        itb.setNotBefore(new Date(FIXED_TIMESTAMP));

        // Set transaction hash if we have valid fields
        try {
            if (to != null && !to.isBlank()) {
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
     * Main method for direct execution (bypasses surefire limitations).
     */
    public static void main(String[] args) throws Exception {
        DeclarationTransactionTestVectorGenerator generator = new DeclarationTransactionTestVectorGenerator();
        setUp();

        if (args.length > 0 && args[0].equals("--validate-flutter")) {
            generator.validateFlutterGeneratedTransaction();
        } else {
            generator.outputFlutterTestVectorsJson();
        }
    }

    @Test
    void validateFlutterGeneratedTransaction() throws Exception {
        System.out.println("=== VALIDATE FLUTTER-GENERATED DECLARATION TRANSACTION ===\n");

        // Flutter-generated DECLARATION transaction (from declaration_transaction_cross_platform_test.dart)
        String flutterPublicKey = "4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.";
        String flutterSignature = "SrqjNz0OT59_ZPZO2p1FAimYWoM0pkMerni8mFvpzdpXiLD1f7GGXljpJev1n80FLj2T1cUy8NK5dmPP6EAkBg..";
        String flutterMessage = "{\"from\":null,\"to\":\"4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.\",\"message\":\"Flutter-generated DECLARATION for Java validation\",\"notBefore\":1736942400000,\"redValue\":100000000,\"greenValue\":500000000,\"transactionType\":\"DECLARATION\",\"transactionHash\":\"flutter_generated_hash\",\"epoch\":null,\"slot\":null}";
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
        System.out.println("ITB to: " + parsedItb.getTo());
        System.out.println("ITB greenValue: " + parsedItb.getGreenValue());
        System.out.println("ITB redValue: " + parsedItb.getRedValue());
        System.out.println("ITB valid syntax: " + validation.isValidSyntax());

        assertTrue(validation.isValidSyntax(), "Flutter-generated DECLARATION ITB should have valid syntax");
        assertEquals(KeyContexts.TransactionType.DECLARATION, parsedItb.getTransactionType());

        System.out.println("\n=== FLUTTER DECLARATION TRANSACTION VALIDATION PASSED ===");
    }
}
