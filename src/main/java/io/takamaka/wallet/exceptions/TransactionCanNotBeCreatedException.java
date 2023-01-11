/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class TransactionCanNotBeCreatedException extends WalletException {

    public TransactionCanNotBeCreatedException() {
        super();
    }

    public TransactionCanNotBeCreatedException(String msg) {
        super(msg);
    }

    public TransactionCanNotBeCreatedException(Throwable er) {
        super(er);
    }

    public TransactionCanNotBeCreatedException(String msg,Throwable er) {
        super(msg, er);
    }
}
