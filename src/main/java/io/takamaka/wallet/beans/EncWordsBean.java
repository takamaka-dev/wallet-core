/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.exceptions.QRNotFromJsonException;
import io.takamaka.wallet.exceptions.QRNullWordsException;
import io.takamaka.wallet.utils.KeyContexts;
import io.takamaka.wallet.utils.TkmSignUtils;
import io.takamaka.wallet.utils.TkmTextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author giovanni.antino@h2tcoin.com
 */
@Slf4j
public class EncWordsBean {

    private EncKeyBean eb;
    private KeyContexts.QrType qType;
    private KeyContexts.CrcType cType;
    private transient String jsonReadedCrc;

    public EncKeyBean getEb() {
        return eb;
    }

    public void setEb(EncKeyBean eb) {
        this.eb = eb;
    }

    public String getCrc() {
        try {
            switch (cType) {
                case TYPE_1:
                    return TkmSignUtils.Hash256ToHex(TkmTextUtils.toJson(eb));

                default:
                    return null;
            }
        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException ex) {
            log.error("CRC error", ex);
            return null;
        }
    }

    public String getqType() {
        return qType.name();
    }

    public void setqType(KeyContexts.QrType qType) {
        this.qType = qType;
    }

    public String getcType() {
        return cType.getCRC();
    }

    public void setcType(KeyContexts.CrcType cType) {
        this.cType = cType;
    }

    @JsonIgnore
    public boolean isValid() throws QRNotFromJsonException, QRNullWordsException {
        if (TkmTextUtils.isNullOrBlank(jsonReadedCrc)) {
            throw new QRNotFromJsonException("null jsonReadedCrc");
        }
        if (TkmTextUtils.isNullOrBlank(TkmTextUtils.toJson(eb))) {
            throw new QRNullWordsException("null or empty words");
        }
        return jsonReadedCrc.equals(getCrc());
    }

    @JsonIgnore
    public String getJsonReadedCrc() {
        return jsonReadedCrc;
    }

    @JsonIgnore
    public void setJsonReadedCrc(String jsonReadedCrc) {
        this.jsonReadedCrc = jsonReadedCrc;
    }

}
