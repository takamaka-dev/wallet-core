package io.takamaka.wallet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.takamaka.wallet.utils.TkmSignUtils;

/**
 * F11 — the RSA-wrapped conversation key ({@code enc_key}) exists on the wire in two base64 forms, and
 * every Java consumer must read both, permanently.
 *
 * <p><b>The failure this prevents.</b> A conversation created by the Android wallet app was fetched by
 * chat-web-gui and then silently dropped: its {@code enc_key} uses the URL-safe alphabet with {@code .}
 * padding, and BouncyCastle's {@code Base64.decode} is alphabet-strict. The resulting
 * {@code DecoderException} is <b>unchecked</b>, so it escaped the caller's {@code catch (WalletException)}
 * entirely — the dual-provider fallback never ran, and the user saw nothing at all. 18 of the 60
 * conversations on the test VM were in that state.
 *
 * <p><b>Why the tolerance can never be removed.</b> {@code enc_key} sits inside the Ed25519-signed
 * {@code topic}, and the envelope is stored verbatim on the server and in every device's local database.
 * Historic values therefore cannot be re-encoded by anyone, and there is no date after which the old form
 * stops arriving. This is a permanent read contract, not a migration window.
 *
 * <p>The vectors below are <b>real captured wire bytes</b>, read out of {@code rschat} on the test VM on
 * 2026-08-05 — one written by the field-tested wallet app, one by chat-web-gui, both wrapping a key for
 * the same recipient. Using real bytes rather than locally-generated ones is the point: a value this
 * codebase produced can only prove self-consistency, which is exactly the blind spot that let the defect
 * ship.
 */
class EncKeyAlphabetToleranceTest {

    /** Written by the wallet app ({@code tkm_stage_20260803.apk}) — Base64URL, {@code .} padding. */
    private static final String APK_ENC_KEY =
            "V2bmEj8tcwluOInm4vrEyk2uTzKAAA6Phqr1Ypg1IwtxtMFXhPZCL71ylAxa--X4LEUEQe0lfEqJ"
            + "DTzC0HcDLIxtcnOvTvWIO7jsKWLQIhUYahagnN01cmxHfa9bMkKjZp2VO3yenWERSX_OcZpxdhDY"
            + "lT089orET-FI8dpGn4jYfdTjWHAx3J8fbD6jRwF0Y-wY_wWv6FwR-KAiJdcvxVAMdZ-ZtKta4m97"
            + "V-0G1yhXzCqaLIK5ixPmuA5IdBGYGpQaCP9A6pXi-4aBFgfOQi16pDOCdtoo7VdqR5BjP1-dCqn4"
            + "6U9sKcGjamjQlbWgu47Cl1RSlPRNLstjWgkMJRoFEi3CZjgwApWXkF3zT9EGGLDmoeqgYR5_uX4z"
            + "jYi5TTR7287Om4QQvy-AjcJ7BCUvrvLdcgJ6pdPPRh86RaXUvmskNTMMj8OV18DRv0gLbQrAYy0t"
            + "SW1nXqOaNdUEun3in1NRfl1o98JV1ftU1T4mB5IGL61WxNHw2Q4mYUwsXpGvAFONAO9QJRumbAQY"
            + "kQ9SLhtHPEYDfceDUnjnPsiJwxA_nH1K5AGNabrsHLZ3sKCViyKCNkCceA4D4I4nASIEvUw3obev"
            + "iYnbFJAvJVPV3oKSmOv08JU49TFJUkwcXkaWkmuN2x6uEPzLJ8DuTKNEddmb50XosksVfax5_8A.";

    /** Written by chat-web-gui (this provider's own output) — standard base64, {@code =} padding. */
    private static final String JAVA_LEGACY_ENC_KEY =
            "Ton6nMJLcungeB38uJfPvPzvanZH2AFENaZT2Rs3aERcHzlw5L9lnlvoljYR4Sib16QyDM5dkkWD"
            + "E3M5BBgjCvFrxzh2FycWBNDEcduzso3okutb31IW246qFRanSMTtf2Y02HIdZDnqW9unIh5D/J/+"
            + "AKJ7SrBK4xj4rRsWnCLFvHto7YUlHgRfgVI1I6MVvtunCkcFsjnniIepkxrMrJ0zJyggyE3T8RxK"
            + "9fCDLjv6UKT8Tegkgy1N84tSqUsqaqt8I2GX+jpr7ORPhhgFCsNfWcPKeMjnf1y1aG+d3zSLIG91"
            + "0KqgvMPq0TNAKLSmDA3dp4ZEWaaRDT5LMEgXTMc1zgm+RB90nboGUC7b0hbkR+7EeLwb/3touWkc"
            + "1rntQNYxkbT0erOacfi9VA4bpr93eb2wzUQ76Nh30mChFKyJHyW9xzfmcFECBoZWlpnSoVSaOqdb"
            + "brJZQBSdfKQLWAWqxmHYtzY0QWk16Aj9BofywsgAuusfAxToDAiKHvjvRU9b1CVR3eOOe6HzsNWi"
            + "nYJK9hHmm9uJ8k+sk3+rJxJHAAz/8c5OMWBxYHdyqRrvTKoGbq0cDDqr3smStFiycpF/fQbwf+jc"
            + "Zq88+Fo/SftJYqhA7VqewCLLu8Umb7ckxDIONy1gEXW+KTQvQ5lYYaB7XPNDWqbGVxAREz+Cei8=";

    /** RSA-4096 ciphertext = 512 bytes = 684 base64 chars in EITHER alphabet — no length check can help. */
    private static final int RSA_4096_CIPHER_BYTES = 512;

    @Test
    @DisplayName("the wallet app's URL-safe '.' enc_key decodes to a 512-byte RSA-4096 ciphertext")
    void decodesWalletAppForm() {
        assertEquals(RSA_4096_CIPHER_BYTES, TkmSignUtils.fromAnyB64ToByteArray(APK_ENC_KEY).length);
    }

    @Test
    @DisplayName("the legacy standard '=' enc_key still decodes — this must never be removed")
    void decodesLegacyJavaForm() {
        assertEquals(RSA_4096_CIPHER_BYTES, TkmSignUtils.fromAnyB64ToByteArray(JAVA_LEGACY_ENC_KEY).length);
    }

    @Test
    @DisplayName("plain BouncyCastle Base64.decode STILL throws on the URL-safe form — why the helper exists")
    void bouncyCastleAloneStillRejectsUrlSafe() {
        // Pins the CONSTRAINT, not our belief about it. If BouncyCastle ever became lenient this goes red
        // and tells us the premise moved, instead of the workaround quietly outliving its reason.
        assertThrows(org.bouncycastle.util.encoders.DecoderException.class,
                () -> org.bouncycastle.util.encoders.Base64.decode(APK_ENC_KEY));
        // ...and accepts the legacy form, so the test cannot pass for the wrong reason.
        assertDoesNotThrow(() -> org.bouncycastle.util.encoders.Base64.decode(JAVA_LEGACY_ENC_KEY));
    }

    @Test
    @DisplayName("all four alphabet/padding combinations decode to the same bytes")
    void acceptsEveryFormOnTheWire() {
        byte[] expected = TkmSignUtils.fromAnyB64ToByteArray(JAVA_LEGACY_ENC_KEY);

        String standardEq = TkmSignUtils.fromByteArrayToB64(expected);        // +/ with =
        String urlDot = TkmSignUtils.fromByteArrayToB64URL(expected);          // -_ with .
        String urlEq = standardEq.replace('+', '-').replace('/', '_');         // -_ with =
        String urlBare = urlDot.replace(".", "");                              // -_ unpadded

        for (String form : new String[] {standardEq, urlDot, urlEq, urlBare}) {
            assertArrayEquals(expected, TkmSignUtils.fromAnyB64ToByteArray(form),
                    "every form on the wire, past or future, must decode to identical bytes");
        }
    }

    @Test
    @DisplayName("a decode failure THROWS rather than returning null — no NPE deep inside a cipher")
    void invalidInputThrowsRatherThanReturningNull() {
        // The sibling helpers fromB64ToByteArray / fromB64URLToByteArray swallow failures into null, which
        // has produced NullPointerExceptions inside cipher calls (SF-4). This one must not.
        assertThrows(org.bouncycastle.util.encoders.DecoderException.class,
                () -> TkmSignUtils.fromAnyB64ToByteArray("not valid base64 !!!"));
        assertThrows(IllegalArgumentException.class,
                () -> TkmSignUtils.fromAnyB64ToByteArray(null));
    }
}
