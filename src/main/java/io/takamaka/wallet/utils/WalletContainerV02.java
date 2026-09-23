package io.takamaka.wallet.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.takamaka.wallet.beans.KeyBean;
import io.takamaka.wallet.exceptions.UnlockWalletException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

/**
 * F47 / DR-050 — the {@code .wallet} container <b>v0.2</b> written by the
 * Takamaka mobile app (takamaka-sdk-wrap {@code TkmWalletContainerV2}, commits
 * 1ae0798 + 2bfffa0). Byte-for-byte the same construction, so a file written
 * by the app opens here and a file written here opens in the app:
 *
 * <pre>
 * {
 *   "version":   "0.2",
 *   "algorithm": "AES-256-GCM",
 *   "kdf":       {"alg": "PBKDF2-HMAC-SHA512", "it": 210000,
 *                 "salt": base64(16 random bytes), "len": 32},
 *   "format":    "words" | "seed-only",
 *   "wallet":    [base64(12-byte nonce), base64(ciphertext || 16-byte tag)]
 * }
 * </pre>
 *
 * <ul>
 * <li>KDF: standard PBKDF2-HMAC-SHA512 ({@code PBKDF2WithHmacSHA512}) over the
 * UTF-8 password and the RAW salt bytes, {@code it} iterations, 32-byte key.
 * {@code it} must lie in [{@link #MIN_ITERATIONS}, {@link #MAX_ITERATIONS}].</li>
 * <li>Cipher: AES-256-GCM, 12-byte nonce, 128-bit tag appended to the
 * ciphertext.</li>
 * <li>AAD: UTF-8 of the RFC 8785 canonical JSON (keys sorted, no whitespace)
 * of the outer object minus {@code wallet} and minus a stray legacy
 * {@code indexes} key.</li>
 * <li>Plaintext: the compact JSON of the KeyBean
 * {@code {"version","algorithm","cypher","seed","words"}}. {@code format:
 * words} ⇔ inner version {@code 0.1}; {@code format: seed-only} ⇔ inner
 * version {@code 0.2} with empty {@code words}.</li>
 * <li>Base64: the STANDARD alphabet with padding.</li>
 * </ul>
 *
 * <p>This is NOT the DR-009 {@code AES_GCM_V2} container of
 * {@link WalletHelper#writeKeyFileV2}: that one is Java/wallet-core-flutter
 * only (flat header, binary AAD, 1 000 000 iterations) and the app cannot
 * read it. v0.2 is the only container that both the app and wallet-core
 * read.
 */
@Slf4j
public final class WalletContainerV02 {

    public static final String VERSION = "0.2";
    public static final String ALGORITHM = "AES-256-GCM";
    public static final String KDF_ALGORITHM = "PBKDF2-HMAC-SHA512";
    /** FROZEN (DR-038 / DR-050): never lowered for speed. */
    public static final int ITERATIONS = 210_000;
    public static final int MIN_ITERATIONS = ITERATIONS;
    /** A hostile file cannot pin a CPU for hours before failing. */
    public static final int MAX_ITERATIONS = 10_000_000;
    public static final int SALT_LENGTH = 16;
    public static final int KEY_LENGTH = 32;
    public static final int NONCE_LENGTH = 12;
    public static final int TAG_LENGTH = 16;
    public static final String FORMAT_WORDS = "words";
    public static final String FORMAT_SEED_ONLY = "seed-only";
    /** Inner KeyBean version of a file carrying the 25 words. */
    public static final String INNER_VERSION_WORDS = "0.1";
    /** Inner KeyBean version of a seed-only file (words empty). */
    public static final String INNER_VERSION_SEED_ONLY = "0.2";
    /** Legacy outer key of some pre-ruling app files: ignored, not in the AAD. */
    public static final String LEGACY_INDEXES_KEY = "indexes";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WalletContainerV02() {
    }

    /** True when {@code rawJson} is an outer object whose version is "0.2". */
    public static boolean isV02(String rawJson) {
        try {
            JsonNode n = MAPPER.readTree(rawJson);
            return n != null && n.isObject() && n.path("version").isTextual()
                    && VERSION.equals(n.get("version").asText());
        } catch (JsonProcessingException ex) {
            return false;
        }
    }

    /**
     * Opens a v0.2 container. A malformed header throws
     * {@link UnlockWalletException} before any key derivation; a wrong
     * password or ANY change to the header or payload fails the GCM tag (GCM
     * cannot tell the two apart). Never logs the plaintext.
     */
    public static KeyBean open(String rawJson, String password) throws UnlockWalletException {
        ObjectNode outer = parseOuter(rawJson);
        String format = outer.get("format").asText();
        JsonNode kdf = outer.get("kdf");
        int iterations = kdf.get("it").intValue();
        byte[] salt = b64(kdf.get("salt"), SALT_LENGTH, "salt");
        byte[] nonce = b64(outer.get("wallet").get(0), NONCE_LENGTH, "nonce");
        byte[] ctAndTag = b64(outer.get("wallet").get(1), -1, "ciphertext");

        byte[] clear;
        byte[] key;
        try {
            key = deriveKey(password, salt, iterations);
        } catch (GeneralSecurityException ex) {
            throw new UnlockWalletException("v0.2 wallet: key derivation failed", ex);
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_LENGTH * 8, nonce));
            cipher.updateAAD(aadOf(outer));
            clear = cipher.doFinal(ctAndTag);
        } catch (AEADBadTagException ex) {
            throw new UnlockWalletException("v0.2 wallet: wrong password or tampered file");
        } catch (GeneralSecurityException ex) {
            log.error("v0.2 wallet decrypt error", ex);
            throw new UnlockWalletException("v0.2 wallet unlock failed", ex);
        } finally {
            java.util.Arrays.fill(key, (byte) 0);
        }

        KeyBean kb = parseKeyBean(new String(clear, StandardCharsets.UTF_8));
        java.util.Arrays.fill(clear, (byte) 0);
        boolean innerSeedOnly = INNER_VERSION_SEED_ONLY.equals(kb.getVersion());
        boolean outerSeedOnly = FORMAT_SEED_ONLY.equals(format);
        if (innerSeedOnly != outerSeedOnly
                || (innerSeedOnly && kb.getWords() != null && !kb.getWords().trim().isEmpty())) {
            throw new UnlockWalletException("v0.2 wallet: the file format does not match its content");
        }
        if (kb.getSeed() == null || kb.getSeed().isEmpty()) {
            throw new UnlockWalletException("v0.2 wallet: empty seed");
        }
        return kb;
    }

    /** The {@code format} of a v0.2 file ("words" / "seed-only"), header only, no password. */
    public static String formatOf(String rawJson) throws UnlockWalletException {
        return parseOuter(rawJson).get("format").asText();
    }

    /**
     * Seals {@code key} into a new v0.2 container with a fresh random salt and
     * nonce. The format follows the bean: non-blank words ⇒ {@code words}
     * (inner version 0.1), blank words ⇒ {@code seed-only} (inner version
     * 0.2, words ""). The input bean is not modified.
     */
    public static String seal(KeyBean key, String password) throws GeneralSecurityException {
        SecureRandom rng = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        rng.nextBytes(salt);
        byte[] nonce = new byte[NONCE_LENGTH];
        rng.nextBytes(nonce);
        return seal(key, password, ITERATIONS, salt, nonce);
    }

    /**
     * Deterministic core of {@link #seal(KeyBean, String)}: fixed salt and
     * nonce give a fixed file, for cross-implementation vectors. <b>Never</b>
     * reuse a salt/nonce pair in production.
     */
    static String seal(KeyBean key, String password, int iterations, byte[] salt, byte[] nonce)
            throws GeneralSecurityException {
        if (key == null || key.getSeed() == null || key.getSeed().isEmpty()) {
            throw new IllegalArgumentException("v0.2 wallet: key bean without a seed");
        }
        if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
            throw new IllegalArgumentException("v0.2 wallet: iterations out of range: " + iterations);
        }
        if (salt.length != SALT_LENGTH || nonce.length != NONCE_LENGTH) {
            throw new IllegalArgumentException("v0.2 wallet: bad salt or nonce length");
        }
        boolean seedOnly = key.getWords() == null || key.getWords().trim().isEmpty();
        String format = seedOnly ? FORMAT_SEED_ONLY : FORMAT_WORDS;
        if (!seedOnly && !SeedGenerator.verifySeedWords(splitWords(key.getWords()))) {
            // F54: a words file whose phrase does not verify would restore a
            // DIFFERENT wallet in the app (it restores from the words).
            throw new IllegalArgumentException("v0.2 wallet: the recovery words are not a valid phrase");
        }

        // Header, in the SDK's insertion order: version, algorithm, kdf{alg,it,salt,len}, format.
        JsonNodeFactory f = JsonNodeFactory.instance;
        ObjectNode outer = f.objectNode();
        outer.put("version", VERSION);
        outer.put("algorithm", ALGORITHM);
        ObjectNode kdf = outer.putObject("kdf");
        kdf.put("alg", KDF_ALGORITHM);
        kdf.put("it", iterations);
        kdf.put("salt", Base64.getEncoder().encodeToString(salt));
        kdf.put("len", KEY_LENGTH);
        outer.put("format", format);

        // Plaintext, in the SDK's KeyBean.toJson order.
        ObjectNode inner = f.objectNode();
        inner.put("version", seedOnly ? INNER_VERSION_SEED_ONLY : INNER_VERSION_WORDS);
        inner.put("algorithm", key.getAlgorithm());
        inner.put("cypher", key.getCypher() == null ? null : key.getCypher().name());
        inner.put("seed", key.getSeed());
        inner.put("words", seedOnly ? "" : key.getWords());
        byte[] plain;
        try {
            plain = MAPPER.writeValueAsString(inner).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("v0.2 wallet: cannot serialise the key bean", ex);
        }

        byte[] k = deriveKey(password, salt, iterations);
        byte[] ctAndTag;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(k, "AES"),
                    new GCMParameterSpec(TAG_LENGTH * 8, nonce));
            cipher.updateAAD(aadOf(outer));
            ctAndTag = cipher.doFinal(plain);
        } finally {
            java.util.Arrays.fill(k, (byte) 0);
            java.util.Arrays.fill(plain, (byte) 0);
        }
        ArrayNode wallet = outer.putArray("wallet");
        wallet.add(Base64.getEncoder().encodeToString(nonce));
        wallet.add(Base64.getEncoder().encodeToString(ctAndTag));
        try {
            return MAPPER.writeValueAsString(outer);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("v0.2 wallet: cannot serialise the container", ex);
        }
    }

    /** PBKDF2-HMAC-SHA512 (standard 128-byte HMAC block), UTF-8 password, raw salt. */
    static byte[] deriveKey(String password, byte[] salt, int iterations) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH * 8);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * AAD = UTF-8 of the canonical JSON (keys sorted recursively, compact) of
     * the outer object without {@code wallet} and {@code indexes}.
     */
    static byte[] aadOf(ObjectNode outer) {
        ObjectNode header = outer.deepCopy();
        header.remove("wallet");
        header.remove(LEGACY_INDEXES_KEY);
        try {
            return MAPPER.writeValueAsString(canonical(header)).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("v0.2 wallet: cannot canonicalise the header", ex);
        }
    }

    private static JsonNode canonical(JsonNode n) {
        if (n.isObject()) {
            List<String> keys = new ArrayList<>();
            for (Iterator<String> it = n.fieldNames(); it.hasNext();) {
                keys.add(it.next());
            }
            Collections.sort(keys); // ASCII keys: String order == UTF-16 order (RFC 8785)
            ObjectNode sorted = JsonNodeFactory.instance.objectNode();
            for (String k : keys) {
                sorted.set(k, canonical(n.get(k)));
            }
            return sorted;
        }
        if (n.isArray()) {
            ArrayNode a = JsonNodeFactory.instance.arrayNode();
            for (JsonNode e : n) {
                a.add(canonical(e));
            }
            return a;
        }
        return n;
    }

    /** Shape validation, mirroring the SDK's {@code kdfParamsOf}. */
    private static ObjectNode parseOuter(String rawJson) throws UnlockWalletException {
        JsonNode n;
        try {
            n = MAPPER.readTree(rawJson);
        } catch (JsonProcessingException ex) {
            throw new UnlockWalletException("v0.2 wallet: not JSON");
        }
        if (n == null || !n.isObject()) {
            throw new UnlockWalletException("v0.2 wallet: not a JSON object");
        }
        if (!VERSION.equals(n.path("version").textValue())) {
            throw new UnlockWalletException("v0.2 wallet: not a v0.2 container");
        }
        if (!ALGORITHM.equals(n.path("algorithm").textValue())) {
            throw new UnlockWalletException("v0.2 wallet: unsupported cipher");
        }
        JsonNode kdf = n.get("kdf");
        if (kdf == null || !kdf.isObject()
                || !KDF_ALGORITHM.equals(kdf.path("alg").textValue())
                || !kdf.path("len").isIntegralNumber() || kdf.get("len").longValue() != KEY_LENGTH
                || !kdf.path("it").isIntegralNumber() || !kdf.get("it").canConvertToInt()
                || !kdf.path("salt").isTextual()) {
            throw new UnlockWalletException("v0.2 wallet: unsupported key derivation");
        }
        int it = kdf.get("it").intValue();
        if (it < MIN_ITERATIONS || it > MAX_ITERATIONS) {
            throw new UnlockWalletException("v0.2 wallet: iteration count out of range");
        }
        b64(kdf.get("salt"), SALT_LENGTH, "salt");
        String format = n.path("format").textValue();
        if (!FORMAT_WORDS.equals(format) && !FORMAT_SEED_ONLY.equals(format)) {
            throw new UnlockWalletException("v0.2 wallet: unknown format");
        }
        JsonNode wallet = n.get("wallet");
        if (wallet == null || !wallet.isArray() || wallet.size() != 2) {
            throw new UnlockWalletException("v0.2 wallet: payload must be [nonce, ciphertext]");
        }
        b64(wallet.get(0), NONCE_LENGTH, "nonce");
        if (b64(wallet.get(1), -1, "ciphertext").length <= TAG_LENGTH) {
            throw new UnlockWalletException("v0.2 wallet: ciphertext too short");
        }
        return (ObjectNode) n;
    }

    private static byte[] b64(JsonNode v, int expectedLength, String what) throws UnlockWalletException {
        if (v == null || !v.isTextual()) {
            throw new UnlockWalletException("v0.2 wallet: " + what + " missing");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(v.textValue());
        } catch (IllegalArgumentException ex) {
            throw new UnlockWalletException("v0.2 wallet: " + what + " not base64");
        }
        if (expectedLength >= 0 && bytes.length != expectedLength) {
            throw new UnlockWalletException("v0.2 wallet: " + what + " must be " + expectedLength + " bytes");
        }
        return bytes;
    }

    private static KeyBean parseKeyBean(String json) throws UnlockWalletException {
        try {
            JsonNode n = MAPPER.readTree(json);
            if (n == null || !n.isObject() || !n.path("seed").isTextual()
                    || !n.path("version").isTextual() || !n.path("cypher").isTextual()) {
                throw new UnlockWalletException("v0.2 wallet: the file does not hold a key bean");
            }
            KeyBean kb = new KeyBean();
            kb.setVersion(n.get("version").textValue());
            kb.setAlgorithm(n.path("algorithm").textValue());
            kb.setCypher(KeyContexts.WalletCypher.valueOf(n.get("cypher").textValue()));
            kb.setSeed(n.get("seed").textValue());
            kb.setWords(n.path("words").isTextual() ? n.get("words").textValue() : "");
            return kb;
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new UnlockWalletException("v0.2 wallet: the file does not hold a key bean");
        }
    }

    private static List<String> splitWords(String words) {
        List<String> out = new ArrayList<>();
        for (String w : words.trim().split("\\s+")) {
            if (!w.isEmpty()) {
                out.add(w);
            }
        }
        return out;
    }
}
