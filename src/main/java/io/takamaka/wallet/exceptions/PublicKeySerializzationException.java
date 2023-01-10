/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
 */
public class PublicKeySerializzationException extends WalletException {

    public PublicKeySerializzationException() {
        super();
    }

    public PublicKeySerializzationException(String msg) {
        super(msg);
    }

    public PublicKeySerializzationException(Throwable er) {
        super(er);
    }

    public PublicKeySerializzationException(String msg,Throwable er) {
        super(msg, er);
    }
}
