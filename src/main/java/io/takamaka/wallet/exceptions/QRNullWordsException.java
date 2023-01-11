/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.exceptions;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class QRNullWordsException extends QRException {

    public QRNullWordsException() {
    }

    public QRNullWordsException(String message) {
        super(message);
    }

    public QRNullWordsException(String message, Throwable cause) {
        super(message, cause);
    }

    public QRNullWordsException(Throwable cause) {
        super(cause);
    }

    public QRNullWordsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
