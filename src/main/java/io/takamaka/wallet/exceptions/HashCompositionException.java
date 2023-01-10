/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
 */
public class HashCompositionException extends ThreadSafeUtilsException{

    public HashCompositionException() {
        super();
    }

    public HashCompositionException(String msg) {
        super(msg);
    }

    public HashCompositionException(Throwable er) {
        super(er);
    }

    public HashCompositionException(String msg, Throwable er) {
        super(msg, er);
    }
}
