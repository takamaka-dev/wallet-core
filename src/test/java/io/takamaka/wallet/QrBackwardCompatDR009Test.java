/*
 * DR-009 backward-compat guard for PRINTED encrypted-QR wallets.
 */
package io.takamaka.wallet;

import io.takamaka.wallet.beans.EncKeyBean;
import io.takamaka.wallet.beans.EncWordsBean;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.TkmTextUtils;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DR-009 backward-compatibility guard for <b>printed encrypted-QR wallets</b>.
 *
 * <p>A printed QR carries a serialized {@link EncWordsBean} whose embedded
 * {@link EncKeyBean} is hashed (sha3-256) into the CRC <em>at print time</em>.
 * Those QRs are immutable artifacts in the wild and MUST remain readable
 * forever. Adding the DR-009 v2 header fields ({@code kdf/salt/iterations}) to
 * {@code EncKeyBean} would change its JSON — and therefore break every printed
 * CRC — unless those fields are omitted for a legacy v1 bean. This test freezes
 * that contract so the suppression can never silently regress:
 * <ol>
 *   <li>a v1 {@code EncKeyBean} serializes byte-identically to the pre-DR-009
 *       format (no {@code kdf/salt/iterations} leak into the JSON);</li>
 *   <li>a QR printed before DR-009 still passes {@link EncWordsBean#isValid()}
 *       under current code (CRC recomputation matches the print-time CRC).</li>
 * </ol>
 */
public class QrBackwardCompatDR009Test {

    // Fixed, representative v1 keystore payload as embedded in a printed QR.
    private static final byte[] IV = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final byte[] CT = Base64.getDecoder().decode(
            "qrste5T0vH8sNFZ4kKvN7xI0VniQq83vEjRWeJCrze8SNFZ4kKvN7w==");

    private static EncKeyBean v1Eb() {
        return new EncKeyBean(KeyContexts.WALLET_JSON_AES, new byte[][]{IV, CT});
    }

    /** Build a printed-QR bean exactly as a generator would have, pre-DR-009. */
    private static EncWordsBean printedQr() {
        EncWordsBean ewb = new EncWordsBean();
        ewb.setEb(v1Eb());
        ewb.setqType(KeyContexts.QrType.IMPORT_ENCRYPTED_KEY_WORDS);
        ewb.setcType(KeyContexts.CrcType.TYPE_1);
        return ewb;
    }

    @Test
    void v1EncKeyBean_serializesByteIdentically() {
        String json = TkmTextUtils.toJson(v1Eb());
        // The DR-009 header fields must NEVER appear in a v1 bean's JSON; their
        // presence would change the QR CRC and break every printed wallet.
        assertFalse(json.contains("\"kdf\""), "v1 EncKeyBean must not serialize kdf");
        assertFalse(json.contains("\"salt\""), "v1 EncKeyBean must not serialize salt");
        assertFalse(json.contains("\"iterations\""), "v1 EncKeyBean must not serialize iterations");
        // Frozen byte-for-byte shape (version + algorithm + wallet only).
        assertEquals(
                "{\"version\":\"0.1\",\"algorithm\":\"AES\",\"wallet\":[\""
                + Base64.getEncoder().encodeToString(IV) + "\",\""
                + Base64.getEncoder().encodeToString(CT) + "\"]}",
                json);
    }

    @Test
    void printedQr_stillValidatesAfterDr009() {
        // CRC as the QR was stamped with at print time.
        String printTimeCrc = printedQr().getCrc();
        assertNotNull(printTimeCrc);

        // Simulate a scan: the reader parses the QR JSON and the stamped CRC,
        // then isValid() recomputes sha3-256(toJson(eb)) and compares.
        EncWordsBean scanned = printedQr();
        scanned.setJsonReadedCrc(printTimeCrc);
        assertDoesNotThrow(() -> assertTrue(scanned.isValid(),
                "a QR printed before DR-009 must still validate under current code"));

        // Frozen CRC vector — pins the CRC algorithm + EncKeyBean serialization
        // together. If this literal ever changes, printed QRs have been broken.
        assertEquals("64f290c08befd2c379290d69767905184ee51e834e71d149bb88b749fd57d648", printTimeCrc);
    }
}
