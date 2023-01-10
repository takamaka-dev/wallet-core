/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.beans.ExtendedTransactionBox;
import io.takamaka.wallet.beans.InternalTransactionBean;
import io.takamaka.wallet.beans.InternalTransactionSyntaxBean;
import io.takamaka.wallet.beans.TransactionBean;
import io.takamaka.wallet.beans.TransactionBox;
import io.takamaka.wallet.beans.TransactionSyntaxBean;
import io.takamaka.wallet.exceptions.WalletException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Iris Dimni <iris.dimni at takamaka.io>
 */
@Slf4j
public class TransactionUtils {

    /**
     * method takes a TransactionBean object in input and returns true if the
     * parameter and its internal fields are not null or empty, returns false
     * otherwise
     *
     * @param input
     * @return
     */
    public static TransactionSyntaxBean isTransactionBeanValid(TransactionBean input) {
        TransactionSyntaxBean result = new TransactionSyntaxBean();
        result.setItsv(KeyContexts.TransactionSyntaxValidity.NULL_TB);
        result.setValidSyntax(false);

        if (input == null) {
            return result;
        }
        result.setItsv(KeyContexts.TransactionSyntaxValidity.INVALID_FIELD_VALUE);
        if (TkmTextUtils.isNullOrBlank(input.getPublicKey())
                || TkmTextUtils.isNullOrBlank(input.getMessage())
                || TkmTextUtils.isNullOrBlank(input.getRandomSeed())
                || TkmTextUtils.isNullOrBlank(input.getSignature())) {
            result.setValidSyntax(false);
            return result;
        }
        boolean itbValid = false;
        try {
            InternalTransactionBean itb = TkmTextUtils.internalTransactionBeanFromJson(input.getMessage());
            InternalTransactionSyntaxBean internalTransactionBeanValid = isInternalTransactionBeanValid(itb);
            itbValid = internalTransactionBeanValid.isValidSyntax();
        } catch (Exception e) {
            log.error("Invalid json " + e.getLocalizedMessage());
            return result;
        }
        result.setValidSyntax(true & itbValid);
        return result;
    }

    /**
     * method takes a InternalTransactionBean object in input and returns true
     * if the parameter and its mandatory internal fields are not null or empty,
     * returns false otherwise
     *
     * @param input
     * @return
     */
    public static InternalTransactionSyntaxBean isInternalTransactionBeanValid(InternalTransactionBean input) {
        InternalTransactionSyntaxBean result = new InternalTransactionSyntaxBean();
        result.setItsv(KeyContexts.InternalTransactionSyntaxValidity.NULL_ITB);
        result.setValidSyntax(false);

        if (input == null) {
            return result;
        }

        result.setItsv(KeyContexts.InternalTransactionSyntaxValidity.INVALID_FIELD_VALUE);
        if (input.getTransactionType() == null
                || input.getNotBefore() == null
                || TkmTextUtils.isNullOrBlank(input.getTransactionHash())) {
            return result;
        } else {
            switch (input.getTransactionType()) {
                case ASSIGN_OVERFLOW:
                    if (TkmTextUtils.isNullOrBlank(input.getTo())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    } else {
                        //to must be a valid url64
                        if (!ArrayUtils.contains(DefaultInitParameters.TO_DATA_LENGTH_WHITELIST, input.getTo().length())) {
                            //F.r("invalid length");
                            return result;
                        }
                        String toB64String = TkmSignUtils.fromB64UrlToHEX(input.getTo());
                        //F.r("decode data: " + toB64String);
                        if (TkmTextUtils.isNullOrBlank(toB64String)) {
                            log.error("MORTE!!!!! " + input.getTo());
                            return result;
                        }
                    }
                    break;
                case UNASSIGN_OVERFLOW:
                    if (TkmTextUtils.isNullOrBlank(input.getTo())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    } else {
                        //to must be a valid url64
                        if (!ArrayUtils.contains(DefaultInitParameters.TO_DATA_LENGTH_WHITELIST, input.getTo().length())) {
                            //F.r("invalid length");
                            return result;
                        }
                        String toB64String = TkmSignUtils.fromB64UrlToHEX(input.getTo());
                        //F.r("decode data: " + toB64String);
                        if (TkmTextUtils.isNullOrBlank(toB64String)) {
                            log.error("MORTE!!!!! " + input.getTo());
                            return result;
                        }
                    }
                    break;

                case BLOB:
                    if (TkmTextUtils.isNullOrBlank(input.getMessage())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    }
                    break;
                case BLOCK:
                    //This type of transaction can't appear inside an InternalTransactionBean
                    result.setExtendedMessage("Found incompatible transaction type for an InternalTransactionBean: BLOCK");
                    return result;
                case BLOCK_HASH:
                    if (TkmTextUtils.isNullOrBlank(input.getMessage())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())
                            || input.getEpoch() == null
                            || input.getSlot() == null) {
                        return result;
                    }
                    break;
                case COINBASE:
                    if (TkmTextUtils.isNullOrBlank(input.getTo())
                            || input.getEpoch() == null
                            || input.getSlot() == null
                            || input.getGreenValue() == null) {
                        return result;
                    } else {
                        //to must be a valid url64
                        if (!ArrayUtils.contains(DefaultInitParameters.TO_DATA_LENGTH_WHITELIST, input.getTo().length())) {
                            //F.r("invalid length");
                            return result;
                        }
                        String toB64String = TkmSignUtils.fromB64UrlToHEX(input.getTo());
                        //F.r("decode data: " + toB64String);
                        if (TkmTextUtils.isNullOrBlank(toB64String)) {
                            log.error("MORTE!!!!! " + input.getTo());
                            return result;
                        }
                    }
                    break;
                case S_CONTRACT_DEPLOY:
                    if (TkmTextUtils.isNullOrBlank(input.getMessage())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    }
                    break;
                case S_CONTRACT_CALL:
                case S_CONTRACT_INSTANCE:
                    if (TkmTextUtils.isNullOrBlank(input.getMessage())
                            //|| TkmTextUtils.isNullOrBlank(input.getTo())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    }
                    break;
                case DECLARATION:
                    if (TkmTextUtils.isNullOrBlank(input.getTo())
                            || (input.getGreenValue() == null && input.getRedValue() == null)
                            || ((input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) <= 0) && (input.getRedValue() != null && input.getRedValue().compareTo(BigInteger.ZERO) <= 0))) {
                        return result;
                    } else {
                        //to must be a valid url64
                        if (!ArrayUtils.contains(DefaultInitParameters.TO_DATA_LENGTH_WHITELIST, input.getTo().length())) {
                            //F.r("invalid length");
                            return result;
                        }
                        String toB64String = TkmSignUtils.fromB64UrlToHEX(input.getTo());
                        //F.r("decode data: " + toB64String);
                        if (TkmTextUtils.isNullOrBlank(toB64String)) {
                            log.error("MORTE!!!!! " + input.getTo());
                            return result;
                        }
                    }
                    break;
                case DEREGISTER_MAIN:
                    if (TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    }
                    break;
                case DEREGISTER_OVERFLOW:
                    if (TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    }
                    break;
                case PAY:
//                    if (TkmTextUtils.isNullOrBlank(input.getTo())
//                            || TkmTextUtils.isNullOrBlank(input.getFrom())
//                            || (input.getGreenValue() == null && input.getRedValue() == null)
//                            || ((input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) <= 0))
//                            || ((input.getRedValue() != null && input.getRedValue().compareTo(BigInteger.ZERO) <= 0)) //|| ((input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) <= 0) && (input.getRedValue() != null && input.getRedValue().compareTo(BigInteger.ZERO) <= 0))
//                            )  {
                    if ((input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) < 0)
                            || (input.getRedValue() != null && input.getRedValue().compareTo(BigInteger.ZERO) < 0)
                            || (input.getRedValue() == null && input.getGreenValue() == null)
                            || (input.getRedValue() == null && input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) == 0)
                            || (input.getGreenValue() == null && input.getRedValue() != null && input.getRedValue().compareTo(BigInteger.ZERO) == 0)
                            || (input.getRedValue() != null && input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) == 0 && input.getRedValue().compareTo(BigInteger.ZERO) == 0)) {
                        BigInteger green = input.getGreenValue();
                        BigInteger red = input.getRedValue();
                        boolean a = (input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) < 0);
                        boolean b = (input.getRedValue() != null && input.getRedValue().compareTo(BigInteger.ZERO) < 0);
                        boolean c = (input.getRedValue() == null && input.getGreenValue() == null);
                        boolean d = (input.getRedValue() == null && input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) == 0);
                        boolean e = (input.getGreenValue() == null && input.getRedValue() != null && input.getRedValue().compareTo(BigInteger.ZERO) == 0);
                        boolean f = (input.getRedValue() != null && input.getGreenValue() != null && input.getGreenValue().compareTo(BigInteger.ZERO) == 0 && input.getRedValue().compareTo(BigInteger.ZERO) == 0);

                        return result;
                    } else {
                        //to must be a valid url64
                        String toB64String = TkmSignUtils.fromB64UrlToHEX(input.getTo());
                        if (TkmTextUtils.isNullOrBlank(toB64String)) {
                            return result;
                        }
                    }
                    break;
                case PREVIOUS_BLOCK:
                    if (TkmTextUtils.isNullOrBlank(input.getMessage())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())
                            || input.getEpoch() == null
                            || input.getSlot() == null) {
                        return result;
                    }
                    break;
                case REGISTER_MAIN:
                    if (TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    }
                    break;
                case REGISTER_OVERFLOW:
                    if (TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    }
                    break;
                case STAKE:
                    if (TkmTextUtils.isNullOrBlank(input.getTo())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())
                            || input.getGreenValue() == null
                            || ((input.getGreenValue().compareTo(BigInteger.ZERO) <= 0) | (input.getGreenValue().compareTo(TkmTK.unitTK(DefaultInitParameters.MINIMUM_STAKE_BET_UNIT)) < 0))) {
                        return result;
                    } else {
                        //to must be a valid url64
                        if (!ArrayUtils.contains(DefaultInitParameters.TO_DATA_LENGTH_WHITELIST, input.getTo().length())) {
                            //F.r("invalid length");
                            return result;
                        }
                        String toB64String = TkmSignUtils.fromB64UrlToHEX(input.getTo());
                        //F.r("decode data: " + toB64String);
                        if (TkmTextUtils.isNullOrBlank(toB64String)) {
                            log.error("MORTE!!!!! " + input.getTo());
                            return result;
                        }
                    }
                    break;
                case STAKE_UNDO:
                    if (TkmTextUtils.isNullOrBlank(input.getFrom())) {
                        return result;
                    }
                    break;
                case STATE_POINTER_TRANSACTION:
                    if (TkmTextUtils.isNullOrBlank(input.getMessage())
                            || TkmTextUtils.isNullOrBlank(input.getFrom())
                            || TkmTextUtils.isNullOrBlank(input.getTo())
                            || input.getEpoch() == null
                            || input.getSlot() == null) {
                        return result;
                    }
                    break;
//                    
//                case S_CREATE_ACCOUNT:
//                    if (TkmTextUtils.isNullOrBlank(input.getMessage())
//                            || TkmTextUtils.isNullOrBlank(input.getFrom())) {
//                        return result;
//                    }
//                    //more syntax spam
//
//                    try {
//                        if (!SMMessageBeanVerifier.verify(input.getMessage()).isValidSyntax()) {
//                            return result;
//                        }
//                        /*
//                String message = input.getMessage();
//                SMCreateAccountMessageBean createAccountMessageBean = SMSerializer.fromJsonToSMCreateAccountMessageBean(message);
//                if (createAccountMessageBean == null) {
//                throw new WalletException("null createAccountMessageBean content");
//                }
//                RedGreenGameteCreationTransactionRequest redGreenGameteCreationTransactionRequest = createAccountMessageBean.getRedGreenGameteCreationTransactionRequest();
//                byte[] pkByte = SMHelper.fromBase64ToByte(redGreenGameteCreationTransactionRequest.publicKey);
//                if (pkByte == null || pkByte.length == 0) {
//                throw new WalletException("null PK byte content");
//                }
//                BigInteger green = redGreenGameteCreationTransactionRequest.initialAmount;
//                if (green != null) {
//                if (BigInteger.ZERO.compareTo(green) > 0) {
//                throw new WalletException("negative green value");
//                }
//                }
//                BigInteger red = redGreenGameteCreationTransactionRequest.redInitialAmount;
//                if (red != null) {
//                if (BigInteger.ZERO.compareTo(red) > 0) {
//                throw new WalletException("negative red value");
//                }
//                }
//                         */
//                    } catch (WalletException e) {
//                        result.setValidSyntax(false);
//                        result.setExtendedMessage(e.getLocalizedMessage());
//                        //TODO debug 
//                        log.error("Syntax error in transaction.", e);
//                        return result;
//                    }
//                    break;

                case REQUEST_BLOCK:
                case RETURN_BLOCK:
                case BLOCK_CLOSE:
                case UNDEFINED:
                default:
                    return result;
            }
        }

        result.setValidSyntax(true);
        return result;
    }

    /**
     * split transaction by type in efficent way, type bucket can be empty but
     * not null
     *
     * @param transactions
     * @return
     */
    public static ConcurrentSkipListMap<KeyContexts.TransactionType, ConcurrentSkipListSet<TransactionBox>> splitByType(TransactionBox[] transactions) {
        ConcurrentSkipListMap<KeyContexts.TransactionType, ConcurrentSkipListSet<TransactionBox>> res = new ConcurrentSkipListMap<KeyContexts.TransactionType, ConcurrentSkipListSet<TransactionBox>>();
        Arrays.stream(KeyContexts.TransactionType.values()).parallel().forEach((KeyContexts.TransactionType type) -> {
            res.put(type, new ConcurrentSkipListSet<TransactionBox>());
        });
        if (transactions != null) {
            Arrays.stream(transactions).parallel().filter((TransactionBox tbox) -> {
                return tbox != null;
            }).filter((TransactionBox tbox) -> {
                return tbox.isValid();
            }).forEach((TransactionBox tbox) -> {
                res.get(tbox.type()).add(tbox);
            });
        }
        return res;
    }

    public static ConcurrentSkipListMap<KeyContexts.TransactionType, ConcurrentSkipListSet<ExtendedTransactionBox>> splitByType(ExtendedTransactionBox[] transactions) {
        ConcurrentSkipListMap<KeyContexts.TransactionType, ConcurrentSkipListSet<ExtendedTransactionBox>> res = new ConcurrentSkipListMap<KeyContexts.TransactionType, ConcurrentSkipListSet<ExtendedTransactionBox>>();
        Arrays.stream(KeyContexts.TransactionType.values()).unordered().parallel().forEach((KeyContexts.TransactionType type) -> {
            res.put(type, new ConcurrentSkipListSet<ExtendedTransactionBox>());
        });
        if (transactions != null) {
            Arrays.stream(transactions).unordered().parallel().filter((ExtendedTransactionBox tbox) -> {
                return tbox != null;
            }).filter((ExtendedTransactionBox tbox) -> {
                return tbox.isValid();
            }).forEach((ExtendedTransactionBox tbox) -> {
                res.get(tbox.type()).add(tbox);
            });
        }
        return res;
    }

}
