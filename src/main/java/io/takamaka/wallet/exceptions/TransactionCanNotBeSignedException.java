/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Iris Dimni iris.dimni@takamaka.io
 */
public class TransactionCanNotBeSignedException extends WalletException {

    /**
     *
     */
    public TransactionCanNotBeSignedException() {
        super();
    }

    /**
     *
     * @param message
     */
    public TransactionCanNotBeSignedException(String message) {
        super(message);
    }

    /**
     *
     * @param err
     */
    public TransactionCanNotBeSignedException(Throwable err) {
        super(err);
    }

    /**
     *
     * @param message
     * @param err
     */
    public TransactionCanNotBeSignedException(String message, Throwable err) {
        super(message, err);
    }
}