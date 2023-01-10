/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
 */
public class HashEncodeException extends ThreadSafeUtilsException{

    public HashEncodeException() {
        super();
    }

    public HashEncodeException(String msg) {
        super(msg);
    }

    public HashEncodeException(Throwable er) {
        super(er);
    }

    public HashEncodeException(String msg, Throwable er) {
        super(msg, er);
    }
}
