/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import io.takamaka.wallet.utils.KeyContexts;
import java.util.Objects;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class InternalTransactionSyntaxBean {
    
    private boolean validSyntax;
    private KeyContexts.InternalTransactionSyntaxValidity itsv;
    private String extendedMessage;

    public boolean isValidSyntax() {
        return validSyntax;
    }

    public void setValidSyntax(boolean validSyntax) {
        this.validSyntax = validSyntax;
    }

    public KeyContexts.InternalTransactionSyntaxValidity getItsv() {
        return itsv;
    }

    public void setItsv(KeyContexts.InternalTransactionSyntaxValidity itsv) {
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
        hash = 79 * hash + (this.validSyntax ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.itsv);
        hash = 79 * hash + Objects.hashCode(this.extendedMessage);
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
        final InternalTransactionSyntaxBean other = (InternalTransactionSyntaxBean) obj;
        if (this.validSyntax != other.validSyntax) {
            return false;
        }
        if (!Objects.equals(this.extendedMessage, other.extendedMessage)) {
            return false;
        }
        return this.itsv == other.itsv;
    }
    
    
}
