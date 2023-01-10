/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
 */
public class KeyDecodeException extends ThreadSafeUtilsException{

    public KeyDecodeException() {
        super();
    }

    public KeyDecodeException(String msg) {
        super(msg);
    }

    public KeyDecodeException(Throwable er) {
        super(er);
    }

    public KeyDecodeException(String msg, Throwable er) {
        super(msg, er);
    }
}
