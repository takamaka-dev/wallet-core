/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.exceptions.ThreadSafeUtilsException;
import io.takamaka.wallet.exceptions.WalletException;
import static io.takamaka.wallet.utils.DefaultInitParameters.QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS;
import static io.takamaka.wallet.utils.DefaultInitParameters.WALLET_EXTENSION;
import static io.takamaka.wallet.utils.DefaultInitParameters.ZERO_BLOCK_FILE_NUMBER;
import static io.takamaka.wallet.utils.FixedParameters.ADDRESS_BOOK_FOLDER;
import static io.takamaka.wallet.utils.FixedParameters.CHAIN_FOLDER;
import static io.takamaka.wallet.utils.FixedParameters.CHAIN_LOCK_FILE;
import static io.takamaka.wallet.utils.FixedParameters.CURRENT_STATE_FILE;
import static io.takamaka.wallet.utils.FixedParameters.EPOCH_FOLDER_PREFIX;
import io.takamaka.wallet.utils.FixedParameters.HexKeyWriter;
import static io.takamaka.wallet.utils.FixedParameters.SLOT_FOLDER_PREFIX;
import static io.takamaka.wallet.utils.FixedParameters.LOGS_FOLDER;
import static io.takamaka.wallet.utils.FixedParameters.PUBLICKEY_FOLDER;
import static io.takamaka.wallet.utils.FixedParameters.REFERENCE_QTESLA_ADDR_PREFIX;
import static io.takamaka.wallet.utils.FixedParameters.STATE_DB_PREFIX;
import static io.takamaka.wallet.utils.FixedParameters.STATE_FILE_EXTENSION;
import static io.takamaka.wallet.utils.FixedParameters.TRANSACTION_FOLDER;
import static io.takamaka.wallet.utils.FixedParameters.WALLET_EPHEMERAL_FOLDER;
import static io.takamaka.wallet.utils.FixedParameters.WALLET_FOLDER;
import static io.takamaka.wallet.utils.FixedParameters.ZERO_BLOCK_FOLDER;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class FileHelper {

    public static final Object KEYLOCK = new Object();
    private static String rootDir = null;

    public static final String getRootDir() {
        synchronized (KEYLOCK) {
            return rootDir;
        }
    }

    public static final void setRootDir(String rootDir) {
        synchronized (KEYLOCK) {
            FileHelper.rootDir = rootDir;
        }
    }

    public static final String[] getFileNameList(Path p) {
        // 0.10.0 M1' fix — replace silent null-return with explicit
        // IllegalArgumentException, and guard against listFiles() returning
        // null when the path is not a directory (would NPE inside
        // Arrays.stream prior to the fix). Empty directories now correctly
        // return an empty array.
        if (p == null) {
            throw new IllegalArgumentException("getFileNameList: path must not be null");
        }
        java.io.File[] files = p.toFile().listFiles();
        if (files == null) {
            // listFiles() returns null when the path is not a directory or
            // when an I/O error occurs. Distinguish for caller diagnostics.
            if (!p.toFile().isDirectory()) {
                throw new IllegalArgumentException(
                        "getFileNameList: path is not a directory: " + p.toString());
            }
            // Directory exists but listFiles failed (rare — I/O error, perm).
            return new String[0];
        }
        return Arrays.stream(files).map(f -> f.getAbsolutePath()).toArray(String[]::new);
    }

    /**
     * return the default path to application directory
     *
     * @return
     */
    public static final Path getDefaultApplicationDirectoryPath() {
        synchronized (KEYLOCK) {
            if (TkmTextUtils.isNullOrBlank(rootDir)) {
                rootDir = System.getProperty("user.home");
            }
            //Log.log(Level.INFO, userHome);
            return Paths.get(rootDir, DefaultInitParameters.APPLICATION_ROOT_FOLDER_NAME);
        }
    }

    /**
     * 0.10.0 — appRoot-aware overload. When {@code appRoot} is non-null,
     * returns it directly (caller specifies the full application directory,
     * e.g. {@code ~/.tkm-chain-fixture-1}). When null, falls back to the
     * legacy synchronized resolution against {@code System.getProperty("user.home")}.
     *
     * <p>Per nodeflux/docs/TASK-wallet-core-app-root-overload.md, every
     * Path-returning getter in this class follows the same overload pattern:
     * existing zero-arg behaviour preserved; new {@code (Path appRoot)}
     * variant chains through this method.
     *
     * @param appRoot explicit application directory; null falls back to legacy
     * @return application directory path
     * @since 0.10.0
     */
    public static final Path getDefaultApplicationDirectoryPath(Path appRoot) {
        if (appRoot == null) {
            return getDefaultApplicationDirectoryPath();
        }
        return appRoot;
    }

    public static final Path getDeletedWalletFolderPath() {
        return Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), DefaultInitParameters.DELETED_WALLET_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getDeletedWalletFolderPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), DefaultInitParameters.DELETED_WALLET_FOLDER);
    }

    public static final Path getReferenceKeysDatabaseFolder() {
        return Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), DefaultInitParameters.REFERENCE_KEYS_DATABASE_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getReferenceKeysDatabaseFolder(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), DefaultInitParameters.REFERENCE_KEYS_DATABASE_FOLDER);
    }

    public static final Path getMainDatabaseFolder() {
        return Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), DefaultInitParameters.STATE_DATABASE_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getMainDatabaseFolder(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), DefaultInitParameters.STATE_DATABASE_FOLDER);
    }

    public static final Path getTkmTempFolderPath() {
        return Paths.get(
                FileHelper.getDefaultApplicationDirectoryPath().toString(),
                FixedParameters.TEMP_DIR_NAME);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getTkmTempFolderPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.TEMP_DIR_NAME);
    }

    public static final Path getLiveStateFolter() {
        return Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), DefaultInitParameters.LIVE_STATE_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload (typo retained for API parity). @since 0.10.0 */
    public static final Path getLiveStateFolter(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), DefaultInitParameters.LIVE_STATE_FOLDER);
    }

    public static final Path getApplicationConfigFile() {
        synchronized (KEYLOCK) {
            return Paths.get(getDefaultApplicationDirectoryPath().toString(), FixedParameters.CONFIG_FILE_NAME);
        }
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getApplicationConfigFile(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.CONFIG_FILE_NAME);
    }

    public static final Path getThemeConfigFilePath() {
        String userHome = rootDir;
        return Paths.get(userHome, DefaultInitParameters.APPLICATION_ROOT_FOLDER_NAME, "themeConfig.json");
    }

    /**
     * 0.10.0 — appRoot-aware overload. NOTE: the zero-arg form references
     * {@code rootDir} directly (a legacy artifact of the pre-overload state).
     * The appRoot variant uses the foundation overload, which gives correct
     * resolution with explicit appRoot OR falls back to legacy when null.
     *
     * @since 0.10.0
     */
    public static final Path getThemeConfigFilePath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), "themeConfig.json");
    }

    public static final Path getHotmokaTestDirectoryPath() {
        String userHome = rootDir;
        //Log.log(Level.INFO, userHome);
        return Paths.get(getDefaultApplicationDirectoryPath().toString(), DefaultInitParameters.HOTMOKA_TEST_FOLDER_NAME);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getHotmokaTestDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), DefaultInitParameters.HOTMOKA_TEST_FOLDER_NAME);
    }

    public static final Path getHotmokaFilesDirectoryPath() {
        String userHome = rootDir;
        //Log.log(Level.INFO, userHome);
        return Paths.get(getDefaultApplicationDirectoryPath().toString(), DefaultInitParameters.HOTMOKA_FILES_FOLDER_NAME);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getHotmokaFilesDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), DefaultInitParameters.HOTMOKA_FILES_FOLDER_NAME);
    }

    /**
     * return the default user wallets directory
     *
     * @return
     */
    public static final Path getUserWalletsDirectoryPath() {
        String userHome = rootDir;
        //Log.log(Level.INFO, userHome);
        return Paths.get(getDefaultApplicationDirectoryPath().toString(), FixedParameters.USER_WALLETS_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getUserWalletsDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.USER_WALLETS_FOLDER);
    }

    /**
     * return the path of the application state file
     *
     * @return
     */
    public static final Path getDefaultStatePath() {
        return Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), CURRENT_STATE_FILE + STATE_FILE_EXTENSION);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getDefaultStatePath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), CURRENT_STATE_FILE + STATE_FILE_EXTENSION);
    }

    /**
     * true if the application state file exists
     *
     * @return
     */
    public static final boolean defaultStatePathExists() {
        return Paths.get(FileHelper.getChainDirectory().toString(), CURRENT_STATE_FILE + STATE_FILE_EXTENSION).toFile().exists();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean defaultStatePathExists(Path appRoot) {
        return Paths.get(getChainDirectory(appRoot).toString(), CURRENT_STATE_FILE + STATE_FILE_EXTENSION).toFile().exists();
    }

    /**
     * @return true if the default project home dir exist
     */
    public static final boolean homeDirExists() {
        Path applicationDirectoryPath = getDefaultApplicationDirectoryPath();
        File applicationDirectoryFilePointer = applicationDirectoryPath.toFile();
        return applicationDirectoryFilePointer.isDirectory();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean homeDirExists(Path appRoot) {
        return getDefaultApplicationDirectoryPath(appRoot).toFile().isDirectory();
    }

    /**
     * @return true if the theme configuration file exists
     */
    public static final boolean themeConfigFileExists() {
        Path themePath = getThemeConfigFilePath();
        File themeFile = themePath.toFile();
        return themeFile.isFile();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean themeConfigFileExists(Path appRoot) {
        return getThemeConfigFilePath(appRoot).toFile().isFile();
    }

    /**
     *
     * @return default wallet directory path
     */
    public static final Path getDefaultWalletDirectoryPath() {
        return Paths.get(getDefaultApplicationDirectoryPath().toString(), WALLET_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getDefaultWalletDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), WALLET_FOLDER);
    }

    /**
     *
     * @return default public keys directory path
     */
    public static final Path getPublicKeyDirectoryPath() {
        return Paths.get(getDefaultWalletDirectoryPath().toString(), PUBLICKEY_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getPublicKeyDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultWalletDirectoryPath(appRoot).toString(), PUBLICKEY_FOLDER);
    }

    /**
     *
     * @return default transactions directory path
     */
    public static final Path getTransactionsDirectoryPath() {
        return Paths.get(getDefaultWalletDirectoryPath().toString(), TRANSACTION_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getTransactionsDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultWalletDirectoryPath(appRoot).toString(), TRANSACTION_FOLDER);
    }

    /**
     *
     * @return ephemeral wallet directory path
     */
    public static final Path getEphemeralWalletDirectoryPath() {
        return Paths.get(getDefaultApplicationDirectoryPath().toString(), WALLET_EPHEMERAL_FOLDER);
    }

    /**
     * 0.10.0 — appRoot-aware overload. Routes to the legacy flat ephemeral
     * directory under appRoot (or under default app dir when appRoot is null).
     * For epoch-routed ephemeral storage, see
     * {@link #getEphemeralWalletDirectoryPath(int, Path)}.
     * @since 0.10.0
     */
    public static final Path getEphemeralWalletDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), WALLET_EPHEMERAL_FOLDER);
    }

    /**
     * 0.10.0 — epoch-aware overload (Proposal 3 from
     * TASK-wallet-core-app-root-overload.md §12.4).
     *
     * <p>Routing rules (strict — see §12.4.1):
     * <ul>
     *   <li>{@code epoch == -1}: legacy flat directory
     *       {@code {appRoot}/ephemeralWallets/} — same as
     *       {@link #getEphemeralWalletDirectoryPath(Path)}</li>
     *   <li>{@code epoch >= 0}: subdirectory
     *       {@code {appRoot}/ephemeralWallets/E{epoch:05d}/} —
     *       per-epoch isolation; supports filesystem-level GC by epoch</li>
     *   <li>{@code epoch < -1}: throws {@link IllegalArgumentException} —
     *       no defensive fallthrough; defends against typos like
     *       {@code Integer.MIN_VALUE} or {@code -2}</li>
     * </ul>
     *
     * <p>Rationale: ephemeral wallets at scale (forward-key rotation use case
     * surfaced in nodeflux v0.13.4) can produce 24000+ wallet files per epoch.
     * A flat directory with that many files degrades performance on many
     * filesystems and complicates lifecycle GC. Per-epoch routing makes both
     * tractable.
     *
     * @param epoch -1 for legacy flat, &gt;= 0 for epoch-routed subdir
     * @param appRoot explicit application directory; null uses legacy default
     * @return resolved ephemeral wallet directory path
     * @throws IllegalArgumentException if epoch &lt; -1
     * @since 0.10.0
     */
    public static final Path getEphemeralWalletDirectoryPath(int epoch, Path appRoot) {
        if (epoch == -1) {
            return getEphemeralWalletDirectoryPath(appRoot);
        }
        if (epoch < -1) {
            throw new IllegalArgumentException(
                    "epoch must be either -1 (legacy flat dir) or >= 0 "
                    + "(epoch-routed subdir); got " + epoch);
        }
        Path baseEphemeral = getEphemeralWalletDirectoryPath(appRoot);
        return baseEphemeral.resolve(String.format("E%05d", epoch));
    }

    /**
     *
     * @return default address book directory path
     */
    public static final Path getDefaultAddressBookDirectoryPath() {
        return Paths.get(getDefaultWalletDirectoryPath().toString(),
                ADDRESS_BOOK_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getDefaultAddressBookDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultWalletDirectoryPath(appRoot).toString(), ADDRESS_BOOK_FOLDER);
    }

    /**
     *
     * @return default logs directory path
     */
    public static final Path getDefaultLogsDirectoryPath() {
        return Paths.get(getDefaultApplicationDirectoryPath().toString(),
                LOGS_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getDefaultLogsDirectoryPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), LOGS_FOLDER);
    }

    /**
     * convert pattern from {@code Log.getLogFilePattern()} to path
     *
     * @param pattern
     * @return
     */
    public static final Path logFile(String pattern) {
        return Paths.get(getDefaultLogsDirectoryPath().toString(), pattern);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path logFile(String pattern, Path appRoot) {
        return Paths.get(getDefaultLogsDirectoryPath(appRoot).toString(), pattern);
    }

    /**
     * return the path of default chain directory
     *
     * @return
     */
    public static final Path getChainDirectory() {
        return Paths.get(getDefaultApplicationDirectoryPath().toString(),
                CHAIN_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getChainDirectory(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), CHAIN_FOLDER);
    }

    /**
     * default path for chain lock file
     *
     * @return
     */
    public static final Path getChainLockFile() {
        return Paths.get(getChainDirectory().getParent().toString(), CHAIN_LOCK_FILE);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getChainLockFile(Path appRoot) {
        return Paths.get(getChainDirectory(appRoot).getParent().toString(), CHAIN_LOCK_FILE);
    }

    /**
     * return the path to the epoch folder
     *
     * @param epochNumber
     * @return
     */
    public static final Path getEpochDirectory(int epochNumber) {
        return Paths.get(getChainDirectory().toString(),
                EPOCH_FOLDER_PREFIX + epochNumber);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getEpochDirectory(int epochNumber, Path appRoot) {
        return Paths.get(getChainDirectory(appRoot).toString(), EPOCH_FOLDER_PREFIX + epochNumber);
    }

    /**
     * return the path to the slot directory
     *
     * @param epochNumber
     * @param slotNumber
     * @return
     */
    public static final Path getSlotDirectory(int epochNumber, int slotNumber) {
        return Paths.get(getEpochDirectory(epochNumber).toString(),
                SLOT_FOLDER_PREFIX + slotNumber);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getSlotDirectory(int epochNumber, int slotNumber, Path appRoot) {
        return Paths.get(getEpochDirectory(epochNumber, appRoot).toString(), SLOT_FOLDER_PREFIX + slotNumber);
    }

    /**
     * return the path to the zero block folder
     *
     * @return
     */
    public static final Path getDefaultZeroBlockDirectory() {
        return Paths.get(getDefaultApplicationDirectoryPath().toString(), ZERO_BLOCK_FOLDER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getDefaultZeroBlockDirectory(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), ZERO_BLOCK_FOLDER);
    }

    /**
     * return the path to the block required to initialize the chain
     *
     * @return
     */
    public static final Path getDefaultZeroBlockFile() {
        return Paths.get(getDefaultZeroBlockDirectory().toString(), ZERO_BLOCK_FILE_NUMBER);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getDefaultZeroBlockFile(Path appRoot) {
        return Paths.get(getDefaultZeroBlockDirectory(appRoot).toString(), ZERO_BLOCK_FILE_NUMBER);
    }

    /**
     * return the bucket directory
     *
     * @param epoch
     * @param slot
     * @param uid
     * @param hkw
     * @return
     */
    public static final Path getKeyWriterBucketDirectory(int epoch, int slot, String uid, HexKeyWriter hkw) {
        return Paths.get(getSlotDirectory(epoch, slot).toString(), uid + "." + hkw.name());
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getKeyWriterBucketDirectory(int epoch, int slot, String uid, HexKeyWriter hkw, Path appRoot) {
        return Paths.get(getSlotDirectory(epoch, slot, appRoot).toString(), uid + "." + hkw.name());
    }

    public static final Path initKeyHexWriterDirectoryTree(int epoch, int slot, String uid, HexKeyWriter hkw) throws IOException {
        Path baseDir = null;
        log.info("init Key Hex Dir for " + hkw.name());
        baseDir = FileHelper.getKeyWriterBucketDirectory(epoch, slot, uid, hkw);
        FileHelper.createDir(baseDir);
        return baseDir;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path initKeyHexWriterDirectoryTree(int epoch, int slot, String uid, HexKeyWriter hkw, Path appRoot) throws IOException {
        log.info("init Key Hex Dir for " + hkw.name());
        Path baseDir = FileHelper.getKeyWriterBucketDirectory(epoch, slot, uid, hkw, appRoot);
        FileHelper.createDir(baseDir);
        return baseDir;
    }

    /**
     * First block directory
     *
     * @return true if exists and is a directory
     */
    public static final boolean defaultZeroBlockDirectoryExists() {
        return getDefaultZeroBlockDirectory().toFile().isDirectory();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean defaultZeroBlockDirectoryExists(Path appRoot) {
        return getDefaultZeroBlockDirectory(appRoot).toFile().isDirectory();
    }

    /**
     * First block
     *
     * @return true if is file and exixsts
     */
    public static final boolean defaultZeroBlockFileExists() {
        return getDefaultZeroBlockFile().toFile().isFile();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean defaultZeroBlockFileExists(Path appRoot) {
        return getDefaultZeroBlockFile(appRoot).toFile().isFile();
    }

    /**
     * Slot directory
     *
     * @param epochNumber
     * @param slotNumber
     * @return true if exists and is a directory
     */
    public static final boolean slotDirectoryExists(int epochNumber, int slotNumber) {
        return getSlotDirectory(epochNumber, slotNumber).toFile().isDirectory();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean slotDirectoryExists(int epochNumber, int slotNumber, Path appRoot) {
        return getSlotDirectory(epochNumber, slotNumber, appRoot).toFile().isDirectory();
    }

    /**
     * Epoch directory
     *
     * @param epochNumber
     * @return true if exists and is a directory
     */
    public static final boolean epochDirectoryExists(int epochNumber) {
        return getEpochDirectory(epochNumber).toFile().isDirectory();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean epochDirectoryExists(int epochNumber, Path appRoot) {
        return getEpochDirectory(epochNumber, appRoot).toFile().isDirectory();
    }

    public static final Path getNodeNetworkSettings() {
        Path settingsPathFolder = FileHelper.getSettingsPathFolder();
        Path nodeNetworkSettingsPath = Paths.get(settingsPathFolder.toString(), FixedParameters.NODE_NETWORK_SETTINGS_FILE);
        return nodeNetworkSettingsPath;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getNodeNetworkSettings(Path appRoot) {
        return Paths.get(getSettingsPathFolder(appRoot).toString(), FixedParameters.NODE_NETWORK_SETTINGS_FILE);
    }

    /**
     * Chain directory
     *
     * @return true if exists and is a directory
     */
    public static final boolean chainDirectoryExists() {
        return getChainDirectory().toFile().isDirectory();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean chainDirectoryExists(Path appRoot) {
        return getChainDirectory(appRoot).toFile().isDirectory();
    }

    /**
     * Log directory
     *
     * @return true if exists and is a directory
     */
    public static final boolean logsDirectoryExists() {
        return getDefaultLogsDirectoryPath().toFile().isDirectory();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean logsDirectoryExists(Path appRoot) {
        return getDefaultLogsDirectoryPath(appRoot).toFile().isDirectory();
    }

    /**
     * Create slot directory and return path, or return slot directory path if
     * exists
     *
     * @param epochNumber
     * @param slotNumber
     * @return slot directory path
     * @throws IOException
     */
    public static final Path createSlotDirectory(int epochNumber, int slotNumber) throws IOException {
        if (!chainDirectoryExists()) {
            createDir(getChainDirectory());
        }
        log.info("chain directory " + getChainDirectory());
        if (!epochDirectoryExists(epochNumber)) {
            createDir(getEpochDirectory(epochNumber));
        }
        log.info("epoch directory " + getEpochDirectory(epochNumber));
        if (!slotDirectoryExists(epochNumber, slotNumber)) {
            createDir(getSlotDirectory(epochNumber, slotNumber));
        }
        log.info("slot directory " + getSlotDirectory(epochNumber, slotNumber));
        return getSlotDirectory(epochNumber, slotNumber);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path createSlotDirectory(int epochNumber, int slotNumber, Path appRoot) throws IOException {
        Path chainDir = getChainDirectory(appRoot);
        if (!chainDir.toFile().isDirectory()) {
            createDir(chainDir);
        }
        log.info("chain directory " + chainDir);
        Path epochDir = getEpochDirectory(epochNumber, appRoot);
        if (!epochDir.toFile().isDirectory()) {
            createDir(epochDir);
        }
        log.info("epoch directory " + epochDir);
        Path slotDir = getSlotDirectory(epochNumber, slotNumber, appRoot);
        if (!slotDir.toFile().isDirectory()) {
            createDir(slotDir);
        }
        log.info("slot directory " + slotDir);
        return slotDir;
    }

    /**
     * Creates a directory at the indicated path if none exist already
     *
     * @param path
     * @throws IOException
     */
    public static final void createFolderAtPathIfNoneExist(Path path) throws IOException {

        if (!path.toFile().isDirectory()) {
            createDir(path);
        }
    }

    /**
     *
     * @return
     */
    public static final Path getTransactionBoxFolder(int epoch, int slot) {
        Path result = null;

        result = Paths.get(getSlotDirectory(epoch, slot).toString(), FixedParameters.TRANSACTION_BOX_FOLDER);

        return result;
    }

    /**
     *
     * @return
     */
    public static final Path getTransactionBoxDateFolder(Date input) {
        Path result = null;

        DateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
        String date = dateformat.format(input);
        result = Paths.get(getDefaultAddressBookDirectoryPath().toString(), date + "_" + FixedParameters.TRANSACTION_BOX_FOLDER);

        return result;
    }

    /**
     * convert uid into database name
     *
     * @param blockUID
     * @return
     */
    public static final String getBlockName(String blockUID) {
        return STATE_DB_PREFIX + blockUID;
    }

    /**
     * return the path without extension
     *
     * @param epochNumber
     * @param slotNumber
     * @param blockHashNumber {@code StringUtils.getBlockHashNumber(blockHash)}
     * @return return the path for the requested block
     */
    public static final Path getBlockPath(int epochNumber, int slotNumber, String blockHashNumber) {
        return Paths.get(getSlotDirectory(epochNumber, slotNumber).toString(), blockHashNumber);
    }

    /**
     * return the path with extension
     *
     * @param epochNumber
     * @param slotNumber
     * @param blockHashNumber {@code StringUtils.getBlockHashNumber(blockHash)}
     * @return return the path for the requested block
     */
    public static final Path getBlockPathWithExension(int epochNumber, int slotNumber, String blockHashNumber) {
        return Paths.get(getSlotDirectory(epochNumber, slotNumber).toString(), blockHashNumber + FixedParameters.BLOCK_FILE_EXTENSION);
    }

    /**
     *
     * @param epochNumber
     * @param slotNumber
     * @param blockHashNumber
     * @return true if file exists and is file
     */
    public static final boolean blockExists(int epochNumber, int slotNumber, String blockHashNumber) {
        return chainDirectoryExists()
                && epochDirectoryExists(epochNumber)
                && slotDirectoryExists(epochNumber, slotNumber)
                && getBlockPath(epochNumber, slotNumber, blockHashNumber).toFile().isFile();
    }

    /**
     *
     * @return true if the directory exist
     */
    public static final boolean walletDirExists() {
        return getDefaultWalletDirectoryPath().toFile().isDirectory();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean walletDirExists(Path appRoot) {
        return getDefaultWalletDirectoryPath(appRoot).toFile().isDirectory();
    }

    /**
     *
     * @return true if the directory exist
     */
    public static final boolean publicKeyDirExists() {
        return getPublicKeyDirectoryPath().toFile().isDirectory();
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final boolean publicKeyDirExists(Path appRoot) {
        return getPublicKeyDirectoryPath(appRoot).toFile().isDirectory();
    }

    /**
     *
     * @return true if the directory exist
     */
    public static final boolean transactionsDirExists() {
        return getTransactionsDirectoryPath().toFile().isDirectory();
    }

    /**
     *
     * @return true if the directory exist
     */
    public static final boolean walletEphemeralDirExists() {
        return getEphemeralWalletDirectoryPath().toFile().isDirectory();
    }

    /**
     *
     * @return true if the address book exist
     */
    public static final boolean addressBookDirExists() {
        return getDefaultAddressBookDirectoryPath().toFile().isDirectory();
    }

    /**
     * create directory the path is built using {@code Paths.get()}
     *
     * @param directoryPathAndName
     * @throws IOException
     */
    public static final void createDir(Path directoryPathAndName) throws IOException {
        Files.createDirectory(directoryPathAndName);
    }

    public static final Path getSettingsPathFolder() {
        Path settingsFolder = Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), FixedParameters.SETTINGS_FOLDER);
        return settingsFolder;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getSettingsPathFolder(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.SETTINGS_FOLDER);
    }

    public static final Path getTransactionsDumpPathFolder() {
        Path settingsFolder = Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), FixedParameters.TRANSACTIONS_DUMP_FOLDER);
        return settingsFolder;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getTransactionsDumpPathFolder(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.TRANSACTIONS_DUMP_FOLDER);
    }

    public static final Path getSimulationPathFolder() {
        Path settingsFolder = Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), FixedParameters.SIMULATION_DUMP_FOLDER);
        return settingsFolder;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getSimulationPathFolder(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.SIMULATION_DUMP_FOLDER);
    }

    public static final Path getPSQLFilePath() {
        Path settingsFolder = Paths.get(FileHelper.getSettingsPathFolder().toString(), DefaultInitParameters.psqlSettingsFile);
        return settingsFolder;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getPSQLFilePath(Path appRoot) {
        return Paths.get(getSettingsPathFolder(appRoot).toString(), DefaultInitParameters.psqlSettingsFile);
    }

    public static final Path getPSQLTablespacesRootFolderPath() {
        Path settingsFolder = Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), DefaultInitParameters.psqlTablespacesRootFolder);
        return settingsFolder;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getPSQLTablespacesRootFolderPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), DefaultInitParameters.psqlTablespacesRootFolder);
    }

    public static final Path getPSQLTablespacesBlockTable() {
        Path settingsFolder = Paths.get(FileHelper.getPSQLTablespacesRootFolderPath().toString(), DefaultInitParameters.psqlTablespacesBlocksFolderName);
        return settingsFolder;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getPSQLTablespacesBlockTable(Path appRoot) {
        return Paths.get(getPSQLTablespacesRootFolderPath(appRoot).toString(), DefaultInitParameters.psqlTablespacesBlocksFolderName);
    }

    public static final Path getTransactionsDumpPathFolder(String pidName) throws IOException {
        return getTransactionsDumpPathFolder(pidName, null);
    }

    /**
     * 0.10.0 — appRoot-aware overload. M2' fix retained: explicit IOException
     * on creation failure, no silent null-return.
     * @since 0.10.0
     */
    public static final Path getTransactionsDumpPathFolder(String pidName, Path appRoot) throws IOException {
        Path baseDumpFolder = Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.TRANSACTIONS_DUMP_FOLDER);
        if (!baseDumpFolder.toFile().isDirectory()) {
            FileHelper.createDir(baseDumpFolder);
        }
        if (!baseDumpFolder.toFile().isDirectory()) {
            throw new IOException(
                    "getTransactionsDumpPathFolder: failed to create base"
                    + " transactions-dump folder at " + baseDumpFolder);
        }
        Path inner = Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.TRANSACTIONS_DUMP_FOLDER, pidName);
        FileHelper.createDir(inner);
        if (!inner.toFile().isDirectory()) {
            throw new IOException(
                    "getTransactionsDumpPathFolder: failed to create inner"
                    + " transactions-dump folder for pid '" + pidName + "' at "
                    + inner);
        }
        return inner;
    }

    /**
     *
     * @param resourceName resource name
     * @param destinationPath destination folder path
     */
    public static final void extractFromJar(String resourceName, Path destinationPath) throws IOException {
        Path resourcePath = Paths.get(destinationPath.toString(), resourceName);
        if (fileExists(resourcePath)) {
            delete(resourcePath);
        }
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Stream<URL> resources = systemClassLoader.resources(resourceName);
        InputStream resourceAsStream;

        if (resources.count() <= 0) {
            System.out.println("FALLBACK");
            resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        } else {
            resourceAsStream = systemClassLoader.getResourceAsStream(resourceName);
        }
        FileUtils.copyInputStreamToFile(resourceAsStream, resourcePath.toFile());
    }

    /**
     * create project directory and folder structure using default parameters
     *
     * @throws IOException
     */
    public static final void initProjectFiles() throws IOException {

        //init project home
        if (!homeDirExists()) {
            createDir(getDefaultApplicationDirectoryPath());
            //Log.log(Level.INFO , "Home directory created");
        }

        if (!getDeletedWalletFolderPath().toFile().isDirectory()) {
            createDir(getDeletedWalletFolderPath());
        }

        if (!getTkmTempFolderPath().toFile().isDirectory()) {
            createDir(getTkmTempFolderPath());
        }

        //init logs directory
        if (!logsDirectoryExists()) {
            createDir(getDefaultLogsDirectoryPath());
            log.info("Logs directory created");

        }
        //init wallets home
        if (!walletDirExists()) {
            createDir(getDefaultWalletDirectoryPath());
            log.info("Created wallet directory");
        }

        //init publickeys directory
        if (!publicKeyDirExists()) {
            createDir(getPublicKeyDirectoryPath());
            log.info("Created public keys directory");
        }

        if (!getReferenceKeysDatabaseFolder().toFile().isDirectory()) {
            createDir(getReferenceKeysDatabaseFolder());
        }

        //init publickeys directory
        if (!transactionsDirExists()) {
            createDir(getTransactionsDirectoryPath());
            log.info("Created transactions directory");
        }

        //init ephemeral wallets home
        if (!walletEphemeralDirExists()) {
            createDir(getEphemeralWalletDirectoryPath());
        }

        if (!directoryExists(getSettingsPathFolder())) {
            FileHelper.createDir(getSettingsPathFolder());
        }

        Path tboxCacheDir = Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), FixedParameters.TRANSACTION_BOX_CACHING_FOLDER);
        if (!FileHelper.directoryExists(tboxCacheDir)) {
            createDir(tboxCacheDir);
            log.info("cache dir created");
        }

        Path globalStorageDir = getGlobalFolderPath();
        if (!FileHelper.directoryExists(globalStorageDir)) {
            createDir(globalStorageDir);
            log.info("globalStorageDir dir created");
        }

        Path userWalletFolder = Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), FixedParameters.USER_WALLETS_FOLDER);
        if (!FileHelper.directoryExists(userWalletFolder)) {
            createDir(userWalletFolder);
            log.info("user wallets dir created");
        }

        //Path txsDumpFolder = Paths.get(settingsFolder.toString(), FixedParameters.API_SETTINGS);
        if (!getTransactionsDumpPathFolder().toFile().isDirectory()) {
            FileHelper.createDir(getTransactionsDumpPathFolder());
        }

        if (!getSimulationPathFolder().toFile().isDirectory()) {
            FileHelper.createDir(getSimulationPathFolder());
        }

        if (!getHotmokaFilesDirectoryPath().toFile().isDirectory()) {
            FileHelper.createDir(getHotmokaFilesDirectoryPath());
        }

        //live state folder
        if (!directoryExists(FileHelper.getLiveStateFolter())) {
            createDir(FileHelper.getLiveStateFolter());
        }

        //reference key folder
        if (!directoryExists(getReferenceKeysDatabaseFolder())) {
            FileHelper.createDir(getReferenceKeysDatabaseFolder());
        }

        //extract hotmoka base jar
        //extractFromJar(DefaultInitParameters.TAKAMAKA_CODE_JAR_RESOURCE, getHotmokaFilesDirectoryPath());

        if (!getHotmokaTestDirectoryPath().toFile().isDirectory()) {
            FileHelper.createDir(getHotmokaTestDirectoryPath());
        }

        Path hotmokaTestDirectoryPath = getHotmokaTestDirectoryPath();
        //deleteFolderContent(hotmokaTestDirectoryPath.toFile());

        Path settingsFolder = getSettingsPathFolder();
        Path settingsBookmarksFile = Paths.get(settingsFolder.toString(), FixedParameters.SETTINGS_BOOKMARKS);
        if (!FileHelper.directoryExists(settingsFolder)) {
            createDir(settingsFolder);
            log.info("settingsWalletFolder wallets dir created");
        }

    }

    public static final Path getGlobalFolderPath() {
        Path globalStorageDir = Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), FixedParameters.GLOBAL_FOLDER);
        return globalStorageDir;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getGlobalFolderPath(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.GLOBAL_FOLDER);
    }

    public static final Path getJarStorage(String b64filename, FixedParameters.GLOBAL_FOLDER_FILES gff) {
        String fromB64ToHEX = TkmSignUtils.fromB64ToHEX(b64filename);
        Path globalStorageDir = Paths.get(FileHelper.getDefaultApplicationDirectoryPath().toString(), FixedParameters.GLOBAL_FOLDER, fromB64ToHEX + gff.getExtension());
        return globalStorageDir;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getJarStorage(String b64filename, FixedParameters.GLOBAL_FOLDER_FILES gff, Path appRoot) {
        String fromB64ToHEX = TkmSignUtils.fromB64ToHEX(b64filename);
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.GLOBAL_FOLDER, fromB64ToHEX + gff.getExtension());
    }

    public static final Path getQteslaReferenceFolder() {
        Path defaultApplicationDirectoryPath = getDefaultApplicationDirectoryPath();
        Path folder = Paths.get(defaultApplicationDirectoryPath.toString(), FixedParameters.REFERENCE_QTESLA_ADDR_FOLDER);
        return folder;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getQteslaReferenceFolder(Path appRoot) {
        return Paths.get(getDefaultApplicationDirectoryPath(appRoot).toString(), FixedParameters.REFERENCE_QTESLA_ADDR_FOLDER);
    }

    public static final Path getQteslaReferenceAddressFolder(String addr) throws ThreadSafeUtilsException {
        if (TkmTextUtils.isNullOrBlank(addr)) {
            log.error("NULL ADDRESS VALUE");
            throw new ThreadSafeUtilsException("NULL ADDRESS VALUE");
        }
        if (addr.length() < QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS) {
            log.error("BAD ADDRESS LENGHT");
            throw new ThreadSafeUtilsException("BAD ADDRESS LENGHT");
        }
        String head = addr.substring(0, QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS);
        return Paths.get(getQteslaReferenceFolder().toString(), head);
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path getQteslaReferenceAddressFolder(String addr, Path appRoot) throws ThreadSafeUtilsException {
        if (TkmTextUtils.isNullOrBlank(addr)) {
            log.error("NULL ADDRESS VALUE");
            throw new ThreadSafeUtilsException("NULL ADDRESS VALUE");
        }
        if (addr.length() < QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS) {
            log.error("BAD ADDRESS LENGHT");
            throw new ThreadSafeUtilsException("BAD ADDRESS LENGHT");
        }
        String head = addr.substring(0, QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS);
        return Paths.get(getQteslaReferenceFolder(appRoot).toString(), head);
    }

    public static final Path createIfNotExistQteslaReferenceFolder(String addr) throws ThreadSafeUtilsException, IOException {
        Path aFold = getQteslaReferenceAddressFolder(addr);
        if (aFold == null) {
            log.error("FOLDER ERROR");
            return null;
        }
        if (!directoryExists(aFold)) {
            createDir(aFold);
        }
        return aFold;
    }

    /** 0.10.0 — appRoot-aware overload. @since 0.10.0 */
    public static final Path createIfNotExistQteslaReferenceFolder(String addr, Path appRoot) throws ThreadSafeUtilsException, IOException {
        Path aFold = getQteslaReferenceAddressFolder(addr, appRoot);
        if (aFold == null) {
            log.error("FOLDER ERROR");
            return null;
        }
        if (!directoryExists(aFold)) {
            createDir(aFold);
        }
        return aFold;
    }

    private static Path getQteslaRefenceAddrFilePathInternal(String refQaddrFraction) throws WalletException, ThreadSafeUtilsException {
        Path qRefAddrFold = getQteslaReferenceAddressFolder(refQaddrFraction);
        if (qRefAddrFold == null | TkmTextUtils.isNullOrBlank(refQaddrFraction)) {
            log.error("ERROR CREATING FILE PATH qFold " + qRefAddrFold + " qAddrFr " + refQaddrFraction);
            throw new WalletException("ERROR CREATING FILE PATH qFold " + qRefAddrFold + " qAddrFr " + refQaddrFraction);
        }
        return Paths.get(qRefAddrFold.toString(), refQaddrFraction);
    }

    public static final String deflateInflateAddress(String addr, boolean deflate) throws IOException, WalletException, ThreadSafeUtilsException {
        if (deflate) {
            return saveQteslaAddress(addr);
        } else {
            return retriveQaddrByReference(addr);
        }
    }

    public static final String saveQteslaAddress(String qAddr) throws WalletException, ThreadSafeUtilsException, IOException {
        String refQAddrFraction;
        try {
            refQAddrFraction = TkmSignUtils.Hash256ToHex(qAddr);
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("CAN NOT CALCULATE HASH", ex);
            throw new WalletException("CAN NOT CALCULATE HASH", ex);
        }
        if (TkmTextUtils.isNullOrBlank(refQAddrFraction)) {
            log.error("ERROR IN REFERENCE ADDRESS");
            throw new WalletException("ERROR IN REFERENCE ADDRESS");
        }
        Path addrPath = createIfNotExistQteslaReferenceFolder(refQAddrFraction);
        if (addrPath == null) {
            log.error("ERROR CREATING ADDR PATH");
            throw new WalletException("ERROR IN ADDR PATH");
        }
        Path qRefFractAddrFilePath = getQteslaRefenceAddrFilePathInternal(refQAddrFraction);
        if (qRefFractAddrFilePath == null) {
            log.error("ERROR IN ADDR PATH");
            throw new WalletException("ERROR IN ADDR PATH");
        }
        if (!fileExists(qRefFractAddrFilePath)) {
            try {
                writeStringToFile(addrPath, refQAddrFraction, qAddr, true);
            } catch (IOException ex) {
                log.error("CAN NOT WRITE FILE!", ex);
                throw new WalletException("CAN NOT WRITE FILE!", ex);
            }
        } else {
            log.error("ADDRESS ALREADY PRESENT IN THE LIST");
        }
        return FixedParameters.REFERENCE_QTESLA_ADDR_PREFIX + refQAddrFraction;
    }

    public static final String getReferenceKeyStringNoSave(String qAddr) throws ThreadSafeUtilsException {
        String refQAddrFraction;
        try {
            refQAddrFraction = TkmSignUtils.Hash256ToHex(qAddr);
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("CAN NOT CALCULATE HASH", ex);
            throw new ThreadSafeUtilsException("CAN NOT CALCULATE HASH", ex);
        }
        return FixedParameters.REFERENCE_QTESLA_ADDR_PREFIX + refQAddrFraction;
    }

    //8e35c2cd3bf6641bdb0e2050b76932cbb2e6034a0ddacc1d9bea82a6ba57f7cf <- 64 char
    public static final String retriveQaddrByReference(String refAddr) throws IOException, WalletException, ThreadSafeUtilsException {
        int prefixLength = REFERENCE_QTESLA_ADDR_PREFIX.length();
        if (TkmTextUtils.isNullOrBlank(refAddr)) {
            log.error("NULL REFADDR");
            throw new ThreadSafeUtilsException("NULL REFADDR");
        }
        if (refAddr.length() != prefixLength + 64) {
            log.error("WRONG ADDRESS LENGTH, missing prefix?");
            throw new ThreadSafeUtilsException("WRONG ADDRESS LENGTH, missing prefix?");
        }
        if (!refAddr.contains(REFERENCE_QTESLA_ADDR_PREFIX)) {
            log.error("NOT A REFERENCE ADDRESS");
            throw new ThreadSafeUtilsException("NOT A REFERENCE ADDRESS");
        }
        String internalRefAddr = refAddr.substring(prefixLength, refAddr.length());
        Path qFile = getQteslaRefenceAddrFilePathInternal(internalRefAddr);
        if (!fileExists(qFile)) {
            log.error("MISSING QFILE, ADDRESS NOT IN LIST");
            throw new ThreadSafeUtilsException("MISSING QFILE, ADDRESS NOT IN LIST");
        }
        String readStringFromFile;
        readStringFromFile = readStringFromFile(qFile);
        return readStringFromFile;
    }

    /**
     *
     * @param file
     * @return true if exists and is a file
     */
    public static final boolean fileExists(Path file) {
        boolean exists = file.toFile().isFile();

        return exists;
    }

    /**
     *
     * @param filePointer
     * @return true if exists and is a directory
     */
    public static final boolean directoryExists(Path filePointer) {
        return filePointer.toFile().isDirectory();
    }

    /**
     * Write a string to file<br>
     * If file does not exist the string will be written to file and the
     * function return true.<br>
     * <pre>
     * <code>FileHelper.writeStringToFile(
     *              FileHelper.getDefaultApplicationDirectoryPath(),
     *              "TestFile.txt", "Only create, no overwrite", false)
     * </code></pre><br>
     * If file exists and overwite is specified the original file is deleted,
     * new one is created, the string is written in it and the function return
     * true.<br>
     * <pre>
     * <code>FileHelper.writeStringToFile(
     *              FileHelper.getDefaultApplicationDirectoryPath(),
     *              "TestFile.txt", "Create or Overwrite", true)
     * </code><br></pre>
     * <br>
     *
     * If file exists, overvrite is denied function terminate and return false
     *
     * @param directory destination directory
     * @param fileName file name
     * @param content string to be writed to file
     * @param overwrite true for overwriting content if file exists, ignored
     * otherwise
     * @return true if succesfully written
     * @throws IOException
     */
    public static final boolean writeStringToFile(Path directory, String fileName, String content, boolean overwrite) throws IOException {
        synchronized (KEYLOCK) {
            Path filePath = Paths.get(directory.toString(), fileName);
            log.info("File to be written " + filePath.toString());
            if (!fileExists(filePath)) {
                log.info("File does not exist");
                Files.createFile(filePath);
                log.info("New empty file created");
            } else {
                //file exist, checking overwrite flag
                if (overwrite) {
                    //log.info("Deleting obsolete content:" + fileName);
                    //filePath.toFile().delete();
                    log.info("Content deleted");
                    filePath.toFile().createNewFile();
                    log.info("New empty file created");
                } else {
                    // file exist no overwriting
                    log.info("File exist no overwriting, operation aborted");
                    return false;
                }
            }
            log.info("Writing content to file...");
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
            writer.write(content);
            writer.close();
            log.info("content written and file closed");
            return true;
        }
    }

    /**
     * Write a string to file<br>
     * If file does not exist the string will be written to file and the
     * function return true.<br>
     * <pre>
     * <code>FileHelper.writeStringToFile(
     *              FileHelper.getDefaultApplicationDirectoryPath(),
     *              "TestFile.txt", "Only create, no overwrite", false)
     * </code></pre><br>
     * If file exists and overwite is specified the original file is deleted,
     * new one is created, the string is written in it and the function return
     * true.<br>
     * <pre>
     * <code>FileHelper.writeStringToFile(
     *              FileHelper.getDefaultApplicationDirectoryPath(),
     *              "TestFile.txt", "Create or Overwrite", true)
     * </code><br></pre>
     * <br>
     *
     * If file exists, overvrite is denied function terminate and return false
     *
     * @param directory destination directory
     * @param fileName file name
     * @param content string to be writed to file
     * @param overwrite true for overwriting content if file exists, ignored
     * otherwise
     * @return true if succesfully written
     * @throws IOException
     */
    public static final boolean writeStringToFileUTF8(Path directory, String fileName, String content, boolean overwrite) throws IOException {
        synchronized (KEYLOCK) {
            Path filePath = Paths.get(directory.toString(), fileName);
            log.info("File to be written " + filePath.toString());
            if (!fileExists(filePath)) {
                log.info("File does not exist");
                Files.createFile(filePath);
                log.info("New empty file created");
            } else {
                //file exist, checking overwrite flag
                if (overwrite) {
                    //log.info("Deleting obsolete content:" + fileName);
                    //filePath.toFile().delete();
                    log.info("Content deleted");
                    filePath.toFile().createNewFile();
                    log.info("New empty file created");
                } else {
                    // file exist no overwriting
                    log.info("File exist no overwriting, operation aborted");
                    return false;
                }
            }
            log.info("Writing content to file...");
            byte[] utf8ByteContent = content.getBytes(StandardCharsets.UTF_8);
            Files.write(filePath, utf8ByteContent, StandardOpenOption.WRITE);
//            
//            
//            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
//            writer.write(content);
//            writer.close();
            log.info("content written and file closed");
            return true;
        }
    }

    /**
     * return the list of wallets inside folder path
     *
     * @param folderPath
     * @param filter
     * @return
     */
    public static final ArrayList<String> getWalletListInFolder(Path folderPath, String filter) {
        ArrayList<String> result = null;
        File folder = new File(folderPath.toString());
        File[] listOfFiles = folder.listFiles();
        String temp;
        result = new ArrayList<>();
        for (File f : listOfFiles) {
            if (f.isFile()) {
                if (!TkmTextUtils.isNullOrBlank(filter)) {
                    temp = f.getName().substring(0, f.getName().length() - WALLET_EXTENSION.length());
                    if (temp.toLowerCase().contains(filter.toLowerCase())) {
                        result.add(f.getName());
                    }
                } else {
                    result.add(f.getName());
                }
            }
        }

        return result;
    }

    /**
     * return the list of files inside folder path,
     *
     * @param folderPath
     * @param filter
     * @return
     */
    public static final ArrayList<String> getFilteredFileListInFolder(Path folderPath, String filter) {
        ArrayList<String> result = null;
        File folder = new File(folderPath.toString());
        File[] listOfFiles = folder.listFiles();
        String filename;
        result = new ArrayList<>();
        for (File f : listOfFiles) {
            if (f.isFile()) {
                if (!TkmTextUtils.isNullOrBlank(filter)) {
                    filename = f.getName();
                    if (filename.toLowerCase().contains(filter.toLowerCase())) {
                        result.add(f.getName());
                    }
                } else {
                    result.add(f.getName());
                }
            }
        }

        return result;
    }

    public static final ArrayList<String> getFilteredFileListInFolderByType(Path folderPath, String filter) {
        ArrayList<String> result = null;
        File folder = new File(folderPath.toString());
        File[] listOfFiles = folder.listFiles();
        String filename;
        result = new ArrayList<>();
        for (File f : listOfFiles) {
            if (f.isFile()) {
                if (!TkmTextUtils.isNullOrBlank(filter)) {
                    filename = f.getName();
                    if (filename.toLowerCase().endsWith(filter.toLowerCase())) {
                        result.add(f.getName());
                    }
                } else {
                    result.add(f.getName());
                }
            }
        }

        return result;
    }

    public static final ArrayList<String> getWalletList() {
        Path folderPath = FileHelper.getDefaultWalletDirectoryPath();
        ArrayList<String> result = null;
        File folder = new File(folderPath.toString());
        File[] listOfFiles = folder.listFiles();
        String fileName;
        result = new ArrayList<>();
        for (File f : listOfFiles) {
            if (f.isFile()) {
                fileName = f.getName().substring(0, f.getName().length() - WALLET_EXTENSION.length());
                result.add(fileName);
            }
        }
        return result;
    }

    /**
     * list file in the passed folder
     *
     * @param folderPath
     * @return
     */
    public static final File[] getFolderContent(Path folderPath) {
        File[] result = null;
        File folder = new File(folderPath.toString());
        result = folder.listFiles();
        return result;
    }

    /**
     * delete passed file or the folder and all its content, recursively. If f
     * is dir f is deleted
     *
     * @param f
     * @throws IOException
     */
    public static final void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    /**
     *
     * @param filePath
     */
    public static final void deleteSingleFile(Path filePath) throws IOException {
        // 0.10.0 H4 fix — promote silent log-only failure to a hard failure.
        // Prior code logged on !delete and on missing file, then returned;
        // callers had no way to tell whether the delete actually succeeded.
        // delete() returns false on legitimate failures (open file handles
        // on Windows, permission issues, ENOSPC, etc.) — all caller-relevant.
        if (!filePath.toFile().exists()) {
            throw new IOException(
                    "deleteSingleFile: file does not exist: " + filePath.toString());
        }
        boolean delete = filePath.toFile().delete();
        if (!delete) {
            throw new IOException(
                    "deleteSingleFile: delete() returned false for "
                    + filePath.toString()
                    + " (possible causes: file in use, permission denied, "
                    + "filesystem read-only, path is a non-empty directory)");
        }
    }

    /**
     * delete RECURSIVELY everithing in the path
     *
     * @param file
     * @throws IOException
     */
    public static final void delete(Path file) throws IOException {
        File f = new File(file.toString());
        if (f.isDirectory()) {
            throw new IOException("Failed, i can't delete a directory: " + f);
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    /**
     * recursively delete folder content
     *
     * TODO delete root folder?
     *
     * @param f
     * @throws IOException
     */
    public static final void deleteFolderContent(File f) throws IOException {
        if (f.isFile()) {
            log.error("not a folder, do nothing " + f.toString());
        }

        if (f.isDirectory()) {
            for (File listFile : f.listFiles()) {
                try {
                    delete(listFile);

                } catch (Exception e) {
                    log.error("delete folder error", e);
                    throw new IOException("delete folder error", e);
                }
            }
        }

    }

    /**
     * reads a string from the file indicated in the path if it exists
     *
     * @param file {@code Path}
     * @return
     * @throws FileNotFoundException
     */
    public static final String readStringFromFile(Path file) throws FileNotFoundException, IOException {
        if (!file.toFile().isFile()) {
            throw new FileNotFoundException();
        }
        String content = null;
        content = new String(Files.readAllBytes(file));
        return content;
    }

    /**
     * reads a string from the file indicated in the path if it exists
     *
     * @param file {@code Path}
     * @return
     * @throws FileNotFoundException
     */
    public static final String readStringFromFileUTF8(Path file) throws FileNotFoundException, IOException {

        if (!file.toFile().isFile()) {
            throw new FileNotFoundException();
        }
        String content = null;
        byte[] readAllBytes = Files.readAllBytes(file);
        content = new String(readAllBytes, StandardCharsets.UTF_8);

        return content;

    }

    /**
     * moves the target file/folder to the new destination and can overwrite the
     * existing file/folder if it exists
     *
     * @param sourcePath can be of type String/File/Path
     * @param targetPath can be of type String/File/Path
     * @param overwrite indicated whether to overwrite existing files/folders
     * @throws IOException
     */
    public static final void rename(Object sourcePath, Object targetPath, Boolean overwrite) throws IOException {
        File sourceFile = null, targetFile = null;
        Boolean success = false;

        if (sourcePath instanceof String && targetPath instanceof String) {
            sourceFile = new File((String) sourcePath);
            targetFile = new File((String) targetPath);
        } else if (sourcePath instanceof Path && targetPath instanceof Path) {
            sourceFile = ((Path) sourcePath).toFile();
            targetFile = ((Path) targetPath).toFile();
        } else if (sourcePath instanceof File && targetPath instanceof File) {
            sourceFile = (File) sourcePath;
            targetFile = (File) targetPath;
        }
        // 0.10.0 H3 fix — defend against unsupported types: prior code would
        // NPE on the renameTo call below if neither branch matched. Surface
        // the contract violation explicitly.
        if (sourceFile == null || targetFile == null) {
            throw new IOException(
                    "rename: unsupported source/target types — expected matching"
                    + " String/Path/File pair, got source="
                    + (sourcePath == null ? "null" : sourcePath.getClass().getName())
                    + " target="
                    + (targetPath == null ? "null" : targetPath.getClass().getName()));
        }
        // 0.10.0 H3 fix (additional) — honor overwrite=false explicitly. Prior
        // code only used `overwrite` to optionally pre-delete the target;
        // when overwrite=false and target existed, renameTo() on Linux
        // (POSIX rename(2) syscall) silently overwrites anyway, violating the
        // overwrite=false contract. Cross-platform-divergent silent behaviour.
        if (!overwrite && targetFile.exists()) {
            throw new IOException(
                    "rename: target exists and overwrite=false: "
                    + targetFile.getAbsolutePath()
                    + " — pass overwrite=true or delete the target explicitly.");
        }
        if (overwrite && targetFile.exists()) {
            delete(targetFile);
        }
        success = sourceFile.renameTo(targetFile);
        // 0.10.0 H3 fix — promote silent skip to a hard failure. Prior code
        // assigned success and discarded it, leaving callers unable to tell
        // whether the rename actually happened. renameTo returns false in
        // legitimate situations (cross-device move, target exists, permission
        // denied, etc.) — all of which are caller-relevant.
        if (!success) {
            throw new IOException(
                    "rename failed: " + sourceFile.getAbsolutePath()
                    + " -> " + targetFile.getAbsolutePath()
                    + " (renameTo returned false; possible causes: cross-device"
                    + " move, target exists with overwrite=false, permission"
                    + " denied, source missing)");
        }
    }

    /**
     * OVERWRITE
     *
     * @param sources
     * @param destination
     * @throws IOException
     */
    public static final void copy(Path sources, Path destination) throws IOException {
        Files.copy(sources, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Moves and renames any existing file or folder within the source epoch and
     * slot to the target epoch and slot, overwrites if file/folder already
     * exists
     *
     * @param sourceEpoch
     * @param sourceSlot
     * @param sourceFile
     * @param targetEpoch
     * @param targetSlot
     * @param targetFile
     * @throws Exception
     */
    public static final void moveToSlot(int sourceEpoch, int sourceSlot, String sourceFile, int targetEpoch, int targetSlot, String targetFile) throws Exception {
        Path sourcePath = null, targetPath = null;
        //if (StringUtils.isNullOrWhiteSpace(sourceFile) || StringUtils.isNullOrWhiteSpace(targetFile)) {
        if (org.apache.commons.lang3.StringUtils.isNoneBlank(sourceFile) || org.apache.commons.lang3.StringUtils.isNoneBlank(targetFile)) {
            log.error("Either source or target file has not been specified");
            return;
        }

        if (epochDirectoryExists(sourceEpoch) && slotDirectoryExists(sourceEpoch, sourceSlot)) {
            sourcePath = FileHelper.getBlockPath(0, 0, sourceFile);//getSlotDirectory(sourceSlot, sourceSlot);
            targetPath = FileHelper.getBlockPath(0, 1, targetFile);//getSlotDirectory(targetEpoch, targetSlot);

            if (!epochDirectoryExists(targetEpoch) || !slotDirectoryExists(targetEpoch, targetSlot)) {
                createSlotDirectory(targetEpoch, targetSlot);
                log.debug("Slot " + targetSlot + " created for epoch:" + targetEpoch);
            }

            rename(sourcePath, targetPath, true);
        } else {
            log.error("Could not find source folder.");
        }
    }

    public static final void deleteSettings(String option) throws IOException {
        Path settingsFolder = getSettingsPathFolder();
        Path settingsBookmarksFile = Paths.get(settingsFolder.toString(), option);
        FileHelper.delete(settingsBookmarksFile.toFile());
    }

    /**
     *
     * @param path
     * @param input
     */
    public static final void writeTransactionBoxToFile(Path path, Object input) throws IOException {
        try {
            FileOutputStream fout = null;
            fout = new FileOutputStream(path.toString());
            try ( ObjectOutputStream oos = new ObjectOutputStream(fout)) {
                oos.writeObject(input);
                oos.flush();
            } catch (IOException e) {
                throw new IOException("stream error", e);
            }
        } catch (IOException ex) {
            throw new IOException("file not found? ", ex);
        }

    }

    /**
     *
     * @param path
     * @param sithNumber
     * @param input
     */
    public static final void writeObjectToFile(Path path, String sithNumber, Object input) throws IOException {
        if (!path.toFile().isDirectory()) {
            createFolderAtPathIfNoneExist(path);
        }
        Path filePath = Paths.get(path.toString(), sithNumber);
        writeTransactionBoxToFile(filePath, input);

    }

    /**
     *
     * @param filePath
     * @return
     */
    public static final Object readObjectFromFile(Path filePath) throws IOException {
        Object result = null;
        if (filePath.toFile().isFile()) {
            try ( FileInputStream fileIn = new FileInputStream(filePath.toFile()); //
                      ObjectInputStream objectIn = new ObjectInputStream(fileIn)//
                    ) {
                result = objectIn.readObject();
            } catch (ClassNotFoundException | IOException ex) {
                throw new IOException("read object stream error", ex);
            }
        }

        return result;
    }

    /**
     *
     * @param filepath
     * @return
     */
    public static final Object readObjectFromFile(String filepath) throws IOException {
        Object result = null;
        try ( FileInputStream fileIn = new FileInputStream(filepath);  ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            result = objectIn.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            throw new IOException("stream read object error", ex);
        }
        return result;
    }

    /**
     *
     * @param epoch
     * @param slot
     * @param uid
     * @return false if cs is not consistent
     */
    public static final boolean testMandatoryStatusBean(int epoch, int slot, String uid) {
        if (TkmTextUtils.isNullOrBlank(uid)) {
            return false;
        }
        FixedParameters.HexKeyWriter[] kwTypes = FixedParameters.HexKeyWriter.values();
        Path slotDirectory = FileHelper.getSlotDirectory(epoch, slot);
        ConcurrentSkipListSet<Boolean> nuke = new ConcurrentSkipListSet<Boolean>();
        Arrays.stream(kwTypes).parallel()
                .forEach((FixedParameters.HexKeyWriter hkw) -> {
                    Path index = null;
                    switch (hkw) {
                        case ACCEPTED_BET:
                            index = Paths.get(slotDirectory.toString(), uid + "_." + hkw.name());
                            break;
                        case BALANCE:
                            index = Paths.get(slotDirectory.toString(), uid + "_." + hkw.name());
                            break;
                        case BLOCKS:
                            index = Paths.get(slotDirectory.toString(), uid + "_." + hkw.name());
                            break;
                        case NODE:
                            index = Paths.get(slotDirectory.toString(), uid + "_." + hkw.name());
                            break;
                        case OVER_THE_LIMIT:
                            index = Paths.get(slotDirectory.toString(), uid + "_." + hkw.name());
                            break;
                        case STAKE:
                            index = Paths.get(slotDirectory.toString(), uid + "_." + hkw.name());
                            break;
                        case STAKE_UNDO:
                            index = Paths.get(slotDirectory.toString(), uid + "_." + hkw.name());
                            break;
                        /*    
                        case TRANSACTION_REFERENCE:
                            index = Paths.get(slotDirectory.toString(), uid + "_." + hkw.name());
                            break;*/
                        default:
                            throw new RuntimeException("THIS SECTION MUST BE UPDATED TO TRACK CONSISTENCY OF DEFAULT STATE");
                    }
                    if (!fileExists(index)) {
                        log.error("DOES NOT EXIST " + index.toString());
                        log.error("FOUND INCOSISTENCY IN DEFAULT STATE PROPOSAL");
                        nuke.add(true);
                    }
                });
        if (nuke.contains(true)) {
            return false;
        } else {
            return true;
        }
    }

}
