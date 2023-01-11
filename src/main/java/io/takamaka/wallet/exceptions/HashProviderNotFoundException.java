/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class HashProviderNotFoundException extends ThreadSafeUtilsException{

    public HashProviderNotFoundException() {
        super();
    }

    public HashProviderNotFoundException(String msg) {
        super(msg);
    }

    public HashProviderNotFoundException(Throwable er) {
        super(er);
    }

    public HashProviderNotFoundException(String msg, Throwable er) {
        super(msg, er);
    }
}
