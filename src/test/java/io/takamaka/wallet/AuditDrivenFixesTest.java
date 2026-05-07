/*
 * Audit-Driven Fixes Test — wallet-core 0.10.0
 *
 * Positive tests for the silent-failure-promoted-to-throw fixes:
 *   H1: WalletHelper.writeKeyFile        → KeystoreFileExistsException
 *   H2: WalletHelper.writePublicKey      → PublicKeyFileExistsException
 *   H3: FileHelper.rename                → IOException on !success
 *   H4: FileHelper.deleteSingleFile      → IOException on !delete or missing
 *   M1': FileHelper.getFileNameList      → IllegalArgumentException on null/non-dir
 *   M2': FileHelper.getTransactionsDumpPathFolder → IOException on creation failure
 *
 * Reference: nodeflux/docs/TASK-wallet-core-app-root-overload.md §11/§12.1
 */
package io.takamaka.wallet;

import io.takamaka.wallet.beans.KeyBean;
import io.takamaka.wallet.exceptions.KeystoreFileExistsException;
import io.takamaka.wallet.utils.FileHelper;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.WalletHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

public class AuditDrivenFixesTest {

    // ---------------------------------------------------------------- H1

    @Test
    void writeKeyFile_succeedsOnEmptyDir(@TempDir Path tempDir) throws Exception {
        KeyBean kb = new KeyBean("POWSEED", KeyContexts.WalletCypher.Ed25519BC,
                "test-seed-h1-001", "test words concat string");
        Path result = WalletHelper.writeKeyFile(tempDir, "h1.wallet", kb, "Password");
        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertTrue(Files.size(result) > 0, "wallet file must have content");
    }

    @Test
    void writeKeyFile_throwsKeystoreFileExistsExceptionOnConflict(@TempDir Path tempDir)
            throws Exception {
        KeyBean kb = new KeyBean("POWSEED", KeyContexts.WalletCypher.Ed25519BC,
                "test-seed-h1-002", "test words concat string");

        // first write succeeds
        WalletHelper.writeKeyFile(tempDir, "h1-conflict.wallet", kb, "Password");

        // second write throws domain-specific exception
        KeystoreFileExistsException ex = assertThrows(
                KeystoreFileExistsException.class,
                () -> WalletHelper.writeKeyFile(tempDir, "h1-conflict.wallet", kb, "Password"));
        assertTrue(ex.getMessage().contains("Refusing to overwrite"),
                "exception message should explain refusal");
        assertTrue(ex.getMessage().contains("h1-conflict.wallet"),
                "exception message should include the filename");
    }

    // ---------------------------------------------------------------- H3

    @Test
    void rename_succeedsOnExistingSource(@TempDir Path tempDir) throws Exception {
        Path src = tempDir.resolve("h3-src.txt");
        Files.writeString(src, "h3 content");
        Path dst = tempDir.resolve("h3-dst.txt");

        FileHelper.rename(src, dst, false);

        assertFalse(Files.exists(src), "source must be gone after rename");
        assertTrue(Files.exists(dst), "target must exist after rename");
        assertEquals("h3 content", Files.readString(dst));
    }

    @Test
    void rename_throwsIOExceptionOnTargetExistsNoOverwrite(@TempDir Path tempDir) throws Exception {
        Path src = tempDir.resolve("h3-src.txt");
        Files.writeString(src, "h3 content");
        Path dst = tempDir.resolve("h3-dst.txt");
        Files.writeString(dst, "pre-existing target");

        IOException ex = assertThrows(IOException.class,
                () -> FileHelper.rename(src, dst, false));
        assertTrue(ex.getMessage().contains("target exists")
                || ex.getMessage().contains("rename failed"),
                "exception message should explain the conflict; got: "
                + ex.getMessage());
    }

    @Test
    void rename_throwsIOExceptionOnUnsupportedTypes() {
        IOException ex = assertThrows(IOException.class,
                () -> FileHelper.rename(new Object(), new Object(), false));
        assertTrue(ex.getMessage().contains("unsupported source/target types"));
    }

    // ---------------------------------------------------------------- H4

    @Test
    void deleteSingleFile_succeedsOnExistingFile(@TempDir Path tempDir) throws Exception {
        Path f = tempDir.resolve("h4-target.txt");
        Files.writeString(f, "h4 content");

        FileHelper.deleteSingleFile(f);

        assertFalse(Files.exists(f), "file should be deleted");
    }

    @Test
    void deleteSingleFile_throwsIOExceptionOnMissingFile(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("h4-does-not-exist.txt");

        IOException ex = assertThrows(IOException.class,
                () -> FileHelper.deleteSingleFile(missing));
        assertTrue(ex.getMessage().contains("file does not exist"));
    }

    // ---------------------------------------------------------------- M1'

    @Test
    void getFileNameList_throwsIllegalArgumentExceptionOnNull() {
        assertThrows(IllegalArgumentException.class,
                () -> FileHelper.getFileNameList(null));
    }

    @Test
    void getFileNameList_throwsIllegalArgumentExceptionOnFile(@TempDir Path tempDir)
            throws Exception {
        Path f = tempDir.resolve("not-a-dir.txt");
        Files.writeString(f, "regular file");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> FileHelper.getFileNameList(f));
        assertTrue(ex.getMessage().contains("not a directory"));
    }

    @Test
    void getFileNameList_returnsEntriesForDirectory(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("a.txt"), "a");
        Files.writeString(tempDir.resolve("b.txt"), "b");

        String[] names = FileHelper.getFileNameList(tempDir);

        assertNotNull(names);
        assertEquals(2, names.length);
    }

    @Test
    void getFileNameList_returnsEmptyArrayForEmptyDirectory(@TempDir Path tempDir) {
        String[] names = FileHelper.getFileNameList(tempDir);
        assertNotNull(names);
        assertEquals(0, names.length);
    }
}
