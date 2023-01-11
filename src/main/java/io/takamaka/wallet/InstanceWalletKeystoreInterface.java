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
 * Interface for Instance Wallet Keystore. This interface defines the methods
 * for creating, reading and accessing key pairs in a wallet. It also defines
 * the method for getting the public key in different forms.
 *
 * @author Giovanni Antino <giovanni.antino at takamaka.io>
 */
public interface InstanceWalletKeystoreInterface extends Comparable<InstanceWalletKeystoreInterface> {

    /**
     * Get the type of cypher used for key generation
     *
     * @return the type of cypher used
     */
    KeyContexts.WalletCypher getWalletCypher();

    /**
     * Get the keypair at a specific index in the wallet
     *
     * @param i the index of the keypair
     * @return the keypair at the given index
     * @throws WalletException if the index is not valid
     */
    AsymmetricCipherKeyPair getKeyPairAtIndex(int i) throws WalletException;

    /**
     * Get the public key of keypair at a specific index in the wallet
     *
     * @param i the index of the keypair
     * @return the public key of keypair at the given index
     * @throws WalletException if there is an error getting the public key
     */
    byte[] getPublicKeyAtIndexByte(int i) throws WalletException;

    /**
     * Get the URL64 encoded public key of keypair at a specific index in the
     * wallet
     *
     * @param i the index of the keypair
     * @return the URL64 encoded public key of keypair at the given index
     * @throws WalletException if there is an error getting the public key
     */
    String getPublicKeyAtIndexURL64(int i) throws WalletException;

    /**
     * Get the current wallet ID
     *
     * @return the current wallet ID
     */
    String getCurrentWalletID();
}
