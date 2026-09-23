/*
 * F47 / F64 — cross-implementation vectors shared with wallet-core-flutter and
 * takamaka-sdk-wrap (resource f47-f64-core-vectors.json):
 *   - F47: the app's .wallet container v0.2 (SDK TkmWalletContainerV2) opens
 *     here (words and seed-only files written by the SDK), and the Java writer
 *     produces a byte-exact file for fixed salt + nonce (the same string the
 *     Dart port must produce, and that the SDK reader opens).
 *   - F64: importKeyFromWords refuses a phrase whose count, dictionary
 *     membership or checksum (words 24-25) is wrong, BEFORE deriving.
 *
 * SECURITY: test mnemonics and a test password only.
 */
package io.takamaka.wallet.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.takamaka.wallet.beans.KeyBean;
import io.takamaka.wallet.exceptions.InvalidRecoveryWordsException;
import io.takamaka.wallet.exceptions.UnlockWalletException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

public class F47F64CoreVectorsTest {

    private static JsonNode fx;
    private static String password;
    private static List<String> words;
    private static String expectedSeed;

    @BeforeAll
    static void load() throws Exception {
        try (InputStream in = F47F64CoreVectorsTest.class.getClassLoader()
                .getResourceAsStream("f47-f64-core-vectors.json")) {
            fx = new ObjectMapper().readTree(in);
        }
        password = fx.get("password").asText();
        words = list(fx.get("words"));
        expectedSeed = fx.get("expectedSeed").asText();
    }

    private static List<String> list(JsonNode arr) {
        List<String> out = new ArrayList<>();
        arr.forEach(n -> out.add(n.asText()));
        return out;
    }

    private static byte[] hex(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }
        return b;
    }

    private static Path write(Path dir, String name, String content) throws Exception {
        Path p = dir.resolve(name);
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
        return p;
    }

    private static KeyBean keyBean(String w) {
        JsonNode d = fx.get("deterministic");
        return new KeyBean(d.get("algorithm").asText(),
                KeyContexts.WalletCypher.valueOf(d.get("cypher").asText()), expectedSeed, w);
    }

    // ------------------------------------------------------------------ F64

    @Test
    void f64_theFixtureSeedIsTheJavaSeedOfTheWords() throws Exception {
        assertEquals(expectedSeed, SeedGenerator.generateSeedPWH(words),
                "the SDK-derived seed of the fixture phrase must equal Java's");
    }

    @Test
    void f64_sharedMnemonicVectors_verifySeedWords() {
        for (JsonNode v : fx.get("mnemonicVectors")) {
            assertEquals(v.get("valid").asBoolean(), SeedGenerator.verifySeedWords(list(v.get("words"))),
                    "vector " + v.get("id").asText());
        }
    }

    @Test
    void f64_importKeyFromWords_refusesEveryInvalidVector_beforeDeriving(@TempDir Path dir) {
        for (JsonNode v : fx.get("mnemonicVectors")) {
            if (v.get("valid").asBoolean()) {
                continue;
            }
            String id = v.get("id").asText();
            InvalidRecoveryWordsException ex = assertThrows(InvalidRecoveryWordsException.class,
                    () -> WalletHelper.importKeyFromWords(list(v.get("words")), dir, id + ".wallet",
                            KeyContexts.WalletCypher.Ed25519BC, "pw"), "vector " + id);
            for (String w : list(v.get("words"))) {
                assertFalse(ex.getMessage().contains(w), "the message must not echo a word (" + id + ")");
            }
            assertFalse(Files.exists(dir.resolve(id + ".wallet")), "no file for an invalid phrase (" + id + ")");
        }
    }

    @Test
    void f64_reasonsArePositionsOnly() {
        List<String> w = new ArrayList<>(words);
        w.set(4, "notaword");
        InvalidRecoveryWordsException ex = assertThrows(InvalidRecoveryWordsException.class,
                () -> SeedGenerator.requireValidSeedWords(w));
        assertTrue(ex.getMessage().contains("position 5"), ex.getMessage());
        assertThrows(InvalidRecoveryWordsException.class,
                () -> SeedGenerator.requireValidSeedWords(words.subList(0, 24)));
        assertThrows(InvalidRecoveryWordsException.class, () -> SeedGenerator.requireValidSeedWords(null));
    }

    @Test
    void f64_importKeyFromWords_acceptsTheValidPhrase(@TempDir Path dir) throws Exception {
        Path p = WalletHelper.importKeyFromWords(words, dir, "ok.wallet", KeyContexts.WalletCypher.Ed25519BC, "pw");
        assertEquals(expectedSeed, WalletHelper.readKeyFile(p, "pw").getSeed());
    }

    // ------------------------------------------------------------------ F47

    @Test
    void f47_sdkWordsFile_opensInJava(@TempDir Path dir) throws Exception {
        Path p = write(dir, "sdk-words.wallet", fx.get("sdkFiles").get("words").asText());
        KeyBean kb = WalletHelper.readKeyFile(p, password);
        assertEquals(expectedSeed, kb.getSeed());
        assertEquals(String.join(" ", words), kb.getWords());
        assertEquals(KeyContexts.WalletCypher.Ed25519BC, kb.getCypher());
        assertEquals(WalletContainerV02.INNER_VERSION_WORDS, kb.getVersion());
    }

    @Test
    void f47_sdkSeedOnlyFile_opensInJava(@TempDir Path dir) throws Exception {
        Path p = write(dir, "sdk-seed.wallet", fx.get("sdkFiles").get("seedOnly").asText());
        KeyBean kb = WalletHelper.readKeyFile(p, password);
        assertEquals(expectedSeed, kb.getSeed());
        assertEquals("", kb.getWords());
        assertEquals(WalletContainerV02.INNER_VERSION_SEED_ONLY, kb.getVersion());
        assertEquals(WalletContainerV02.FORMAT_SEED_ONLY,
                WalletContainerV02.formatOf(fx.get("sdkFiles").get("seedOnly").asText()));
    }

    @Test
    void f47_sdkFile_wrongPassword_isRejected(@TempDir Path dir) throws Exception {
        Path p = write(dir, "sdk-words.wallet", fx.get("sdkFiles").get("words").asText());
        assertThrows(UnlockWalletException.class, () -> WalletHelper.readKeyFile(p, password + "x"));
    }

    @Test
    void f47_headerTampering_failsTheTag_orTheShapeCheck() throws Exception {
        ObjectMapper m = new ObjectMapper();
        String raw = fx.get("sdkFiles").get("words").asText();
        // format flip (authenticated) -> tag failure
        ObjectNode a = (ObjectNode) m.readTree(raw);
        a.put("format", "seed-only");
        assertThrows(UnlockWalletException.class, () -> WalletContainerV02.open(m.writeValueAsString(a), password));
        // an extra outer key is part of the AAD -> tag failure
        ObjectNode b = (ObjectNode) m.readTree(raw);
        b.put("extra", "x");
        assertThrows(UnlockWalletException.class, () -> WalletContainerV02.open(m.writeValueAsString(b), password));
        // below-floor iterations -> refused before deriving
        ObjectNode c = (ObjectNode) m.readTree(raw);
        ((ObjectNode) c.get("kdf")).put("it", 1000);
        assertThrows(UnlockWalletException.class, () -> WalletContainerV02.open(m.writeValueAsString(c), password));
        // wrong kdf algorithm
        ObjectNode d = (ObjectNode) m.readTree(raw);
        ((ObjectNode) d.get("kdf")).put("alg", "PBKDF2-HMAC-SHA256");
        assertThrows(UnlockWalletException.class, () -> WalletContainerV02.open(m.writeValueAsString(d), password));
        // key reordering is NOT tampering (canonical AAD) and a stray legacy
        // "indexes" key is ignored, as in the SDK
        ObjectNode e = (ObjectNode) m.readTree(raw);
        ObjectNode reordered = m.createObjectNode();
        reordered.set("wallet", e.get("wallet"));
        reordered.set("format", e.get("format"));
        reordered.set("kdf", e.get("kdf"));
        reordered.set("algorithm", e.get("algorithm"));
        reordered.set("version", e.get("version"));
        reordered.putArray("indexes").add(0).add(4);
        assertEquals(expectedSeed, WalletContainerV02.open(m.writeValueAsString(reordered), password).getSeed());
    }

    @Test
    void f47_deterministicJavaFile_isByteExactToTheSharedVector() throws Exception {
        JsonNode d = fx.get("deterministic");
        byte[] salt = hex(d.get("saltHex").asText());
        byte[] nonce = hex(d.get("nonceHex").asText());
        int it = d.get("iterations").asInt();
        String wordsFile = WalletContainerV02.seal(keyBean(String.join(" ", words)), password, it, salt, nonce);
        String seedFile = WalletContainerV02.seal(keyBean(""), password, it, salt, nonce);
        String gen = System.getProperty("f47.gen");
        if (gen != null) {
            Files.write(Path.of(gen, "java_v02_words.wallet"), wordsFile.getBytes(StandardCharsets.UTF_8));
            Files.write(Path.of(gen, "java_v02_seedonly.wallet"), seedFile.getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(d.get("wordsFile").asText(), wordsFile);
        assertEquals(d.get("seedOnlyFile").asText(), seedFile);
        // and they open again
        assertEquals(expectedSeed, WalletContainerV02.open(wordsFile, password).getSeed());
        assertEquals("", WalletContainerV02.open(seedFile, password).getWords());
    }

    @Test
    void f47_writeKeyFileV02_roundTrip_andRefusesAnInvalidPhrase(@TempDir Path dir) throws Exception {
        Path p = WalletHelper.writeKeyFileV02(dir, "w.wallet", keyBean(String.join(" ", words)), password);
        JsonNode outer = new ObjectMapper().readTree(Files.readString(p));
        assertEquals("0.2", outer.get("version").asText());
        assertEquals("words", outer.get("format").asText());
        assertEquals(210000, outer.get("kdf").get("it").asInt());
        assertEquals(String.join(" ", words), WalletHelper.readKeyFile(p, password).getWords());

        List<String> bad = new ArrayList<>(words);
        bad.set(24, "abandon");
        assertThrows(IllegalArgumentException.class,
                () -> WalletHelper.writeKeyFileV02(dir, "bad.wallet", keyBean(String.join(" ", bad)), password));
    }

    @Test
    void f47_legacyAndDr009FilesStillOpen(@TempDir Path dir) throws Exception {
        KeyBean kb = keyBean(String.join(" ", words));
        WalletHelper.writeKeyFile(dir, "v1.wallet", kb, "pw");
        assertEquals(expectedSeed, WalletHelper.readKeyFile(dir.resolve("v1.wallet"), "pw").getSeed());
        WalletHelper.writeKeyFileV2(dir, "v2.wallet", kb, "pw", KeyContexts.WALLET_V2_MIN_ITERATIONS);
        assertEquals(expectedSeed, WalletHelper.readKeyFile(dir.resolve("v2.wallet"), "pw").getSeed());
    }
}
