/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.TkmSignUtils;
import java.io.Serializable;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author francesco.pasetto@takamaka.io
 */
@Slf4j
public class PublicKeyBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String hashHex;
    private KeyContexts.WalletCypher cypher;
    private String version;
    private String publicKey;

    public PublicKeyBean() {
    }

    public PublicKeyBean(KeyContexts.WalletCypher cypher, String version, String publicKey) {
        try {
            this.hashHex = TkmSignUtils.Hash256ToHex(publicKey);
            this.cypher = cypher;
            this.version = version;
            this.publicKey = publicKey;
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("public key bean instantiation error", ex);
        }
    }

    public String getHashHex() {
        return hashHex;
    }

    public void setHashHex(String hashHex) {
        this.hashHex = hashHex;
    }

    public KeyContexts.WalletCypher getCypher() {
        return cypher;
    }

    public void setCypher(KeyContexts.WalletCypher cypher) {
        this.cypher = cypher;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.hashHex);
        hash = 83 * hash + Objects.hashCode(this.publicKey);
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
        final PublicKeyBean other = (PublicKeyBean) obj;
        if (!Objects.equals(this.hashHex, other.hashHex)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.publicKey, other.publicKey)) {
            return false;
        }
        if (this.cypher != other.cypher) {
            return false;
        }
        return true;
    }

}
