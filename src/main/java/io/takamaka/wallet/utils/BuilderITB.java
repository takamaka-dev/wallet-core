/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import io.takamaka.wallet.beans.InternalBlockBean;
import io.takamaka.wallet.beans.InternalTransactionBean;
import io.takamaka.wallet.beans.PrivateBlockTxBean;
import io.takamaka.wallet.beans.TkmRewardBean;
import io.takamaka.wallet.beans.TransactionBean;
import io.takamaka.wallet.exceptions.HashCompositionException;
import io.takamaka.wallet.exceptions.NullInternalTransactionBeanException;
import io.takamaka.wallet.exceptions.TransactionNotYetImplementedException;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Iris Dimni iris.dimni@takamaka.io
 */
@Slf4j
public class BuilderITB {

    private static InternalTransactionBean common(KeyContexts.TransactionType tType, Date notBefore) {
        InternalTransactionBean result = new InternalTransactionBean();
        result.setTransactionType(tType);
        result.setNotBefore(notBefore);
        return result;
    }

    /**
     *
     * @param from
     * @param to
     * @param greenValue
     * @param redValue
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean pay(String from, String to, BigInteger greenValue, BigInteger redValue, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.PAY, notBefore);
            result.setFrom(from);
            result.setTo(to);
            result.setGreenValue(greenValue);
            result.setRedValue(redValue);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating pay transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param to
     * @param greenValue
     * @param redValue
     * @param message
     * @return
     */
    public static InternalTransactionBean pay(String from, String to, BigInteger greenValue, BigInteger redValue, String message) {
        return BuilderITB.pay(from, to, greenValue, redValue, message, new Date());
    }

    /**
     *
     * @param from
     * @param to
     * @param greenValue
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean stake(String from, String to, BigInteger greenValue, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.STAKE, notBefore);
            result.setFrom(from);
            result.setTo(to);
            result.setGreenValue(greenValue);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating stake transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from holder address
     * @param to destination main address
     * @param greenValue token in mTKG
     * @param message
     * @return
     */
    public static InternalTransactionBean stake(String from, String to, BigInteger greenValue, String message) {
        return BuilderITB.stake(from, to, greenValue, message, new Date());
    }

    /**
     *
     * @param from
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean stakeUndo(String from, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.STAKE_UNDO, notBefore);
            result.setFrom(from);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating stake undo transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param message
     * @return
     */
    public static InternalTransactionBean stakeUndo(String from, String message) {
        return BuilderITB.stakeUndo(from, message, new Date());
    }

    /**
     *
     * @param from
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean blob(String from, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.BLOB, notBefore);
            result.setFrom(from);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating blob transaction", ex);
            return null;
        }
    }

    public static InternalTransactionBean test(KeyContexts.TransactionType type, String from, String to, String message, BigInteger green, BigInteger red, Integer epoch, Integer slot, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(type, notBefore);
            result.setFrom(from);
            result.setTo(to);
            result.setMessage(message);
            result.setGreenValue(green);
            result.setRedValue(red);
            result.setEpoch(epoch);
            result.setSlot(slot);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating generic transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param message
     * @return
     */
    public static InternalTransactionBean blob(String from, String message) {
        return BuilderITB.blob(from, message, new Date());
    }

    /**
     *
     * @param to
     * @param greenValue
     * @param redValue
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean declaration(String to, BigInteger greenValue, BigInteger redValue, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.DECLARATION, notBefore);
            result.setTo(to);
            result.setGreenValue(greenValue);
            result.setRedValue(redValue);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating declaration transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param to
     * @param greenValue
     * @param redValue
     * @param message
     * @return
     */
    public static InternalTransactionBean declaration(String to, BigInteger greenValue, BigInteger redValue, String message) {
        return BuilderITB.declaration(to, greenValue, redValue, message, new Date());
    }

    /**
     *
     * @param from
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean registerMain(String from, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.REGISTER_MAIN, notBefore);
            result.setFrom(from);
//            if (!TkmTextUtils.isNullOrBlank(to)) {
//                result.setTo(to);
//            }
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating register main transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param message
     * @return
     */
    public static InternalTransactionBean registerMain(String from, String message) {
        return BuilderITB.registerMain(from, message, new Date());
    }

    /**
     *
     * @param from
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean registerOverflow(String from, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.REGISTER_OVERFLOW, notBefore);
            result.setFrom(from);
//            if (!TkmTextUtils.isNullOrBlank(to)) {
//                result.setTo(to);
//            }
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating register overflow transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param message
     * @return
     */
    public static InternalTransactionBean registerOverflow(String from, String message) {
        return BuilderITB.registerOverflow(from, message, new Date());
    }

    /**
     *
     * @param from
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean deregisterOverflow(String from, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.DEREGISTER_OVERFLOW, notBefore);
            result.setFrom(from);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating deregister overflow transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param message
     * @return
     */
    public static InternalTransactionBean deregisterOverflow(String from, String message) {
        return BuilderITB.deregisterOverflow(from, message, new Date());
    }

    /**
     *
     * @param from
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean deregisterMain(String from, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.DEREGISTER_MAIN, notBefore);
            result.setFrom(from);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating deregister main transaction", ex);
            return null;
        }
    }

    public static InternalBlockBean block(TransactionBean coinbase, TransactionBean previousBlock, TransactionBean blockHash, ConcurrentSkipListMap<String, String> forwardKeys, ConcurrentSkipListMap<String, TkmRewardBean> rewardList, ConcurrentSkipListSet<PrivateBlockTxBean> transactions) {

        InternalBlockBean result = new InternalBlockBean();
        result.setCoinbase(coinbase);
        result.setPreviousBlock(previousBlock);
        result.setBlockHash(blockHash);
        result.setForwardKeys(forwardKeys);
        result.setRewardList(rewardList);
        result.setTransactions(transactions);
        return result;
    }

    /**
     *
     * @param from
     * @param message
     * @return
     */
    public static InternalTransactionBean deregisterMain(String from, String message) {
        return BuilderITB.deregisterMain(from, message, new Date());
    }

    /**
     * assign the overflow address (to) to the main address (from)
     *
     * @param from main address
     * @param to overflow addess
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean assignOverflow(String from, String to, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.ASSIGN_OVERFLOW, notBefore);
            result.setFrom(from);
            result.setTo(to);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating assign overflow transaction", ex);
            return null;
        }
    }

    /**
     * assign the overflow address (to) to the main address (from)
     *
     * @param from main address
     * @param to overflow addess
     * @param message
     * @return
     */
    public static InternalTransactionBean assignOverflow(String from, String to, String message) {
        return BuilderITB.assignOverflow(from, to, message, new Date());
    }

    /**
     *
     * @param from
     * @param to
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean unassignOverflow(String from, String to, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.UNASSIGN_OVERFLOW, notBefore);
            result.setFrom(from);
            result.setTo(to);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating unassign overflow transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param to
     * @param message
     * @return
     */
    public static InternalTransactionBean unassignOverflow(String from, String to, String message) {
        return BuilderITB.unassignOverflow(from, to, message, new Date());
    }

    /**
     *
     * @param from
     * @param to
     * @param epoch
     * @param slot
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean statePointerTransaction(String from, String to, int epoch, int slot, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.STATE_POINTER_TRANSACTION, notBefore);
            result.setFrom(from);
            result.setTo(to);
            result.setEpoch(epoch);
            result.setSlot(slot);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating state pointer transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param to
     * @param epoch
     * @param slot
     * @param message
     * @return
     */
    public static InternalTransactionBean statePointerTransaction(String from, String to, int epoch, int slot, String message) {
        return BuilderITB.statePointerTransaction(from, to, epoch, slot, message, new Date());
    }

    /**
     *
     * @param from
     * @param epoch
     * @param slot
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean blockHash(String from, int epoch, int slot, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.BLOCK_HASH, notBefore);
            result.setFrom(from);
            result.setEpoch(epoch);
            result.setSlot(slot);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating blockhash transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param epoch
     * @param slot
     * @param message
     * @return
     */
    public static InternalTransactionBean blockHash(String from, int epoch, int slot, String message) {
        return BuilderITB.blockHash(from, epoch, slot, message, new Date());
    }

    /**
     *
     * @param to
     * @param epoch
     * @param slot
     * @param greenValue
     * @param redValue
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean coinBase(String to, int epoch, int slot, BigInteger greenValue, BigInteger redValue, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.COINBASE, notBefore);
            result.setTo(to);
            result.setEpoch(epoch);
            result.setSlot(slot);
            result.setGreenValue(greenValue);
            result.setRedValue(redValue);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating coinbase transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param to
     * @param epoch
     * @param slot
     * @param greenValue
     * @param redValue
     * @param message
     * @return
     */
    public static InternalTransactionBean coinBase(String to, int epoch, int slot, BigInteger greenValue, BigInteger redValue, String message) {
        return BuilderITB.coinBase(to, epoch, slot, greenValue, redValue, message, new Date());
    }
//    cambiare le transazioni di contratto in tre
//            contract_deploy
//            contract_instance
//            contract_call

    /**
     *
     * @param from
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean contractDeploy(String from, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.S_CONTRACT_DEPLOY, notBefore);
            result.setFrom(from);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating contract deploy transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param message
     * @return
     */
    public static InternalTransactionBean contractDeploy(String from, String message) {
        return BuilderITB.contractDeploy(from, message, new Date());
    }

    /**
     *
     * @param from
     * @param to
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean contractInstance(String from, String to, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.S_CONTRACT_INSTANCE, notBefore);
            result.setFrom(from);
            result.setFrom(to);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating contract instance transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param to
     * @param message
     * @return
     */
    public static InternalTransactionBean contractInstance(String from, String to, String message) {
        return BuilderITB.contractInstance(from, to, message, new Date());
    }

    /**
     *
     * @param from
     * @param to
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean contractCall(String from, String to, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.S_CONTRACT_CALL, notBefore);
            result.setFrom(from);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating contract call transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param to
     * @param message
     * @return
     */
    public static InternalTransactionBean contractCall(String from, String to, String message) {
        return BuilderITB.contractCall(from, to, message, new Date());
    }

    /**
     *
     * @param from
     * @param epoch
     * @param slot
     * @param message
     * @param notBefore
     * @return
     */
    public static InternalTransactionBean previousBlock(String from, int epoch, int slot, String message, Date notBefore) {

        try {
            InternalTransactionBean result = BuilderITB.common(KeyContexts.TransactionType.PREVIOUS_BLOCK, notBefore);
            result.setFrom(from);
            result.setEpoch(epoch);
            result.setSlot(slot);
            result.setMessage(message);
            result.setTransactionHash(TkmTextUtils.internalTransactionBeanHash(result));

            return result;
        } catch (NullInternalTransactionBeanException | HashCompositionException ex) {
            log.error("Error creating previous block transaction", ex);
            return null;
        }
    }

    /**
     *
     * @param from
     * @param epoch
     * @param slot
     * @param message
     * @return
     */
    public static InternalTransactionBean previousBlock(String from, int epoch, int slot, String message) {
        return BuilderITB.previousBlock(from, epoch, slot, message, new Date());
    }

    public static InternalTransactionBean getInternalTransactionBean(KeyContexts.TransactionType type, String from, String to, BigInteger greenValue, BigInteger redValue, String message) throws TransactionNotYetImplementedException {
        InternalTransactionBean itb = null;
        switch (type) {
            case PAY:
                itb = BuilderITB.pay(from, to, greenValue, redValue, message);
                break;
            case DECLARATION:
                itb = BuilderITB.declaration(to, greenValue, redValue, message);
                break;
            case REGISTER_MAIN:
                itb = BuilderITB.registerMain(from, message);
                break;
            case REGISTER_OVERFLOW:
                itb = BuilderITB.registerOverflow(from, message);
                break;
            case DEREGISTER_OVERFLOW:
                itb = BuilderITB.deregisterOverflow(from, message);
                break;
            case DEREGISTER_MAIN:
                itb = BuilderITB.deregisterMain(from, message);
                break;
            case ASSIGN_OVERFLOW:
                itb = BuilderITB.assignOverflow(from, to, message);
                break;
            case UNASSIGN_OVERFLOW:
                itb = BuilderITB.unassignOverflow(from, to, message);
                break;
            case STAKE_UNDO:
                itb = BuilderITB.stakeUndo(from, message);
                break;
            case STAKE:
                itb = BuilderITB.stake(from, to, greenValue, message);
                break;
            case BLOB:
                itb = BuilderITB.blob(from, message);
                break;
            default:
                throw new TransactionNotYetImplementedException("NOT YET IMPLEMENTED: " + type.name());
        }
        return itb;
    }
}
