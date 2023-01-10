/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.takamaka.wallet.utils.KeyContexts;
import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author Iris Dimni <iris.dimni at takamaka.io>
 */
public class InternalBlockBean implements Serializable{
    private TransactionBean coinbase;
    private TransactionBean previousBlock;
    private TransactionBean blockHash;
    private ConcurrentSkipListMap<String, String> forwardKeys;
    /**
     * sorting string,reward bean
     */
    private ConcurrentSkipListMap<String, TkmRewardBean> rewardList;
    private ConcurrentSkipListSet<PrivateBlockTxBean> transactions;
    
    public InternalBlockBean() {
    }

    public ConcurrentSkipListMap<String, String> getForwardKeys() {
        return forwardKeys;
    }

    /**
     * @return the coinbase
     */
    public TransactionBean getCoinbase() {
        return coinbase;
    }

    /**
     * return the transaction containing the previous block hash
     *
     * @return the previousBlock
     */
    public TransactionBean getPreviousBlock() {
        return previousBlock;
    }

    /**
     * return the transaction containing the block hash
     *
     * @return the blockHash
     */
    public TransactionBean getBlockHash() {
        return blockHash;
    }

    /**
     * @param coinbase the coinbase to set
     */
    public void setCoinbase(TransactionBean coinbase) {
        this.coinbase = coinbase;
    }

    /**
     * @param previousBlock the previousBlock to set
     */
    public void setPreviousBlock(TransactionBean previousBlock) {
        this.previousBlock = previousBlock;
    }

    /**
     * @param blockHash the blockHash to set
     */
    public void setBlockHash(TransactionBean blockHash) {
        this.blockHash = blockHash;
    }

    /**
     * @return the transactions
     */
    public ConcurrentSkipListSet<PrivateBlockTxBean> getTransactions() {
        return transactions;
    }

    /**
     * @param transactions the transactions to set
     */
    public void setTransactions(ConcurrentSkipListSet<PrivateBlockTxBean> transactions) {
        this.transactions = transactions;
    }

    /**
     * EpochSlot, Public Key
     *
     * @return
     */
    public ConcurrentSkipListMap<String, String> getForwardKeyes() {
        return forwardKeys;
    }

    public void setForwardKeys(ConcurrentSkipListMap<String, String> forwardKeys) {
        this.forwardKeys = forwardKeys;
    }

    /**
     * sorting string,reward bean
     *
     * @return
     */
    public ConcurrentSkipListMap<String, TkmRewardBean> getRewardList() {
        return rewardList;
    }

    /**
     * sorting string,reward bean
     *
     * @param rewardList
     */
    public void setRewardList(ConcurrentSkipListMap<String, TkmRewardBean> rewardList) {
        this.rewardList = rewardList;
    }

    @JsonIgnore
    public KeyContexts.TransactionType getTransactionType() {
        return KeyContexts.TransactionType.BLOCK;
    }
}
