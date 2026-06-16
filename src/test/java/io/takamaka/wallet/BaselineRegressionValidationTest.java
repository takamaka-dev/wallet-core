/*
 * Baseline Regression Validation Test — Phase E permanent harness for
 * wallet-core's deterministic cryptographic chain.
 *
 * Runs automatically as part of `mvn test`. Re-derives the cryptographic
 * outputs for every mnemonic in baseline-mnemonics.json using current
 * wallet-core code, then asserts byte-identical match against the committed
 * baseline-vectors-pre-fix.json (captured 2026-05-07 from rsaEncryption HEAD
 * before the 0.10.0 audit-driven fixes — proven via Phase C diff to also
 * match post-fix outputs across all 550 vector entries).
 *
 * <p>The baseline is the IMMUTABLE reference for wallet-core's cryptographic
 * identity going forward. Any future change that breaks this test indicates
 * either:
 * <ul>
 *   <li>An unintended cryptographic regression — must be reverted before merge</li>
 *   <li>A deliberate algorithm change — must be a major version bump with
 *       a documented migration story for on-chain backward compatibility</li>
 * </ul>
 *
 * <p>If a deliberate change requires a new baseline (rare, high-impact):
 * <ol>
 *   <li>Document the rationale in a TASK doc</li>
 *   <li>Re-run BaselineRegressionVectorGenerator#captureVectors</li>
 *   <li>Move the new vectors file to baseline-vectors-pre-fix.json</li>
 *   <li>Bump wallet-core major version</li>
 *   <li>Coordinate downstream consumer updates</li>
 * </ol>
 *
 * <p>Reference: nodeflux/docs/TASK-wallet-core-app-root-overload.md §12.6
 * Phase E (permanent cross-version regression harness).
 *
 * @since 0.10.0
 */
package io.takamaka.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.takamaka.crypto.tkmsecurityprovider.util.adaptor.r1.QTR1KeyPairGenerator;
import io.takamaka.crypto.tkmsecurityprovider.util.adaptor.r2.QTR2KeyPairGenerator;
import io.takamaka.wallet.beans.TkmCypherBean;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.TkmSignUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BaselineRegressionValidationTest {

    private static final String MNEMONICS_FILE = "src/test/resources/baseline-mnemonics.json";
    private static final String VECTORS_FILE = "src/test/resources/baseline-vectors-pre-fix.json";

    private static final List<String> FIXED_PAYLOADS = Arrays.asList(
            "REGRESSION_PAYLOAD_001",
            "{\"action\":\"pay\",\"timestamp\":1706000000000}",
            "{\"action\":\"stake\",\"amount\":\"100000\"}",
            "DECLARATION_TX_FOR_BASELINE_REGRESSION",
            "ZERO BYTE NULLS IN PAYLOAD"
    );

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    static void setup() throws Exception {
        SeedGenerator.init();
    }

    @Test
    void pbkdf2SeedsMatchBaseline() throws Exception {
        JsonNode baseline = MAPPER.readTree(new File(VECTORS_FILE));
        JsonNode mnemonics = MAPPER.readTree(new File(MNEMONICS_FILE)).get("mnemonics");

        Map<String, String> baselineSeeds = new HashMap<>();
        for (JsonNode entry : baseline.get("pbkdf2_seeds")) {
            baselineSeeds.put(entry.get("mnemonic_id").asText(), entry.get("seed").asText());
        }

        for (JsonNode mn : mnemonics) {
            String id = mn.get("id").asText();
            List<String> words = new ArrayList<>();
            for (JsonNode w : mn.get("words")) words.add(w.asText());

            String currentSeed = SeedGenerator.generateSeedPWH(words);
            assertEquals(baselineSeeds.get(id), currentSeed,
                    "PBKDF2 seed regression for mnemonic " + id
                    + " — wallet-core's cryptographic chain has changed.");
        }
    }

    @Test
    void ed25519PublicKeysMatchBaseline() throws Exception {
        JsonNode baseline = MAPPER.readTree(new File(VECTORS_FILE));
        JsonNode mnemonics = MAPPER.readTree(new File(MNEMONICS_FILE)).get("mnemonics");

        Map<String, String> baselinePks = new HashMap<>();
        for (JsonNode entry : baseline.get("wallet_derivations")) {
            if ("Ed25519BC".equals(entry.get("cipher").asText())) {
                String key = entry.get("mnemonic_id").asText() + "/" + entry.get("key_index").asInt();
                baselinePks.put(key, entry.get("public_key_url64").asText());
            }
        }

        for (JsonNode mn : mnemonics) {
            String id = mn.get("id").asText();
            List<String> words = new ArrayList<>();
            for (JsonNode w : mn.get("words")) words.add(w.asText());
            String seed = SeedGenerator.generateSeedPWH(words);

            for (int idx : new int[]{0, 1, 2}) {
                SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, idx + 1);
                Ed25519KeyPairGenerator gen = new Ed25519KeyPairGenerator();
                gen.init(new Ed25519KeyGenerationParameters(sr));
                AsymmetricCipherKeyPair kp = gen.generateKeyPair();
                Ed25519PublicKeyParameters pub = (Ed25519PublicKeyParameters) kp.getPublic();
                String currentPk = TkmSignUtils.fromByteArrayToB64URL(pub.getEncoded());

                String expected = baselinePks.get(id + "/" + idx);
                assertEquals(expected, currentPk,
                        "Ed25519 public key regression at " + id + "/" + idx
                        + " — keypair derivation chain has changed.");
            }
        }
    }

    @Test
    void qtesla1PublicKeysMatchBaseline() throws Exception {
        validateQteslaPublicKeys("BCQTESLA_PS_1");
    }

    @Test
    void qtesla2PublicKeysMatchBaseline() throws Exception {
        validateQteslaPublicKeys("BCQTESLA_PS_1_R2");
    }

    private void validateQteslaPublicKeys(String cipher) throws Exception {
        JsonNode baseline = MAPPER.readTree(new File(VECTORS_FILE));
        JsonNode mnemonics = MAPPER.readTree(new File(MNEMONICS_FILE)).get("mnemonics");

        Map<String, String> baselinePks = new HashMap<>();
        for (JsonNode entry : baseline.get("wallet_derivations")) {
            if (cipher.equals(entry.get("cipher").asText())) {
                String key = entry.get("mnemonic_id").asText() + "/" + entry.get("key_index").asInt();
                baselinePks.put(key, entry.get("public_key_url64").asText());
            }
        }

        for (JsonNode mn : mnemonics) {
            String id = mn.get("id").asText();
            List<String> words = new ArrayList<>();
            for (JsonNode w : mn.get("words")) words.add(w.asText());
            String seed = SeedGenerator.generateSeedPWH(words);

            for (int idx : new int[]{0, 1, 2}) {
                SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, idx + 1);
                AsymmetricCipherKeyPair kp;
                byte[] pubBytes;
                if ("BCQTESLA_PS_1".equals(cipher)) {
                    kp = QTR1KeyPairGenerator.getKeyPair(sr);
                    pubBytes = QTR1KeyPairGenerator.getBytePublicKey(kp);
                } else {
                    kp = QTR2KeyPairGenerator.getKeyPair(sr);
                    pubBytes = QTR2KeyPairGenerator.getBytePublicKey(kp);
                }
                String currentPk = TkmSignUtils.fromByteArrayToB64URL(pubBytes);

                String expected = baselinePks.get(id + "/" + idx);
                assertEquals(expected, currentPk,
                        cipher + " public key regression at " + id + "/" + idx
                        + " — keypair derivation chain has changed.");
            }
        }
    }

    @Test
    void ed25519SignaturesMatchBaseline() throws Exception {
        JsonNode baseline = MAPPER.readTree(new File(VECTORS_FILE));
        JsonNode mnemonics = MAPPER.readTree(new File(MNEMONICS_FILE)).get("mnemonics");

        Map<String, String> baselineSigs = new HashMap<>();
        for (JsonNode entry : baseline.get("ed25519_signatures_byte_identical")) {
            String key = entry.get("mnemonic_id").asText() + "/"
                    + entry.get("key_index").asInt() + "/"
                    + entry.get("payload_id").asText();
            baselineSigs.put(key, entry.get("signature_url64").asText());
        }

        for (JsonNode mn : mnemonics) {
            String id = mn.get("id").asText();
            List<String> words = new ArrayList<>();
            for (JsonNode w : mn.get("words")) words.add(w.asText());
            String seed = SeedGenerator.generateSeedPWH(words);

            for (int idx : new int[]{0, 1, 2}) {
                SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, idx + 1);
                Ed25519KeyPairGenerator gen = new Ed25519KeyPairGenerator();
                gen.init(new Ed25519KeyGenerationParameters(sr));
                AsymmetricCipherKeyPair kp = gen.generateKeyPair();

                for (int p = 0; p < FIXED_PAYLOADS.size(); p++) {
                    String msg = FIXED_PAYLOADS.get(p);
                    TkmCypherBean signed = TkmCypherProviderBCED25519.sign(kp, msg);
                    assertTrue(signed.isValid(),
                            "Ed25519 sign should report valid for " + id + "/" + idx);

                    String key = id + "/" + idx + "/P" + String.format("%02d", p + 1);
                    String expected = baselineSigs.get(key);
                    assertEquals(expected, signed.getSignature(),
                            "Ed25519 deterministic signature regression at " + key
                            + " — RFC 8032 signing path has changed (or upstream BC).");
                }
            }
        }
    }

    @Test
    void qtesla1SignaturesVerifyAgainstOwnPubkey() throws Exception {
        validateQteslaSignaturesVerifySemanticEquivalent("BCQTESLA_PS_1");
    }

    @Test
    void qtesla2SignaturesVerifyAgainstOwnPubkey() throws Exception {
        validateQteslaSignaturesVerifySemanticEquivalent("BCQTESLA_PS_1_R2");
    }

    /**
     * QTESLA signatures are randomized; signature bytes differ on every call.
     * The regression invariant is semantic: a signature produced now must
     * verify under the corresponding public key.
     */
    private void validateQteslaSignaturesVerifySemanticEquivalent(String cipher) throws Exception {
        JsonNode mnemonics = MAPPER.readTree(new File(MNEMONICS_FILE)).get("mnemonics");

        for (JsonNode mn : mnemonics) {
            String id = mn.get("id").asText();
            List<String> words = new ArrayList<>();
            for (JsonNode w : mn.get("words")) words.add(w.asText());
            String seed = SeedGenerator.generateSeedPWH(words);

            for (int idx : new int[]{0, 1, 2}) {
                SeededRandom sr = new SeededRandom(seed, KeyContexts.WALLET_KEY_CHAIN, idx + 1);
                AsymmetricCipherKeyPair kp;
                if ("BCQTESLA_PS_1".equals(cipher)) {
                    kp = QTR1KeyPairGenerator.getKeyPair(sr);
                } else {
                    kp = QTR2KeyPairGenerator.getKeyPair(sr);
                }

                for (String msg : FIXED_PAYLOADS) {
                    TkmCypherBean signed;
                    TkmCypherBean verified;
                    if ("BCQTESLA_PS_1".equals(cipher)) {
                        signed = TkmCypherProviderBCQTESLAPSSC1Round1.sign(kp, msg);
                        verified = TkmCypherProviderBCQTESLAPSSC1Round1.verify(kp, signed.getSignature(), msg);
                    } else {
                        signed = TkmCypherProviderBCQTESLAPSSC1Round2.sign(kp, msg);
                        verified = TkmCypherProviderBCQTESLAPSSC1Round2.verify(kp, signed.getSignature(), msg);
                    }
                    assertTrue(signed.isValid(),
                            cipher + " sign should report valid at " + id + "/" + idx);
                    assertTrue(verified.isValid(),
                            cipher + " self-verify should succeed at " + id + "/" + idx
                            + " — signature/verification path has diverged.");
                }
            }
        }
    }
}
