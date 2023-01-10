/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
 */
public class UnlockWalletException extends WalletException {

    public UnlockWalletException() {
        super();
    }

    public UnlockWalletException(String msg) {
        super(msg);
    }

    public UnlockWalletException(Throwable er) {
        super(er);
    }

    public UnlockWalletException(String msg,Throwable er) {
        super(msg, er);
    }
}
