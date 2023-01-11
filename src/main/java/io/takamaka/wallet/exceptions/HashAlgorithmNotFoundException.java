/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class HashAlgorithmNotFoundException extends ThreadSafeUtilsException{

    public HashAlgorithmNotFoundException() {
        super();
    }

    public HashAlgorithmNotFoundException(String msg) {
        super(msg);
    }

    public HashAlgorithmNotFoundException(Throwable er) {
        super(er);
    }

    public HashAlgorithmNotFoundException(String msg, Throwable er) {
        super(msg, er);
    }
}
