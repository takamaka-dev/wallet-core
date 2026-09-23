package io.takamaka.wallet.exceptions;

/**
 * F64 — the recovery phrase is not valid: wrong word count, a word outside the
 * dictionary, or words 24-25 that are not the checksum of the words before
 * them. Thrown BEFORE any seed derivation, so a typo or a swapped pair can no
 * longer restore a different wallet. The message carries counts and 1-based
 * positions only, never a word.
 */
public class InvalidRecoveryWordsException extends WalletException {

    public InvalidRecoveryWordsException(String msg) {
        super(msg);
    }
}
