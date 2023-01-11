/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class WalletEmptySeedException extends WalletException {

    public WalletEmptySeedException() {
        super();
    }

    public WalletEmptySeedException(String msg) {
        super(msg);
    }

    public WalletEmptySeedException(Throwable er) {
        super(er);
    }

    public WalletEmptySeedException(String msg,Throwable er) {
        super(msg, er);
    }
}
