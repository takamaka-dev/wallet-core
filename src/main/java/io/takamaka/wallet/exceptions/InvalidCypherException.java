/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class InvalidCypherException extends WalletException {

    public InvalidCypherException() {
        super();
    }

    public InvalidCypherException(String msg) {
        super(msg);
    }

    public InvalidCypherException(Throwable er) {
        super(er);
    }

    public InvalidCypherException(String msg,Throwable er) {
        super(msg, er);
    }
}
