/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 * Thrown by {@link io.takamaka.wallet.utils.WalletHelper#writePublicKey} when
 * a public-key export file already exists at the target path.
 *
 * <p>Refuses to silently overwrite or silently no-op. Callers that legitimately
 * want to replace the exported public-key file must explicitly delete the
 * existing file first or operate on a fresh path.
 *
 * <p>Historical note: prior to wallet-core 0.10.0, {@code writePublicKey}
 * silently failed to overwrite when the target file existed (it called
 * {@code FileHelper.writeStringToFile(..., overwrite=false)} and discarded
 * the boolean return), leaving the caller unable to detect the failure. Same
 * silent-corruption shape as the keystore writeKeyFile bug; promoted to a
 * hard failure in 0.10.0.
 *
 * @since 0.10.0
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class PublicKeyFileExistsException extends WalletException {

    public PublicKeyFileExistsException() {
        super();
    }

    public PublicKeyFileExistsException(String msg) {
        super(msg);
    }

    public PublicKeyFileExistsException(Throwable er) {
        super(er);
    }

    public PublicKeyFileExistsException(String msg, Throwable er) {
        super(msg, er);
    }
}
