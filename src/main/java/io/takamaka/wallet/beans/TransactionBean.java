/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import io.takamaka.wallet.utils.KeyContexts.WalletCypher;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
public class TransactionBean implements Serializable{
    private String publicKey;
    private String signature;
    private String message;
    private String randomSeed;
    private WalletCypher walletCypher;

    public TransactionBean() {
    }

    public String getRandomSeed() {
        return randomSeed;
    }

    /**
     *
     * @param randomSeed
     */
    public void setRandomSeed(String randomSeed) {
        this.randomSeed = randomSeed;
    }

    /**
     * @return the publicKey
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param publicKey the publicKey to set
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the walletCypher
     */
    public WalletCypher getWalletCypher() {
        return walletCypher;
    }

    /**
     * @param walletCypher the walletCypher to set
     */
    public void setWalletCypher(WalletCypher walletCypher) {
        this.walletCypher = walletCypher;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.publicKey);
        hash = 23 * hash + Objects.hashCode(this.signature);
        hash = 23 * hash + Objects.hashCode(this.message);
        hash = 23 * hash + Objects.hashCode(this.randomSeed);
        hash = 23 * hash + Objects.hashCode(this.walletCypher);
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
        final TransactionBean other = (TransactionBean) obj;
        if (!Objects.equals(this.publicKey, other.publicKey)) {
            return false;
        }
        if (!Objects.equals(this.signature, other.signature)) {
            return false;
        }
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        if (!Objects.equals(this.randomSeed, other.randomSeed)) {
            return false;
        }
        if (this.walletCypher != other.walletCypher) {
            return false;
        }
        return true;
    }
}
