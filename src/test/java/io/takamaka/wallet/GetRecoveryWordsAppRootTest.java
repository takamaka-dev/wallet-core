/*
 * 0.10.0 — appRoot-aware getRecoveryWords overload (SPIKE-0 enabler).
 */
package io.takamaka.wallet;

import io.takamaka.wallet.utils.SeedGenerator;
import io.takamaka.wallet.utils.WalletHelper;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies {@link WalletHelper#getRecoveryWords(String, String, Path)} reads a
 * wallet isolated under a custom application root, completing the
 * appRoot-overload surface. Creates a disposable, password-protected wallet
 * under a {@code @TempDir} and reads its 25 recovery words from the SAME root —
 * never touching the user's real wallet folder. Confirms the words are a valid
 * Takamaka mnemonic and that a wrong password is rejected.
 */
public class GetRecoveryWordsAppRootTest {

    @Test
    void getRecoveryWords_readsFromCustomAppRoot(@TempDir Path appRoot) throws Exception {
        final String name = "spike0-export";
        final String password = "password";

        // create + persist a disposable wallet isolated under the temp appRoot
        new InstanceWalletKeyStoreBCED25519(name, password, appRoot);

        // read its recovery words from the SAME appRoot (not the default folder)
        final String words = WalletHelper.getRecoveryWords(name, password, appRoot);
        assertNotNull(words);

        final String[] tokens = words.trim().split("\\s+");
        assertEquals(25, tokens.length, "must be a 25-word mnemonic");
        assertTrue(SeedGenerator.verifySeedWords(Arrays.asList(tokens)),
                "exported words must be a valid Takamaka mnemonic (checksum holds)");
    }

    @Test
    void wrongPassword_isRejected(@TempDir Path appRoot) throws Exception {
        final String name = "spike0-export-wp";
        new InstanceWalletKeyStoreBCED25519(name, "password", appRoot);
        assertThrows(Exception.class,
                () -> WalletHelper.getRecoveryWords(name, "wrong-password", appRoot),
                "a wrong password must not yield recovery words");
    }
}
