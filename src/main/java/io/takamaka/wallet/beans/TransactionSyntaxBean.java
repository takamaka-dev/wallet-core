/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import io.takamaka.wallet.utils.KeyContexts;
import java.util.Objects;

/**
 *
 * @author Iris Dimni iris.dimni@takamaka.io
 */
public final class TransactionSyntaxBean {
    private boolean validSyntax;
    private KeyContexts.TransactionSyntaxValidity itsv;
    private String extendedMessage;

    public boolean isValidSyntax() {
        return validSyntax;
    }

    public void setValidSyntax(boolean validSyntax) {
        this.validSyntax = validSyntax;
    }

    public KeyContexts.TransactionSyntaxValidity getItsv() {
        return itsv;
    }

    public void setItsv(KeyContexts.TransactionSyntaxValidity itsv) {
        this.itsv = itsv;
    }

    public String getExtendedMessage() {
        return extendedMessage;
    }

    public void setExtendedMessage(String extendedMessage) {
        this.extendedMessage = extendedMessage;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.validSyntax ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.itsv);
        hash = 71 * hash + Objects.hashCode(this.extendedMessage);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TransactionSyntaxBean other = (TransactionSyntaxBean) obj;
        if (this.validSyntax != other.validSyntax) {
            return false;
        }
        if (!Objects.equals(this.extendedMessage, other.extendedMessage)) {
            return false;
        }
        return this.itsv == other.itsv;
    }
    
    
}
