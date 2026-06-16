/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 * Thrown by {@link io.takamaka.wallet.utils.WalletHelper#writeKeyFile} (and
 * any caller in the keystore family) when a keystore file already exists at
 * the target path.
 *
 * <p>Refuses to silently overwrite or silently no-op. Callers that legitimately
 * want to replace the keystore must explicitly delete the existing file first
 * (test code) or operate on a fresh path (production code).
 *
 * <p>Historical note: prior to wallet-core 0.10.0, {@code writeKeyFile}
 * silently no-op'd in this case while still returning the path as if write had
 * succeeded — the caller's subsequent reads would see the OLD keystore
 * contents (different keypair) without any error signal. Promoted to a hard
 * failure in 0.10.0 to eliminate the silent-corruption footgun.
 *
 * @since 0.10.0
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class KeystoreFileExistsException extends WalletException {

    public KeystoreFileExistsException() {
        super();
    }

    public KeystoreFileExistsException(String msg) {
        super(msg);
    }

    public KeystoreFileExistsException(Throwable er) {
        super(er);
    }

    public KeystoreFileExistsException(String msg, Throwable er) {
        super(msg, er);
    }
}
