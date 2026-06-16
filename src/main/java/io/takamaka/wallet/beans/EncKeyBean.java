/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.takamaka.wallet.utils.KeyContexts;
import java.io.Serializable;

/**
 *
 * @author francesco.pasetto@takamaka.io
 */
public class EncKeyBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String version = KeyContexts.WALLET_CURRENT_VERSION;
    private String algorithm;
    private byte[][] wallet;

    /**
     * DR-009 — v2 keystore header parameters. These are authenticated cleartext
     * (bound as GCM AAD on write/read). They are {@code NON_DEFAULT}-included so
     * a legacy v1 bean ({@code kdf=null, salt=null, iterations=0}) serializes
     * <b>byte-identically</b> to pre-DR-009 output — the v1 on-disk format is
     * unchanged. Populated only when {@code algorithm == WALLET_JSON_AES_V2}.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String kdf;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private byte[] salt;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int iterations;

    public EncKeyBean() {
    }

    public EncKeyBean(String algorithm, byte[][] wallet) {
        this.algorithm = algorithm;
        this.wallet = wallet;
    }

    /** DR-009 — v2 constructor carrying the authenticated KDF header params. */
    public EncKeyBean(String algorithm, byte[][] wallet, String kdf, byte[] salt, int iterations) {
        this.algorithm = algorithm;
        this.wallet = wallet;
        this.kdf = kdf;
        this.salt = salt;
        this.iterations = iterations;
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

    public String getKdf() {
        return kdf;
    }

    public void setKdf(String kdf) {
        this.kdf = kdf;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

}
