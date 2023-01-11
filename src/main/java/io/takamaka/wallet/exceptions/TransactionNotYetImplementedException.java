/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class TransactionNotYetImplementedException extends ThreadSafeUtilsException {

    public TransactionNotYetImplementedException() {
        super();
    }

    public TransactionNotYetImplementedException(String msg) {
        super(msg);
    }

    public TransactionNotYetImplementedException(Throwable er) {
        super(er);
    }

    public TransactionNotYetImplementedException(String msg,Throwable er) {
        super(msg, er);
    }
}
