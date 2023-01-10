/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.InstanceWalletKeystoreInterface;
import io.takamaka.wallet.TkmCypherProviderBCED25519;
import io.takamaka.wallet.TkmCypherProviderBCQTESLAPSSC1Round1;
import io.takamaka.wallet.TkmCypherProviderBCQTESLAPSSC1Round2;
import io.takamaka.wallet.beans.InternalBlockBean;
import io.takamaka.wallet.beans.InternalTransactionBean;
import io.takamaka.wallet.beans.TkmCypherBean;
import io.takamaka.wallet.beans.TransactionBean;
import io.takamaka.wallet.beans.TransactionBox;
import io.takamaka.wallet.beans.TransactionSyntaxBean;
import io.takamaka.wallet.exceptions.HashCompositionException;
import io.takamaka.wallet.exceptions.InclusionHashCreationException;
import io.takamaka.wallet.exceptions.NullInternalTransactionBeanException;
import io.takamaka.wallet.exceptions.TransactionCanNotBeCreatedException;
import io.takamaka.wallet.exceptions.TransactionCanNotBeSignedException;
import io.takamaka.wallet.exceptions.WalletException;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
 */
@Slf4j
public class TkmWallet {

    /**
     * It returns the Chrypted envelope used later in TKM chain
     *
     * @param itb Java Bean Class - it contains all the transaction elements
     * needed
     * @param iwk is an abstract layer that represents the involved wallet
     * @param signKey is the chosen index needed for the creation of the wallet
     * public address
     * @return TransactionBean - Java Bean Class - it contains the wrapped
     * elements located inside the itb object
     * @throws TransactionCanNotBeCreatedException
     */
    public static TransactionBean createGenericTransaction(InternalTransactionBean itb, InstanceWalletKeystoreInterface iwk, int signKey) throws TransactionCanNotBeCreatedException {
        try {
            //todo verify itb transaction
            itb.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(itb));
            TransactionBean tb = new TransactionBean();
            tb.setRandomSeed(TkmTextUtils.generateWalletRandomString());
            tb.setPublicKey(iwk.getPublicKeyAtIndexURL64(signKey));
            tb.setMessage(TkmTextUtils.toJson(itb));
            tb.setWalletCypher(iwk.getWalletCypher());
            TkmCypherBean signatureBean = new TkmCypherBean();

            switch (tb.getWalletCypher()) {
                case Ed25519BC:
                    signatureBean = TkmCypherProviderBCED25519.sign(iwk.getKeyPairAtIndex(signKey), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());
                    if (!signatureBean.isValid()) {
                        throw new TransactionCanNotBeSignedException(signatureBean.getEx());
                    }
                    break;
                case BCQTESLA_PS_1:
                    signatureBean = TkmCypherProviderBCQTESLAPSSC1Round1.sign(iwk.getKeyPairAtIndex(signKey), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());
                    if (!signatureBean.isValid()) {
                        throw new TransactionCanNotBeSignedException(signatureBean.getEx());
                    }
                    break;
                case BCQTESLA_PS_1_R2:
                    signatureBean = TkmCypherProviderBCQTESLAPSSC1Round2.sign(iwk.getKeyPairAtIndex(signKey), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());
                    if (!signatureBean.isValid()) {
                        throw new TransactionCanNotBeSignedException(signatureBean.getEx());
                    }
                    break;
                default:
                    signatureBean.setValid(false);
                    log.error("UNKNOWN CYPHER");
            }
            tb.setSignature(signatureBean.getSignature());
            TransactionSyntaxBean transactionBeanValid = TransactionUtils.isTransactionBeanValid(tb);
            if (!transactionBeanValid.isValidSyntax()) {
                throw new TransactionCanNotBeCreatedException("invalid internal parameter " + transactionBeanValid.getExtendedMessage());
            }

            return tb;

        } catch (NullInternalTransactionBeanException | HashCompositionException | WalletException ex) {
            log.error("Error generating transaction", ex);
            throw new TransactionCanNotBeCreatedException(ex);
        }

    }

    public static TransactionBean createGenericTransaction(InternalBlockBean ibb, InstanceWalletKeystoreInterface iwk, int signKey) throws TransactionCanNotBeCreatedException {
        try {
            //todo verify itb transaction
            //ibb.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(itb));
            TransactionBean tb = new TransactionBean();
            tb.setRandomSeed(TkmTextUtils.generateWalletRandomString());
            tb.setPublicKey(iwk.getPublicKeyAtIndexURL64(signKey));
            tb.setMessage(TkmTextUtils.toJson(ibb));
            tb.setWalletCypher(iwk.getWalletCypher());
            TkmCypherBean signatureBean = new TkmCypherBean();

            switch (tb.getWalletCypher()) {
                case Ed25519BC:
                    signatureBean = TkmCypherProviderBCED25519.sign(iwk.getKeyPairAtIndex(signKey), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());
                    if (!signatureBean.isValid()) {
                        throw new TransactionCanNotBeSignedException(signatureBean.getEx());
                    }
                    break;
                case BCQTESLA_PS_1:
                    signatureBean = TkmCypherProviderBCQTESLAPSSC1Round1.sign(iwk.getKeyPairAtIndex(signKey), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());
                    if (!signatureBean.isValid()) {
                        throw new TransactionCanNotBeSignedException(signatureBean.getEx());
                    }
                    break;
                case BCQTESLA_PS_1_R2:
                    signatureBean = TkmCypherProviderBCQTESLAPSSC1Round2.sign(iwk.getKeyPairAtIndex(signKey), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());
                    if (!signatureBean.isValid()) {
                        throw new TransactionCanNotBeSignedException(signatureBean.getEx());
                    }
                    break;
                default:
                    signatureBean.setValid(false);
                    //signatureBean.setEx("UNKNOWN CYPHER");
                    log.error("UNKNOWN CYPHER");
            }

            tb.setSignature(signatureBean.getSignature());
            return tb;

        } catch (WalletException ex) {
            log.error("Error creating transaction", ex);
            throw new TransactionCanNotBeCreatedException(ex);
        }

    }

    /**
     * It verifies the Transaction Bean content going through all the wrapped
     * chain elements parsed
     *
     * use this function with caution, check for null values. For transaction
     * integrity check use {@code verifyTransactionIntegrity}, for block
     * integrity check use {@code BlockUtils.decodeBlock(String blockJson)}
     *
     * @param tb - Java Class Bean - Chrypted envelope
     * @return TkmCypherBean
     */
    public static TkmCypherBean verifySign(TransactionBean tb) {
        TkmCypherBean tcb = null;
        switch (tb.getWalletCypher()) {
            case Ed25519BC:
                tcb = TkmCypherProviderBCED25519.verify(tb.getPublicKey(), tb.getSignature(), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());
                break;
            case BCQTESLA_PS_1:
                tcb = TkmCypherProviderBCQTESLAPSSC1Round1.verify(tb.getPublicKey(), tb.getSignature(), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());//here
                break;
            case BCQTESLA_PS_1_R2:
                tcb = TkmCypherProviderBCQTESLAPSSC1Round2.verify(tb.getPublicKey(), tb.getSignature(), tb.getMessage() + tb.getRandomSeed() + tb.getWalletCypher().name());//here
                break;
            default:
                tcb = new TkmCypherBean();
                tcb.setValid(false);
                log.error("CYPHER NOT IMPLEMENTED");
        }
        if (!tcb.isValid()) {
            log.error(tb.getWalletCypher() + "");
        }
        return tcb;
    }

    public static TransactionBox verifyTransactionIntegrity(String transactionJson) {
        TransactionBean tb = TkmTextUtils.transactionBeanFromJson(transactionJson);
        TransactionBox veriTransactionIntegrity = verifyTransactionIntegrity(tb, transactionJson);
        return veriTransactionIntegrity;
    }

    /**
     * It creates a transaction box containing both Transaction bean and itb
     * message Only when the verifySign method sets a valid transaction
     *
     * @param tb Java Class Bean - Chrypted envelope
     * @param transactionJson
     * @return
     */
    public static TransactionBox verifyTransactionIntegrity(TransactionBean tb, String transactionJson) {
        TransactionBox result = new TransactionBox();
        if (tb != null) {
            TkmCypherBean cyBean = verifySign(tb);
            if (cyBean != null && cyBean.isValid()) {
                //this function call syntax check
                TransactionSyntaxBean transactionBeanValid = TransactionUtils.isTransactionBeanValid(tb);
                InternalTransactionBean itb = TkmTextUtils.internalTransactionBeanFromJson(tb.getMessage());
                //if itb null syntax error
                boolean signerFieldsVerification = verifySigner(tb, itb);
                if (!signerFieldsVerification) {
                    //syntax check error add print here for debug
                }
                if (itb != null & signerFieldsVerification & transactionBeanValid.isValidSyntax()) {

                    try {
                        String sith = TkmTextUtils.singleTransactionInclusionHash(itb.getTransactionHash(), tb.getPublicKey(), tb.getSignature(), tb.getRandomSeed(), tb.getWalletCypher().name());
                        result.setSingleInclusionTransactionHash(sith);
                        result.setTb(tb);
                        result.setItb(itb);
                        result.setTransactionJson(transactionJson);
                        result.setValid(true);
                    } catch (InclusionHashCreationException ex) {
                        log.error("Transaction verification error", ex);
                    }
                }
                cyBean = verifyHashIntegrity(itb, cyBean);
                result.setValid(cyBean.isValid() & signerFieldsVerification & transactionBeanValid.isValidSyntax());
                if (cyBean.getEx() != null) {
                    cyBean.getEx().printStackTrace();
                    log.error("INVALID!!");
                }
            } else {
                if (cyBean != null && cyBean.getEx() != null) {
                    cyBean.getEx().printStackTrace();
                }
                log.error("INVALID!!");
            }
        }
        return result;
    }

    public static final TkmCypherBean verifyHashIntegrity(InternalTransactionBean itb, TkmCypherBean cyBean) {
        if (cyBean == null) {
            cyBean = new TkmCypherBean();
            cyBean.setValid(false);
            cyBean.setEx(new Exception("TkmCypherBean must be not null"));
        }
        try {
            String internalTransactionBeanHash = TkmTextUtils.internalTransactionBeanHash(itb);
            //enforce hash check after 1659534306000
            if (itb.getNotBefore().getTime() > 1659534306000L) {
                if (!itb.getTransactionHash().equals(internalTransactionBeanHash)) {
                    cyBean.setValid(false);
                    cyBean.setEx(new Exception("transaction not pass mandatory hash check"));
                }
            }
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            cyBean.setEx(ex);
            log.error("Hash verification error", ex);
        }
        return cyBean;
    }

    /**
     * It creates a transaction box containing both Transaction bean and itb
     * message Only when the verifySign method sets a valid transaction
     *
     * @param tb Java Class Bean - Chrypted envelope
     * @return
     */
    public static TransactionBox verifyTransactionIntegrity(TransactionBean tb) {
        return verifyTransactionIntegrity(tb, TkmTextUtils.toJson(tb));
    }

    private static boolean verifySigner(TransactionBean tb, InternalTransactionBean itb) {
        if (itb == null | tb == null) {
            return false;
        } else {
            switch (itb.getTransactionType()) {
                // signer == form
                case ASSIGN_OVERFLOW:
                case BLOB:
                case BLOCK_HASH:
                case S_CONTRACT_CALL:
                case S_CONTRACT_DEPLOY:
                case S_CONTRACT_INSTANCE:
                case S_CREATE_ACCOUNT:
                case PREVIOUS_BLOCK:
                case DEREGISTER_MAIN:
                case DEREGISTER_OVERFLOW:
                case REGISTER_MAIN:
                case REGISTER_OVERFLOW:
                case STAKE:
                case STAKE_UNDO:
                case STATE_POINTER_TRANSACTION:
                case UNASSIGN_OVERFLOW:
                case PAY:
                    return itb.getFrom().equals(tb.getPublicKey());
                //signer == to
                case DECLARATION:
                    //no check on declarations
                    return true;
                case COINBASE:
                    return itb.getTo().equals(tb.getPublicKey());
                case BLOCK:
                    log.error("NOT IMPLEMENTED");
                case UNDEFINED:
                default:
                    log.error("NOT YET IMPLEMENTED");
                    return false;
            }
        }
    }

}
