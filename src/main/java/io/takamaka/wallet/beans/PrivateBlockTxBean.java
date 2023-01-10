/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import io.takamaka.wallet.utils.KeyContexts;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author giovanni.antino@h2tcoin.com
 */
public class PrivateBlockTxBean implements Serializable, Comparable<PrivateBlockTxBean> {

    private TransactionBean tb;
    private String singleInclusionTransactionHash;
    private KeyContexts.InternalBlockTransactionState transactionValidity;

    public PrivateBlockTxBean() {
    }
    
    /**
     * @return the tb
     */
    public TransactionBean getTb() {
        return tb;
    }

    /**
     * @return the transactionValidity
     */
    public KeyContexts.InternalBlockTransactionState getTransactionValidity() {
        return transactionValidity;
    }

    /**
     * @param tb the tb to set
     */
    public void setTb(TransactionBean tb) {
        this.tb = tb;
    }

    /**
     * @param transactionValidity the transactionValidity to set
     */
    public void setTransactionValidity(KeyContexts.InternalBlockTransactionState transactionValidity) {
        this.transactionValidity = transactionValidity;
    }

    public String getSingleInclusionTransactionHash() {
        return singleInclusionTransactionHash;
    }

    public void setSingleInclusionTransactionHash(String singleInclusionTransactionHash) {
        this.singleInclusionTransactionHash = singleInclusionTransactionHash;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.tb);
        hash = 67 * hash + Objects.hashCode(this.singleInclusionTransactionHash);
        hash = 67 * hash + Objects.hashCode(this.transactionValidity);
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
        final PrivateBlockTxBean other = (PrivateBlockTxBean) obj;
        if (!Objects.equals(this.singleInclusionTransactionHash, other.singleInclusionTransactionHash)) {
            return false;
        }
        if (!Objects.equals(this.tb, other.tb)) {
            return false;
        }
        return this.transactionValidity == other.transactionValidity;
    }
    
    @Override
    public int compareTo(PrivateBlockTxBean t) {
        return this.singleInclusionTransactionHash.compareTo(t.singleInclusionTransactionHash);
    }
    
}
