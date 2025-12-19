/*
 * STAKE Transaction Cross-Platform Test Vector Generator
 *
 * This test class generates valid and invalid STAKE transactions for
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
import io.takamaka.wallet.utils.TkmTK;
import io.takamaka.wallet.utils.TransactionUtils;
import io.takamaka.wallet.utils.DefaultInitParameters;
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
 * Generates STAKE transaction test vectors for Flutter cross-platform validation.
 *
 * STAKE Validation Rules (from TransactionUtils.isInternalTransactionBeanValid):
 * - from: Required, must NOT be null or blank
 * - to: Required, must NOT be null or blank, must be valid Base64URL address (length 44 for Ed25519)
 * - greenValue: Required, must be > 0 AND >= MINIMUM_STAKE_BET_UNIT (200 TKG)
 * - redValue: Not used (null)
 * - message: Optional
 *
 * Invalid combinations:
 * - `from` is null, empty, or blank
 * - `to` is null, empty, blank, or invalid address length
 * - `greenValue` is null
 * - `greenValue` is <= 0
 * - `greenValue` is < 200 TKG (minimum stake)
 *
 * @author Generated for cross-platform testing
 */
public class StakeTransactionTestVectorGenerator {

    // Use the same mnemonic as multi_index_cross_platform_test.dart for consistency
    private static final String[] TEST_WORDS = {
        "layer", "moon", "flash", "vault", "seed", "video", "cactus", "pepper",
        "exile", "game", "hollow", "dirt", "horror", "wine", "list", "stairs",
        "wrestle", "frozen", "skull", "subject", "frequent", "become", "sort",
        "kite", "genre"
    };

    // Fixed timestamp for deterministic tests (2025-01-15 12:00:00 UTC)
    private static final long FIXED_TIMESTAMP = 1736942400000L;

    // Minimum stake: 200 TKG (200 * 10^9 nano-TKG)
    private static final BigInteger MIN_STAKE = TkmTK.unitTK(DefaultInitParameters.MINIMUM_STAKE_BET_UNIT);

    private static String seed;
    private static String publicKeyUrl64;
    private static String targetPublicKeyUrl64;  // "to" address (validator)
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

        // Generate key pair at index 0 (from address)
        keyPair = generateKeyPairAtIndex(seed, 0);
        publicKeyUrl64 = getPublicKeyUrl64(keyPair);

        // Generate key pair at index 1 (to address / validator)
        AsymmetricCipherKeyPair targetKeyPair = generateKeyPairAtIndex(seed, 1);
        targetPublicKeyUrl64 = getPublicKeyUrl64(targetKeyPair);

        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        System.out.println("=== STAKE Transaction Test Vector Generator ===");
        System.out.println("From Public Key (index 0): " + publicKeyUrl64);
        System.out.println("To Public Key (index 1 - validator): " + targetPublicKeyUrl64);
        System.out.println("Minimum Stake (200 TKG): " + MIN_STAKE);
        System.out.println();
    }

    // ========================================================================
    // STAKE VALID CASES
    // ========================================================================

    @Test
    void generateValidStakeTransactions() throws Exception {
        System.out.println("=== VALID STAKE TRANSACTIONS ===\n");

        List<Map<String, Object>> validCases = new ArrayList<>();

        // Case 1: Standard valid STAKE with exact minimum (200 TKG)
        validCases.add(createStakeTestCase(
            "STAKE_VALID_001",
            "Standard STAKE with minimum 200 TKG",
            publicKeyUrl64,  // from
            targetPublicKeyUrl64,  // to (validator)
            MIN_STAKE,  // greenValue = 200 TKG
            "Staking minimum amount",  // message
            true  // expected valid
        ));

        // Case 2: STAKE with larger amount (1000 TKG)
        validCases.add(createStakeTestCase(
            "STAKE_VALID_002",
            "STAKE with 1000 TKG",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            TkmTK.unitTK(1000),  // 1000 TKG
            "Staking larger amount",
            true
        ));

        // Case 3: STAKE with empty message
        validCases.add(createStakeTestCase(
            "STAKE_VALID_003",
            "STAKE with empty message",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            MIN_STAKE,
            "",  // empty message
            true
        ));

        // Case 4: STAKE with null message
        validCases.add(createStakeTestCase(
            "STAKE_VALID_004",
            "STAKE with null message",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            MIN_STAKE,
            null,  // null message
            true
        ));

        // Case 5: STAKE with very large amount
        validCases.add(createStakeTestCase(
            "STAKE_VALID_005",
            "STAKE with 10000 TKG",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            TkmTK.unitTK(10000),
            "Large stake amount",
            true
        ));

        // Output
        for (Map<String, Object> testCase : validCases) {
            System.out.println(mapper.writeValueAsString(testCase));
            System.out.println();
        }
    }

    // ========================================================================
    // STAKE INVALID CASES
    // ========================================================================

    @Test
    void generateInvalidStakeTransactions() throws Exception {
        System.out.println("=== INVALID STAKE TRANSACTIONS ===\n");

        List<Map<String, Object>> invalidCases = new ArrayList<>();

        // Case 1: STAKE with null from
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_001",
            "STAKE with null from address",
            null,  // from is null
            targetPublicKeyUrl64,
            MIN_STAKE,
            "Test message",
            false
        ));

        // Case 2: STAKE with empty from
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_002",
            "STAKE with empty from address",
            "",  // from is empty
            targetPublicKeyUrl64,
            MIN_STAKE,
            "Test message",
            false
        ));

        // Case 3: STAKE with blank from (spaces)
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_003",
            "STAKE with blank from address (spaces only)",
            "   ",  // from is blank
            targetPublicKeyUrl64,
            MIN_STAKE,
            "Test message",
            false
        ));

        // Case 4: STAKE with null to
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_004",
            "STAKE with null to (validator) address",
            publicKeyUrl64,
            null,  // to is null
            MIN_STAKE,
            "Test message",
            false
        ));

        // Case 5: STAKE with empty to
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_005",
            "STAKE with empty to (validator) address",
            publicKeyUrl64,
            "",  // to is empty
            MIN_STAKE,
            "Test message",
            false
        ));

        // Case 6: STAKE with blank to
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_006",
            "STAKE with blank to (validator) address",
            publicKeyUrl64,
            "   ",  // to is blank
            MIN_STAKE,
            "Test message",
            false
        ));

        // Case 7: STAKE with invalid to address length
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_007",
            "STAKE with invalid to address length",
            publicKeyUrl64,
            "invalid_short_address",  // wrong length
            MIN_STAKE,
            "Test message",
            false
        ));

        // Case 8: STAKE with null greenValue
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_008",
            "STAKE with null greenValue",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            null,  // greenValue is null
            "Test message",
            false
        ));

        // Case 9: STAKE with zero greenValue
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_009",
            "STAKE with zero greenValue",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            BigInteger.ZERO,  // greenValue = 0
            "Test message",
            false
        ));

        // Case 10: STAKE with negative greenValue
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_010",
            "STAKE with negative greenValue",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            new BigInteger("-100000000000"),  // negative
            "Test message",
            false
        ));

        // Case 11: STAKE below minimum (199 TKG)
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_011",
            "STAKE below minimum (199 TKG)",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            TkmTK.unitTK(199),  // 199 TKG < 200 TKG minimum
            "Test message",
            false
        ));

        // Case 12: STAKE with tiny amount (1 nano-TKG)
        invalidCases.add(createStakeTestCase(
            "STAKE_INVALID_012",
            "STAKE with tiny amount (1 nano-TKG)",
            publicKeyUrl64,
            targetPublicKeyUrl64,
            BigInteger.ONE,  // far below minimum
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
        System.out.println("=== STAKE TEST VECTORS FOR FLUTTER ===\n");

        // Create STAKE ITB using BuilderITB
        InternalTransactionBean itb = BuilderITB.stake(
            publicKeyUrl64,           // from
            targetPublicKeyUrl64,     // to (validator)
            MIN_STAKE,                // greenValue (200 TKG)
            "Cross-platform test",    // message
            new Date(FIXED_TIMESTAMP) // notBefore
        );

        String itbJson = TkmTextUtils.toJson(itb);
        String randomSeed = "cross_platform_test_seed";
        String walletCypher = KeyContexts.WalletCypher.Ed25519BC.name();

        // Sign the transaction (message + randomSeed + walletCypher)
        String messageToSign = itbJson + randomSeed + walletCypher;
        String signature = signMessage(messageToSign, keyPair);

        Map<String, Object> allVectors = new LinkedHashMap<>();
        allVectors.put("testType", "STAKE_CROSS_PLATFORM");
        allVectors.put("timestamp", FIXED_TIMESTAMP);
        allVectors.put("minimumStake", MIN_STAKE.toString());

        Map<String, Object> signedTx = new LinkedHashMap<>();
        signedTx.put("publicKey", publicKeyUrl64);
        signedTx.put("toAddress", targetPublicKeyUrl64);
        signedTx.put("greenValue", MIN_STAKE.toString());
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
    void validateFlutterGeneratedStake() throws Exception {
        System.out.println("=== VALIDATE FLUTTER-GENERATED STAKE TRANSACTION ===\n");

        // Flutter-generated values from stake_transaction_cross_platform_test.dart
        String flutterPublicKey = "4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.";
        String flutterSignature = "9ibFMfuGRzxup-N2_g8oE1N0fElS0UaA7QFhbUI0btubN3yZ9I9LB7HBn7HKV0J_bGObwo8T3D-2D0CIPhNQDA..";
        String flutterMessage = "{\"from\":\"4mfAa-hIJBU8_iU7IUDIgQpDZCpBVNp7oyCsMED6Y4A.\",\"to\":\"lnVYK3e51TYQPVEHQIQl1s6amHvS7__4U01p8vFSGfU.\",\"message\":\"Flutter-generated STAKE for Java validation\",\"notBefore\":1736942400000,\"redValue\":null,\"greenValue\":200000000000,\"transactionType\":\"STAKE\",\"transactionHash\":\"flutter_generated_hash\",\"epoch\":null,\"slot\":null}";
        String flutterRandomSeed = "flutter_test_random_seed";
        String flutterWalletCypher = "Ed25519BC";

        System.out.println("Flutter Public Key: " + flutterPublicKey);
        System.out.println("Flutter Signature: " + flutterSignature);

        // Skip validation if placeholder
        if (flutterSignature.equals("PLACEHOLDER_FLUTTER_SIGNATURE")) {
            System.out.println("\n*** SKIPPING VALIDATION - Flutter signature not yet provided ***");
            System.out.println("*** Update flutterSignature and flutterMessage with actual Flutter values ***");
            return;
        }

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
        System.out.println("ITB greenValue: " + parsedItb.getGreenValue());
        System.out.println("ITB valid syntax: " + validation.isValidSyntax());

        assertTrue(validation.isValidSyntax(), "Flutter-generated STAKE ITB should have valid syntax");
        assertEquals(KeyContexts.TransactionType.STAKE, parsedItb.getTransactionType());

        System.out.println("\n=== FLUTTER STAKE TRANSACTION VALIDATION PASSED ===");
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Creates a test case for STAKE transaction.
     */
    private Map<String, Object> createStakeTestCase(String testId, String description,
            String from, String to, BigInteger greenValue, String message, boolean expectedValid) {

        Map<String, Object> testCase = new LinkedHashMap<>();
        testCase.put("testId", testId);
        testCase.put("description", description);
        testCase.put("from", from);
        testCase.put("to", to);
        testCase.put("greenValue", greenValue != null ? greenValue.toString() : null);
        testCase.put("message", message);
        testCase.put("expectedValid", expectedValid);

        // Create InternalTransactionBean and validate
        InternalTransactionBean itb = new InternalTransactionBean();
        itb.setTransactionType(KeyContexts.TransactionType.STAKE);
        itb.setFrom(from);
        itb.setTo(to);
        itb.setGreenValue(greenValue);
        itb.setMessage(message);
        itb.setNotBefore(new Date(FIXED_TIMESTAMP));

        // Set transaction hash if we have valid fields
        try {
            if (from != null && !from.isBlank() && to != null && !to.isBlank() && greenValue != null) {
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
        StakeTransactionTestVectorGenerator generator = new StakeTransactionTestVectorGenerator();
        setUp();

        if (args.length > 0 && args[0].equals("--validate-flutter")) {
            generator.validateFlutterGeneratedStake();
        } else {
            generator.generateValidStakeTransactions();
            generator.generateInvalidStakeTransactions();
            generator.outputFlutterTestVectors();
        }
    }
}
