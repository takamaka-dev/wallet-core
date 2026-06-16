/*
 * Baseline Regression Vector Generator — Phase A of the wallet-core 0.10.0
 * silent-failure-fix campaign.
 *
 * Captures deterministic cryptographic outputs (PBKDF2 seeds, public keys at
 * index, Ed25519 signatures) from the CURRENT wallet-core HEAD BEFORE the
 * audit-driven fixes are applied. The same generator runs against the post-fix
 * code in Phase C; the diff proves the fixes did not change any deterministic
 * output.
 *
 * Reference: nodeflux/docs/TASK-wallet-core-app-root-overload.md §12.6 (Phase A-E
 * comparative validation campaign).
 *
 * Outputs:
 *   src/test/resources/baseline-mnemonics.json     — 10 fresh CRC-validated
 *                                                     25-word mnemonics committed
 *                                                     once and frozen
 *   src/test/resources/baseline-vectors-pre-fix.json — pre-fix vector capture
 *   src/test/resources/baseline-vectors-post-fix.json — post-fix vector capture
 *                                                       (run with -Dvectors.mode=post-fix)
 *
 * Run modes:
 *   mvn test -Dtest=BaselineRegressionVectorGenerator#generateMnemonics
 *     - One-shot: generates baseline-mnemonics.json. Refuses to overwrite.
 *
 *   mvn test -Dtest=BaselineRegressionVectorGenerator#captureVectors
 *     - Reads baseline-mnemonics.json + emits vector capture.
 *     - Default: writes baseline-vectors-pre-fix.json
 *     - With -Dvectors.mode=post-fix: writes baseline-vectors-post-fix.json
 *
 * Phase C diff is a separate test (BaselineRegressionValidationTest, future).
 *
 * WARNING — DO NOT USE FOR PRODUCTION. The mnemonics in baseline-mnemonics.json
 * are committed to the repository and visible to every contributor. They exist
 * solely to make wallet-core's deterministic crypto-chain regression-testable.
 */
package io.takamaka.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import io.takamaka.crypto.tkmsecurityprovider.util.adaptor.r1.QTR1KeyPairGenerator;
import io.takamaka.crypto.tkmsecurityprovider.util.adaptor.r2.QTR2KeyPairGenerator;
import io.takamaka.wallet.beans.TkmCypherBean;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.TkmSignUtils;
import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaselineRegressionVectorGenerator {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    private static final String MNEMONICS_FILE = "src/test/resources/baseline-mnemonics.json";
    private static final String VECTORS_PRE_FIX = "src/test/resources/baseline-vectors-pre-fix.json";
    private static final String VECTORS_POST_FIX = "src/test/resources/baseline-vectors-post-fix.json";

    private static final int N_MNEMONICS = 10;
    private static final int[] KEY_INDICES = {0, 1, 2};

    private static final List<String> FIXED_PAYLOADS = Arrays.asList(
            "REGRESSION_PAYLOAD_001",
            "{\"action\":\"pay\",\"timestamp\":1706000000000}",
            "{\"action\":\"stake\",\"amount\":\"100000\"}",
            "DECLARATION_TX_FOR_BASELINE_REGRESSION",
            "ZERO BYTE NULLS IN PAYLOAD"
    );

    @BeforeAll
    static void setup() throws Exception {
        SeedGenerator.init();
    }

    /**
     * One-shot: generates baseline-mnemonics.json with 10 fresh CRC-validated
     * 25-word mnemonics. Refuses to overwrite. Run ONCE; commit the file.
     *
     * <p>Per operator decision 2026-05-07: fresh mnemonics generated for
     * wallet-core baseline regression testing. Committed to test resources
     * with a "DO NOT USE FOR PRODUCTION" warning. Decoupled from nodeflux's
     * LOCAL_TESTNET__FIXED (different purpose, different repo).
     */
    @Test
    @Order(1)
    void generateMnemonics() throws Exception {
        File f = new File(MNEMONICS_FILE);
        if (f.exists()) {
            System.out.println("[BaselineMnemonicGen] " + MNEMONICS_FILE
                    + " already exists; refusing to overwrite. Delete the file"
                    + " manually and re-run if regeneration is intended.");
            return;
        }

        ObjectNode root = MAPPER.createObjectNode();
        root.put("schema", "baseline-mnemonics-v1");
        root.put("generated_at", Instant.now().toString());
        root.put("warning", "DO NOT USE FOR PRODUCTION. These mnemonics are "
                + "committed to the wallet-core repository and visible to every"
                + " contributor. Test fixtures only.");
        root.put("purpose", "Phase A baseline for wallet-core 0.10.0 audit-"
                + "driven fixes (TASK-wallet-core-app-root-overload.md §12.6).");
        root.put("mnemonic_count", N_MNEMONICS);
        root.put("words_per_mnemonic", 25);
        root.put("checksum_type", "PBKDF2-HMAC-SHA512 (last 2 of 25 words)");

        ArrayNode mnemonics = root.putArray("mnemonics");

        for (int i = 0; i < N_MNEMONICS; i++) {
            List<String> words = SeedGenerator.generateWords();
            assertEquals(25, words.size(),
                    "generateWords must yield 25 words");
            assertTrue(SeedGenerator.verifySeedWords(words),
                    "generated mnemonic CRC must validate");

            String wordsConcat = String.join(" ", words);
            ObjectNode mn = mnemonics.addObject();
            mn.put("id", String.format("M%02d", i + 1));
            mn.put("words_concat", wordsConcat);
            mn.put("crc_valid", true);
            ArrayNode wordsArr = mn.putArray("words");
            for (String w : words) wordsArr.add(w);
        }

        f.getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(f)) {
            w.write(MAPPER.writeValueAsString(root));
        }
        System.out.println("[BaselineMnemonicGen] wrote " + f.getAbsolutePath()
                + " — COMMIT THIS FILE.");
    }

    /**
     * Captures the deterministic crypto-chain outputs for every mnemonic in
     * baseline-mnemonics.json. Writes either baseline-vectors-pre-fix.json
     * (default) or baseline-vectors-post-fix.json (-Dvectors.mode=post-fix).
     *
     * <p>Captured per (mnemonic, cipher, index):
     * <ul>
     *   <li>PBKDF2 seed string from SeedGenerator.generateSeedPWH</li>
     *   <li>Public key at index (URL64) — derived via SeededRandom +
     *       cipher-specific KeyPairGenerator. Same path the
     *       InstanceWalletKeyStoreBC* ctors use internally
     *       (verified by reading their initWallet bodies).</li>
     *   <li>Public key bytes length</li>
     * </ul>
     *
     * <p>For Ed25519 (deterministic signing per RFC 8032): captures byte-
     * identical signatures over each FIXED_PAYLOAD via
     * {@link TkmCypherProviderBCED25519#sign}.
     *
     * <p>For QTESLA Round 1/2 (randomized signing): captures only the
     * verify-relationship — a signature produced now must verify under the
     * captured public key. The signature BYTES will differ on every run; the
     * VALIDITY must not.
     */
    @Test
    @Order(2)
    void captureVectors() throws Exception {
        boolean postFix = "post-fix".equalsIgnoreCase(System.getProperty("vectors.mode", "pre-fix"));
        String outFile = postFix ? VECTORS_POST_FIX : VECTORS_PRE_FIX;

        File mnFile = new File(MNEMONICS_FILE);
        assertTrue(mnFile.exists(),
                MNEMONICS_FILE + " missing — run generateMnemonics first.");

        JsonNode mnRoot = MAPPER.readTree(mnFile);
        JsonNode mnArr = mnRoot.get("mnemonics");
        assertNotNull(mnArr, "mnemonics array missing in " + MNEMONICS_FILE);
        assertEquals(N_MNEMONICS, mnArr.size(),
                "mnemonic count must be " + N_MNEMONICS);

        ObjectNode root = MAPPER.createObjectNode();
        root.put("schema", "baseline-vectors-v1");
        root.put("capture_type", postFix ? "post-fix" : "pre-fix");
        root.put("generated_at", Instant.now().toString());
        root.put("purpose", "Phase " + (postFix ? "C" : "A")
                + " of wallet-core 0.10.0 audit-driven-fixes campaign.");
        root.put("source_mnemonics", MNEMONICS_FILE);
        root.put("warning", "Test fixtures only. DO NOT USE FOR PRODUCTION.");

        ArrayNode pbkdf2 = root.putArray("pbkdf2_seeds");
        ArrayNode walletDerivations = root.putArray("wallet_derivations");
        ArrayNode ed25519Sigs = root.putArray("ed25519_signatures_byte_identical");
        ArrayNode qteslaSigs = root.putArray("qtesla_signatures_semantic_equivalent");

        for (JsonNode mn : mnArr) {
            String id = mn.get("id").asText();
            List<String> words = new ArrayList<>();
            for (JsonNode w : mn.get("words")) words.add(w.asText());
            assertTrue(SeedGenerator.verifySeedWords(words),
                    "committed mnemonic " + id + " must still validate CRC");

            String seed = SeedGenerator.generateSeedPWH(words);

            ObjectNode pb = pbkdf2.addObject();
            pb.put("mnemonic_id", id);
            pb.put("seed", seed);

            for (int idx : KEY_INDICES) {
                deriveEd25519(walletDerivations, ed25519Sigs, id, seed, idx);
                deriveQtesla1(walletDerivations, qteslaSigs, id, seed, idx);
                deriveQtesla2(walletDerivations, qteslaSigs, id, seed, idx);
            }
        }

        File f = new File(outFile);
        f.getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(f)) {
            w.write(MAPPER.writeValueAsString(root));
        }
        System.out.println("[BaselineVectorGen] wrote " + f.getAbsolutePath()
                + " — capture_type=" + (postFix ? "post-fix" : "pre-fix"));
    }

    private static void deriveEd25519(ArrayNode walletDerivations,
            ArrayNode ed25519Sigs, String mnId, String seed, int keyIndex)
            throws Exception {
        SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN,
                keyIndex + 1);
        Ed25519KeyPairGenerator gen = new Ed25519KeyPairGenerator();
        gen.init(new Ed25519KeyGenerationParameters(sr));
        AsymmetricCipherKeyPair kp = gen.generateKeyPair();
        Ed25519PublicKeyParameters pub = (Ed25519PublicKeyParameters) kp.getPublic();

        ObjectNode wd = walletDerivations.addObject();
        wd.put("mnemonic_id", mnId);
        wd.put("cipher", "Ed25519BC");
        wd.put("key_index", keyIndex);
        wd.put("public_key_url64", TkmSignUtils.fromByteArrayToB64URL(pub.getEncoded()));
        wd.put("public_key_length", pub.getEncoded().length);

        for (int p = 0; p < FIXED_PAYLOADS.size(); p++) {
            String msg = FIXED_PAYLOADS.get(p);
            TkmCypherBean signed = TkmCypherProviderBCED25519.sign(kp, msg);
            assertTrue(signed.isValid(),
                    "Ed25519 sign should report valid for " + mnId + "/" + keyIndex);

            ObjectNode sv = ed25519Sigs.addObject();
            sv.put("mnemonic_id", mnId);
            sv.put("key_index", keyIndex);
            sv.put("payload_id", String.format("P%02d", p + 1));
            sv.put("payload_text", msg);
            sv.put("signature_url64", signed.getSignature());
            sv.put("signature_length_url64_chars", signed.getSignature().length());
        }
    }

    private static void deriveQtesla1(ArrayNode walletDerivations,
            ArrayNode qteslaSigs, String mnId, String seed, int keyIndex)
            throws Exception {
        SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN,
                keyIndex + 1);
        AsymmetricCipherKeyPair kp = QTR1KeyPairGenerator.getKeyPair(sr);
        byte[] pubBytes = QTR1KeyPairGenerator.getBytePublicKey(kp);

        ObjectNode wd = walletDerivations.addObject();
        wd.put("mnemonic_id", mnId);
        wd.put("cipher", "BCQTESLA_PS_1");
        wd.put("key_index", keyIndex);
        wd.put("public_key_url64", TkmSignUtils.fromByteArrayToB64URL(pubBytes));
        wd.put("public_key_length", pubBytes.length);

        for (int p = 0; p < FIXED_PAYLOADS.size(); p++) {
            String msg = FIXED_PAYLOADS.get(p);
            TkmCypherBean signed = TkmCypherProviderBCQTESLAPSSC1Round1.sign(kp, msg);
            assertTrue(signed.isValid(),
                    "QTESLA-1 sign should report valid for " + mnId + "/" + keyIndex);
            TkmCypherBean verified = TkmCypherProviderBCQTESLAPSSC1Round1.verify(
                    kp, signed.getSignature(), msg);

            ObjectNode sv = qteslaSigs.addObject();
            sv.put("mnemonic_id", mnId);
            sv.put("cipher", "BCQTESLA_PS_1");
            sv.put("key_index", keyIndex);
            sv.put("payload_id", String.format("P%02d", p + 1));
            sv.put("payload_text", msg);
            sv.put("signature_length_url64_chars_at_capture",
                    signed.getSignature().length());
            sv.put("verifies_under_own_pubkey", verified.isValid());
            sv.put("note", "QTESLA randomized; signature URL64 NOT captured "
                    + "(would differ on every run); only verify-relationship is "
                    + "the regression invariant.");
        }
    }

    private static void deriveQtesla2(ArrayNode walletDerivations,
            ArrayNode qteslaSigs, String mnId, String seed, int keyIndex)
            throws Exception {
        SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN,
                keyIndex + 1);
        AsymmetricCipherKeyPair kp = QTR2KeyPairGenerator.getKeyPair(sr);
        byte[] pubBytes = QTR2KeyPairGenerator.getBytePublicKey(kp);

        ObjectNode wd = walletDerivations.addObject();
        wd.put("mnemonic_id", mnId);
        wd.put("cipher", "BCQTESLA_PS_1_R2");
        wd.put("key_index", keyIndex);
        wd.put("public_key_url64", TkmSignUtils.fromByteArrayToB64URL(pubBytes));
        wd.put("public_key_length", pubBytes.length);

        for (int p = 0; p < FIXED_PAYLOADS.size(); p++) {
            String msg = FIXED_PAYLOADS.get(p);
            TkmCypherBean signed = TkmCypherProviderBCQTESLAPSSC1Round2.sign(kp, msg);
            assertTrue(signed.isValid(),
                    "QTESLA-2 sign should report valid for " + mnId + "/" + keyIndex);
            TkmCypherBean verified = TkmCypherProviderBCQTESLAPSSC1Round2.verify(
                    kp, signed.getSignature(), msg);

            ObjectNode sv = qteslaSigs.addObject();
            sv.put("mnemonic_id", mnId);
            sv.put("cipher", "BCQTESLA_PS_1_R2");
            sv.put("key_index", keyIndex);
            sv.put("payload_id", String.format("P%02d", p + 1));
            sv.put("payload_text", msg);
            sv.put("signature_length_url64_chars_at_capture",
                    signed.getSignature().length());
            sv.put("verifies_under_own_pubkey", verified.isValid());
            sv.put("note", "QTESLA Round 2 randomized; verify-only invariant.");
        }
    }
}
