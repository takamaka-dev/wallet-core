// Vector source: rschat-docs/security/vectors/cross_library_derivation_vectors_2026-09-25.json (sha256 aa99466043e0583dabecfe2ac629ee33e3fdd1aa09ef44f71c83b5af164c4c43)
/*
 * F350 — the cross-library seeded-derivation known-answer file, pinned on the
 * JAVA REFERENCE itself.
 *
 * The same JSON file (copied verbatim, sha256-asserted) is loaded by the
 * committed offline tests of wallet-core-flutter, rsclient-flutter and tkmChat
 * (and handed to the takamaka-sdk-wrap owner). This class REGENERATES every
 * deterministic value of that file from the real wallet-core classes and
 * asserts equality, so a wallet-core change (or a BouncyCastle bump: RSA
 * consumes the seeded stream) that moves a derivation fails HERE first and
 * must become a conscious vector bump, instead of silently forking the ports
 * (the F340 class: a re-implemented derivation drifting from Java).
 *
 * Covered: d1 words->seed, d1b 25-word checksum, the SeededRandom stream of
 * the three scopes, d2 Ed25519 idx 0/1/7 (public key, private seed,
 * signature), d3 RSA-4096 chat invite key idx 0/1 (modulus, e, p/q head,
 * SPKI Base64URL string + DER sha256, ENC == ENC256), d4 Curve25519 idx
 * 0/1/7, d5 QTESLA R1/R2 idx 0 public key, d6 v0_1_a PBKDF2 key per scope
 * (+ JCA decryption of the Java ciphertext), d6s stream key.
 * The ciphertexts themselves carry random IVs/salts: they are decrypt
 * vectors, produced by takamaka-extra, and are only decrypted here.
 *
 * Analysis: rschat-docs/analysis/CROSS_LIBRARY_KEY_DERIVATION_PARITY_2026-09-25.md
 * SECURITY: MN-001 is a published test mnemonic, never a real wallet.
 */
package io.takamaka.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.takamaka.wallet.beans.KeyBean;
import io.takamaka.wallet.utils.DefaultInitParameters;
import io.takamaka.wallet.utils.FileHelper;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.SeededRandom;
import io.takamaka.wallet.utils.TkmSignUtils;
import io.takamaka.wallet.utils.WalletHelper;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.UrlBase64;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

public class CrossLibraryDerivationVectorsTest {

    /** sha256 of the verbatim copy of the canonical rschat-docs file. A local edit of the copy fails loudly. */
    static final String VECTORS_SHA256 = "aa99466043e0583dabecfe2ac629ee33e3fdd1aa09ef44f71c83b5af164c4c43";
    static final String VECTORS_RESOURCE = "vectors/cross_library_derivation_vectors_2026-09-25.json";
    static final String WALLET_NAME = "f350vec";
    static final String WALLET_PASSWORD = "vector-password";

    @TempDir
    static Path root;

    private static byte[] raw;
    private static JsonNode v;
    private static List<String> mnemonic;
    private static String seed;

    @BeforeAll
    static void load() throws Exception {
        try (InputStream in = CrossLibraryDerivationVectorsTest.class.getClassLoader().getResourceAsStream(VECTORS_RESOURCE)) {
            assertNotNull(in, VECTORS_RESOURCE);
            raw = in.readAllBytes();
        }
        v = new ObjectMapper().readTree(raw);
        mnemonic = list(v.get("mnemonic"));
        seed = v.at("/d1_words_to_seed/seed").asText();
        SeedGenerator.init();
        // The keystores read a .wallet file: write one holding the vector seed (the generator's path for
        // a checksum-invalid phrase such as MN-001).
        Path wdir = FileHelper.getDefaultWalletDirectoryPath(root);
        Files.createDirectories(wdir);
        WalletHelper.writeKeyFile(wdir, WALLET_NAME + DefaultInitParameters.WALLET_EXTENSION,
                new KeyBean(KeyContexts.WALLET_JSON_AES, KeyContexts.WalletCypher.Ed25519BC, seed, String.join(" ", mnemonic)),
                WALLET_PASSWORD);
    }

    private static List<String> list(JsonNode arr) {
        List<String> out = new ArrayList<>();
        arr.forEach(n -> out.add(n.asText()));
        return out;
    }

    private static String hex(byte[] b) {
        return Hex.toHexString(b);
    }

    private static String sha256(byte[] b) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(b));
    }

    private static byte[] pbkdf2(String pw, byte[] salt, int it, int bits) throws Exception {
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(new PBEKeySpec(pw.toCharArray(), salt, it, bits)).getEncoded();
    }

    @Test
    void vectorFileIsTheVerbatimCanonicalCopy() throws Exception {
        assertEquals(VECTORS_SHA256, sha256(raw),
                "the vector copy differs from rschat-docs/security/vectors/cross_library_derivation_vectors_2026-09-25.json: re-copy it, never edit it");
        assertTrue(v.get("_readme").asText().startsWith("Cross-library key-derivation known-answer vectors"), "_readme intact");
    }

    @Test
    void d1_wordsToSeed() throws Exception {
        assertEquals(seed, SeedGenerator.generateSeedPWH(mnemonic), "d1 seed");
        assertEquals(v.get("mnemonic_checksum_valid_java").asBoolean(), SeedGenerator.verifySeedWords(mnemonic),
                "MN-001 is a derivation vector, its words 24/25 are not a valid checksum");
    }

    @Test
    void d1b_checksum() throws Exception {
        JsonNode c = v.get("d1b_checksum_valid_phrase");
        List<String> w = list(c.get("words"));
        assertTrue(c.get("verifySeedWords").asBoolean());
        assertEquals(c.get("verifySeedWords").asBoolean(), SeedGenerator.verifySeedWords(w), "d1b valid phrase");
        List<String> swapped = new ArrayList<>(w);
        Collections.swap(swapped, 0, 1);
        assertEquals(c.get("swapped_first_two_verifySeedWords").asBoolean(), SeedGenerator.verifySeedWords(swapped), "d1b swapped pair");
        // words 24/25 = dict[PWHash(concat, "TakamakaWalletWords", 1, 4096) as signed BigInteger mod 2048]
        String concat = String.join("", w.subList(0, 23));
        int i1 = new BigInteger(TkmSignUtils.PWHash(concat, "TakamakaWalletWords", 1, 4096)).mod(BigInteger.valueOf(2048)).intValue();
        int i2 = new BigInteger(TkmSignUtils.PWHash(concat + SeedGenerator.words[i1], "TakamakaWalletWords", 1, 4096)).mod(BigInteger.valueOf(2048)).intValue();
        assertEquals(c.get("checksum_indexes").get(0).asInt(), i1, "checksum index 24");
        assertEquals(c.get("checksum_indexes").get(1).asInt(), i2, "checksum index 25");
        assertEquals(w.get(23), SeedGenerator.words[i1]);
        assertEquals(w.get(24), SeedGenerator.words[i2]);
        assertEquals(c.get("seed").asText(), SeedGenerator.generateSeedPWH(w), "d1b seed");
        Ed25519KeyPairGenerator g = new Ed25519KeyPairGenerator();
        g.init(new Ed25519KeyGenerationParameters(new SeededRandom(c.get("seed").asText(), KeyContexts.WALLET_KEY_CHAIN, 1)));
        assertEquals(c.get("ed25519_index0_publicKeyHex").asText(),
                hex(((Ed25519PublicKeyParameters) g.generateKeyPair().getPublic()).getEncoded()), "d1b Ed25519 idx 0");
    }

    @Test
    void seededRandomStreams() {
        JsonNode d = v.get("seeded_random_diag");
        for (String scope : new String[]{KeyContexts.WALLET_KEY_CHAIN, KeyContexts.RSA_PK_ENCRYPTION, KeyContexts.DETERMINISTIC_KEY_AGREEMENT}) {
            SeededRandom r = new SeededRandom(seed, scope, 1);
            byte[] b1 = new byte[32];
            r.nextBytes(b1);
            byte[] b2 = new byte[32];
            r.nextBytes(b2);
            assertEquals(d.get(scope + "|keyNumber=1|first32").asText(), hex(b1), scope + " first draw");
            assertEquals(d.get(scope + "|keyNumber=1|second32").asText(), hex(b2), scope + " second draw (per-call counter)");
        }
    }

    @Test
    void d2_ed25519() throws Exception {
        InstanceWalletKeyStoreBCED25519 ed = new InstanceWalletKeyStoreBCED25519(WALLET_NAME, WALLET_PASSWORD, root);
        for (JsonNode k : v.at("/d2_ed25519/keys")) {
            int i = k.get("index").asInt();
            assertEquals(k.get("publicKeyUrl64").asText(), ed.getPublicKeyAtIndexURL64(i), "Ed25519 publicKeyUrl64 idx " + i);
            assertEquals(k.get("publicKeyHex").asText(), hex(ed.getPublicKeyAtIndexByte(i)), "Ed25519 publicKeyHex idx " + i);
            AsymmetricCipherKeyPair kp = ed.getKeyPairAtIndex(i);
            assertEquals(k.get("privateSeedHex").asText(), hex(((Ed25519PrivateKeyParameters) kp.getPrivate()).getEncoded()), "Ed25519 private idx " + i);
            assertEquals(k.get("signatureUrl64").asText(), TkmCypherProviderBCED25519.sign(kp, k.get("sign_message").asText()).getSignature(),
                    "Ed25519 signature idx " + i);
        }
    }

    @Test
    void d3_rsa4096ChatInviteKey() throws Exception {
        InstanceWalletKeyStoreBCRSA4096ENC256 rsa256 = new InstanceWalletKeyStoreBCRSA4096ENC256(WALLET_NAME, WALLET_PASSWORD, root);
        InstanceWalletKeyStoreBCRSA4096ENC rsa1 = new InstanceWalletKeyStoreBCRSA4096ENC(WALLET_NAME, WALLET_PASSWORD, root);
        for (JsonNode k : v.at("/d3_rsa4096_chat_invite/keys")) {
            int i = k.get("index").asInt();
            AsymmetricCipherKeyPair kp = rsa256.getKeyPairAtIndex(i);
            RSAKeyParameters pub = (RSAKeyParameters) kp.getPublic();
            RSAPrivateCrtKeyParameters prv = (RSAPrivateCrtKeyParameters) kp.getPrivate();
            assertEquals(k.get("modulusHex").asText(), pub.getModulus().toString(16), "RSA modulus idx " + i);
            assertEquals(k.get("publicExponent").asText(), pub.getExponent().toString(), "RSA e idx " + i);
            assertEquals(k.get("pHexHead16").asText(), prv.getP().toString(16).substring(0, 16), "RSA p idx " + i);
            assertEquals(k.get("qHexHead16").asText(), prv.getQ().toString(16).substring(0, 16), "RSA q idx " + i);
            assertEquals(k.get("publicKeyUrl64").asText(), rsa256.getPublicKeyAtIndexURL64(i), "RSA SPKI url64 idx " + i);
            assertEquals(k.get("spkiDerSha256").asText(), sha256(rsa256.getPublicKeyAtIndexByte(i)), "RSA SPKI sha256 idx " + i);
            assertEquals(k.get("same_as_ENC_sha1_class").asBoolean(),
                    rsa1.getPublicKeyAtIndexURL64(i).equals(rsa256.getPublicKeyAtIndexURL64(i)), "ENC == ENC256 idx " + i);
        }
    }

    @Test
    void d4_curve25519() throws Exception {
        InstanceWalletKeyStoreBCCurve25519 cv = new InstanceWalletKeyStoreBCCurve25519(WALLET_NAME, WALLET_PASSWORD, root);
        for (JsonNode k : v.at("/d4_curve25519/keys")) {
            int i = k.get("index").asInt();
            assertEquals(k.get("publicKeyUrl64").asText(), cv.getPublicKeyAtIndexURL64(i), "Curve25519 url64 idx " + i);
            assertEquals(k.get("publicKeyHex").asText(), hex(cv.getPublicKeyAtIndexByte(i)), "Curve25519 hex idx " + i);
        }
    }

    @Test
    void d5_qtesla() throws Exception {
        assertQtesla("BCQTESLA_PS_1", new InstanceWalletKeyStoreBCQTESLAPSSC1Round1(WALLET_NAME, WALLET_PASSWORD, root));
        assertQtesla("BCQTESLA_PS_1_R2", new InstanceWalletKeyStoreBCQTESLAPSSC1Round2(WALLET_NAME, WALLET_PASSWORD, root));
    }

    private static void assertQtesla(String name, InstanceWalletKeystoreInterface ks) throws Exception {
        JsonNode e = v.at("/d5_qtesla/" + name);
        int i = e.get("index").asInt();
        String u = ks.getPublicKeyAtIndexURL64(i);
        assertEquals(e.get("publicKeyUrl64Length").asInt(), u.length(), name + " length");
        assertEquals(e.get("publicKeyUrl64Head64").asText(), u.substring(0, 64), name + " head");
        assertEquals(e.get("publicKeyUrl64Sha256").asText(), sha256(u.getBytes(StandardCharsets.US_ASCII)), name + " sha256");
    }

    @Test
    void d6_v0_1_a_conversationKdf() throws Exception {
        JsonNode d6 = v.get("d6_v0_1_a");
        assertEquals(20000, d6.get("iterations").asInt(), "v0_1_a iterations");
        String convKey = d6.get("conversation_key").asText();
        for (JsonNode c : d6.get("cases")) {
            String scope = c.get("scope").asText();
            byte[] key = TkmSignUtils.PWHash(convKey, scope, 20000, 256);
            assertEquals(c.get("derivedKeyHex").asText(), hex(key), "v0_1_a key " + scope);
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new IvParameterSpec(UrlBase64.decode(c.get("em0_iv_url64").asText())));
            String plain = new String(aes.doFinal(UrlBase64.decode(c.get("em1_ct_url64").asText())), StandardCharsets.UTF_8);
            assertEquals(d6.get("plaintext").asText(), plain, "v0_1_a Java ciphertext decrypts " + scope);
        }
    }

    @Test
    void d6s_streamGcmKey() throws Exception {
        JsonNode s = v.get("d6_v0_2_a_stream_gcm");
        JsonNode sed = s.get("sed");
        assertEquals(20000, sed.get("it").asInt());
        assertEquals(256, sed.get("kl").asInt());
        byte[] key = pbkdf2(s.get("password").asText(), Hex.decode(sed.get("salt").asText()), sed.get("it").asInt(), sed.get("kl").asInt());
        assertEquals(s.get("derivedKeyHex").asText(), hex(key), "stream GCM key");
    }
}
