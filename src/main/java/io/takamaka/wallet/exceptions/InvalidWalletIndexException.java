/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author giovanni
 */
public class InvalidWalletIndexException extends WalletException {

    public InvalidWalletIndexException() {
        super();
    }

    public InvalidWalletIndexException(String msg) {
        super(msg);
    }

    public InvalidWalletIndexException(Throwable er) {
        super(er);
    }

    public InvalidWalletIndexException(String msg,Throwable er) {
        super(msg, er);
    }
}
