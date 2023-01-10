/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import io.takamaka.wallet.utils.KeyContexts;
import java.io.Serializable;

/**
 *
 * @author francesco.pasetto@h2tcoin.com
 */
public class EncKeyBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String version = KeyContexts.WALLET_CURRENT_VERSION;
    private String algorithm;
    private byte[][] wallet;

    public EncKeyBean() {
    }

    public EncKeyBean(String algorithm, byte[][] wallet) {
        this.algorithm = algorithm;
        this.wallet = wallet;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public byte[][] getWallet() {
        return wallet;
    }

    public void setWallet(byte[][] wallet) {
        this.wallet = wallet;
    }

}
