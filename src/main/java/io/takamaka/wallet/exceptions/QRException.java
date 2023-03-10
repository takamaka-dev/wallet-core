/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class QRException extends Exception {

    public QRException() {
    }

    public QRException(String message) {
        super(message);
    }

    public QRException(String message, Throwable cause) {
        super(message, cause);
    }

    public QRException(Throwable cause) {
        super(cause);
    }

    public QRException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
