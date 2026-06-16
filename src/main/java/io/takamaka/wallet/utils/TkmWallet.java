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
import static io.takamaka.wallet.utils.KeyContexts.WalletCypher.Curve25519BC;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@Slf4j
public class TkmWallet {

    /**
     * It returns the signed envelope used later in TKM chain
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
            /**
             * The method createGenericTransaction creates a new TransactionBean
             * object from the provided InternalTransactionBean object,
             * InstanceWalletKeystoreInterface object and the index of the
             * signing key. The InternalTransactionBean object is used to set
             * the message field of the newly created TransactionBean object,
             * the hash of the InternalTransactionBean is calculated and set as
             * well. Then a random seed is generated for this transaction. The
             * publicKey field is set by calling the getPublicKeyAtIndexURL64
             * method of InstanceWalletKeystoreInterface with the given signKey
             * as an input. The walletCypher field is set by calling the
             * getWalletCypher method of InstanceWalletKeystoreInterface. The
             * method then creates a TkmCypherBean and signs the message by
             * calling the appropriate sign method from the TkmCypherProvider
             * class based on the walletCypher. The signature and the cypher
             * type is set on the TransactionBean object. The method then check
             * the valid syntax of the transaction and returns it. If any
             * exception occurs it will throw
             * TransactionCanNotBeCreatedException with the caught exception as
             * its parameter.
             */
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
                case Curve25519BC:
                    
                    break;
                case Ed25519:

                case Tink:

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

    /**
     *
     * The createGenericTransaction() method creates a new transaction given an
     * InternalBlockBean, an InstanceWalletKeystoreInterface, and an integer
     * index for the signKey. The method creates a new TransactionBean, sets its
     * fields such as random seed, public key, message, and wallet cypher. It
     * signs the message using the keyPairAtIndex method of the provided
     * InstanceWalletKeystoreInterface. And returns the created transaction bean
     * object.
     *
     * @param ibb - an InternalBlockBean object containing the details of the
     * transaction
     * @param iwk - an InstanceWalletKeystoreInterface containing the keypair
     * used to sign the transaction
     * @param signKey - an integer representing the index of the key used for
     * signing the transaction
     * @return - a TransactionBean object representing the created transaction
     * @throws TransactionCanNotBeCreatedException - if an error occurred while
     * creating the transaction
     */
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
     * This method is used to verify the {@link TransactionBean} content going
     * through all the wrapped chain elements parsed. It uses the TkmCypherBean
     * to check if the signature is valid.
     *
     * @param tb the TransactionBean object containing the signature to be
     * verified
     * @return a TkmCypherBean object indicating if the signature is valid or
     * not.
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
     * Method to verify the integrity of a transaction, given the transaction
     * bean and the transaction json.
     *
     * The method takes in two arguments: a TransactionBean object, which is an
     * signed envelope containing a JSON string representation of a transaction,
     * and a string representation of the same transaction in JSON format.
     *
     * @param tb the transaction bean
     * @param transactionJson the json representation of the transaction
     * @return a TransactionBox containing the result of the verification,
     * including the Single Inclusion Transaction Hash, the TransactionBean, the
     * InternalTransactionBean, the transactionJson and a boolean indicating if
     * the transaction is valid
     */
    public static TransactionBox verifyTransactionIntegrity(TransactionBean tb, String transactionJson) {
        /**
         * The method first checks that the TransactionBean is not null. It then
         * calls the verifySign method passing the TransactionBean object, to
         * check the validity of the signature of the transaction. If the
         * returned object is not null and is valid, it then checks the syntax
         * of the transaction using the isTransactionBeanValid method and it
         * also checks that the signer fields are valid using the verifySigner
         * method, passed both the TransactionBean and an
         * InternalTransactionBean object, which is created by deserializing the
         * JSON string in the message field of the TransactionBean. If all the
         * above checks are successful, it creates a inclusion hash and
         * populates the TransactionBox object with the TransactionBean,
         * InternalTransactionBean, the JSON string representation of the
         * transaction and the inclusion hash. The validity of the
         * TransactionBox is then set to true if the inclusion hash is
         * successfully created. The verifyHashIntegrity is called passing the
         * InternalTransactionBean and the TkmCypherBean object, to check the
         * integrity of the hash in the transaction, and the validity of the
         * TransactionBox object is set based on the returned result of this
         * function call. If any of the above checks fail, the method will log
         * an error message "INVALID!!" and return the TransactionBox object
         * with the validity field set to false.
         */
        TransactionBox result = new TransactionBox();
        boolean inclusionHashCreation = false;
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
                        inclusionHashCreation = true;
                    } catch (InclusionHashCreationException ex) {
                        log.error("Transaction verification error", ex);
                        inclusionHashCreation = false;
                    }
                }
                cyBean = verifyHashIntegrity(itb, cyBean);
                result.setValid(cyBean.isValid() & signerFieldsVerification & transactionBeanValid.isValidSyntax() & inclusionHashCreation);
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

    /**
     *
     * This method is used to verify the integrity of the hash of an internal
     * transaction bean (itb). It accepts an {@link InternalTransactionBean} and
     * a {@link TkmCypherBean} as input and returns a {@link TkmCypherBean}. The
     * method generates a hash of the itb and compares it with the
     * 'transactionHash' field of the itb. If the hash generated is not equal to
     * the transactionHash, the valid field of returned TkmCypherBean will be
     * set to false and it will have exception message. This check is enforced
     * for the transactions included in the main blockchain with a date greater
     * than 1659534306000 (timestamp).
     *
     * @param itb is an internalTransactionBean for which integrity is to be
     * checked
     * @param cyBean is TkmCypherBean passed for maintaining previous exception
     * messages if present.
     * @return TkmCypherBean, where if the hash check passes it will be
     * valid=true otherwise valid=false
     */
    public static final TkmCypherBean verifyHashIntegrity(InternalTransactionBean itb, TkmCypherBean cyBean) {
        if (cyBean == null) {
            cyBean = new TkmCypherBean();
            cyBean.setValid(false);
            cyBean.setEx(new Exception("TkmCypherBean must be not null"));
        }
        try {
            String internalTransactionBeanHash = TkmTextUtils.internalTransactionBeanHash(itb);
            //enforce hash check after 1659534306000
            //This check was introduced to account for some transactions 
            //included in the main blockchain with an earlier date than 
            //indicated, which due to a bug in the verifier, were accepted with 
            //a hash format that did not comply with the specification.

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
