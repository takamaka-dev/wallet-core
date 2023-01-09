/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.takamaka.wallet;

import io.takamaka.wallet.exceptions.WalletException;
import io.takamaka.wallet.utils.KeyContexts;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

/**
 *
 * @author giovanni.antino@h2tcoin.com
 */
public interface InstanceWalletKeystoreInterface extends Comparable<InstanceWalletKeystoreInterface> {

    /**
     *
     * @return the wallet cypher
     */
    KeyContexts.WalletCypher getWalletCypher();

    /**
     * @param i
     * @return the key pair public and private address related to the index
     * @throws WalletException
     */
    AsymmetricCipherKeyPair getKeyPairAtIndex(int i) throws WalletException;

    /**
     * @param i
     * @return the public key as byte array
     * @throws WalletException
     */
    byte[] getPublicKeyAtIndexByte(int i) throws WalletException;

    /**
     * @param i
     * @return the public key as URL64 image String
     * @throws WalletException
     */
    String getPublicKeyAtIndexURL64(int i) throws WalletException;

    /**
     * @return the current wallet ID
     */
    String getCurrentWalletID();

}
