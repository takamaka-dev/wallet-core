/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class WalletBurnedException extends WalletException {

    public WalletBurnedException() {
        super();
    }

    public WalletBurnedException(String msg) {
        super(msg);
    }

    public WalletBurnedException(Throwable er) {
        super(er);
    }

    public WalletBurnedException(String msg,Throwable er) {
        super(msg, er);
    }
}
