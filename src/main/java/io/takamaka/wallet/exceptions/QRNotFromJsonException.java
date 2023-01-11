/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class QRNotFromJsonException extends QRException {

    public QRNotFromJsonException() {
    }

    public QRNotFromJsonException(String message) {
        super(message);
    }

    public QRNotFromJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public QRNotFromJsonException(Throwable cause) {
        super(cause);
    }

    public QRNotFromJsonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
