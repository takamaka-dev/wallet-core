/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class InclusionHashCreationException extends ThreadSafeUtilsException {

    public InclusionHashCreationException() {
        super();
    }

    public InclusionHashCreationException(String msg) {
        super(msg);
    }

    public InclusionHashCreationException(Throwable er) {
        super(er);
    }

    public InclusionHashCreationException(String msg,Throwable er) {
        super(msg, er);
    }
}