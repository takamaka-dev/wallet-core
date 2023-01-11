/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class NullInternalTransactionBeanException extends ThreadSafeUtilsException{

    public NullInternalTransactionBeanException() {
        super();
    }

    public NullInternalTransactionBeanException(String msg) {
        super(msg);
    }

    public NullInternalTransactionBeanException(Throwable er) {
        super(er);
    }

    public NullInternalTransactionBeanException(String msg, Throwable er) {
        super(msg, er);
    }
}
