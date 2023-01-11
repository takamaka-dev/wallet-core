/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class ThreadSafeUtilsException extends Exception{
     public ThreadSafeUtilsException() {
        super();
    }

    public ThreadSafeUtilsException(String msg) {
        super(msg);
    }

    public ThreadSafeUtilsException(Throwable er) {
        super(er);
    }

    public ThreadSafeUtilsException(String msg,Throwable er) {
        super(msg, er);
    }
}
