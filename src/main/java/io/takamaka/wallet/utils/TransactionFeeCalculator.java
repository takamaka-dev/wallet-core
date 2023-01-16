/*
 * Copyright 2023 AiliA SA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.beans.FeeBean;
import io.takamaka.wallet.beans.TransactionBox;
import io.takamaka.wallet.exceptions.WalletException;
import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author giovanni
 */
@Slf4j
public class TransactionFeeCalculator {

    public static FeeBean getFeeBean(TransactionBox tbox) throws WalletException {
        FeeBean ret = new FeeBean();
        //System.out.println(tbox.getTransactionJson());
        if (tbox != null) {
            if (tbox.isValid()) {
                ret.setSith(tbox.getSingleInclusionTransactionHash());
                ret.setAddr(tbox.from());
                ret.setHexAddr(TkmSignUtils.fromB64UrlToHEX(tbox.from()));
                switch (tbox.type()) {
                    case ASSIGN_OVERFLOW:
                    case BLOB:
                    case DEREGISTER_OVERFLOW:
                    case PAY:
                    case STAKE:
                    case STAKE_UNDO:
                    case UNASSIGN_OVERFLOW:
                    case REGISTER_MAIN:
                    case REGISTER_OVERFLOW:
                    case S_CONTRACT_DEPLOY:
                    case DEREGISTER_MAIN:
                    case S_CREATE_ACCOUNT:
                    case S_CONTRACT_CALL:
                        ret.setCpu(BigInteger.ZERO);
                        ret.setMemory(BigInteger.ZERO);
                        ret.setDisk(new BigInteger(tbox.getTransactionJson().length() + "")
                                .multiply(DefaultInitParameters.DISK_SCALE)
                                .multiply(DefaultInitParameters.TARGET_CLIENT_NUMBER_MAX_BI)
                                .multiply(DefaultInitParameters.YEARS_MOORE_LAW)
                                .multiply(DefaultInitParameters.FEE_SCALE_MULT)
                                .divide(DefaultInitParameters.FEE_SCALE_DIV)
                        );
                        break;

                    //case CONTRACT_DEPLOY:
                    case S_CONTRACT_INSTANCE:
                        log.error("not yet implemented");
                        throw new WalletException("not implemented " + KeyContexts.TransactionType.S_CONTRACT_INSTANCE.name());

                    case BLOCK_HASH:
                    case BLOCK:
                    default:
                        log.error("not yet implemented " + tbox.type().name());
                        throw new WalletException("not implemented " + tbox.type().name());

                }
            } else {
                throw new WalletException("invalid tbox " + tbox.getTransactionJson());
            }
        } else {
            throw new WalletException("null tbox");
        }
        return ret;
    }

    public static final BigDecimal getCostInTK(BigInteger cpuCost, BigInteger memCost, BigInteger diskCost) {
        return new BigDecimal((cpuCost.add(memCost.add(diskCost)))).movePointLeft(DefaultInitParameters.NUMBER_OF_ZEROS);
    }

    public static final BigDecimal getCostInTK(FeeBean fb) {
        return getCostInTK(fb.getCpu(), fb.getMemory(), fb.getDisk());
    }

}
