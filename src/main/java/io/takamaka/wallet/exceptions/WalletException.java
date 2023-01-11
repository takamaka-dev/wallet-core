/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class WalletException extends Exception {

    public WalletException() {
        super();
    }

    public WalletException(String msg) {
        super(msg);
    }

    public WalletException(Throwable er) {
        super(er);
    }

    public WalletException(String msg, Throwable er) {
        super(msg, er);
    }
}
