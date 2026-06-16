/*
 * DR-003 — wallet-core key-cache per-index concurrent memoization.
 *
 * Proves the three acceptance properties of the DR-003 fix:
 *   1. OUTPUT-IDENTICAL  — derivation is unchanged: repeated calls and
 *      independent instances over the same (seed, index) yield byte-identical
 *      keys, and the cache returns a stable reference.
 *   2. NO DUPLICATE KEYGEN — N threads racing first-access to the SAME index
 *      trigger the (expensive) derivation exactly once.
 *   3. CROSS-INDEX PARALLELISM — N threads on DISTINCT indices derive
 *      concurrently (their derivation windows overlap), no longer serialized on
 *      a single instance-wide lock.
 *
 * Properties (2) and (3) are made deterministic (not timing-flaky) via the
 * package-visible test seam {@code generateKeyPairAtIndex(int)} on the RSA
 * keystore: a counting subclass records invocations + execution windows and
 * returns a cheap deterministic stub, so the concurrency behaviour is observed
 * without paying real RSA-4096 cost. Property (1) exercises the real path.
 */
package io.takamaka.wallet;

import io.takamaka.wallet.exceptions.UnlockWalletException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

public class DR003KeyCacheConcurrencyTest {

    // ---------------------------------------------------------------------
    // (1) OUTPUT-IDENTICAL — derivation unchanged by the locking refactor
    // ---------------------------------------------------------------------

    @Test
    void ed25519_derivationIsDeterministicAndCacheIsStable(@TempDir Path appRoot) throws Exception {
        InstanceWalletKeyStoreBCED25519 w = new InstanceWalletKeyStoreBCED25519("ed-det", appRoot);

        for (int index : new int[]{0, 1, 5, 42}) {
            // Repeated calls hit the cache and return a STABLE reference + bytes.
            AsymmetricCipherKeyPair first = w.getKeyPairAtIndex(index);
            assertSame(first, w.getKeyPairAtIndex(index), "cache must return the same instance for index " + index);
            assertEquals(w.getPublicKeyAtIndexURL64(index), w.getPublicKeyAtIndexURL64(index));
            assertArrayEquals(w.getPublicKeyAtIndexByte(index), w.getPublicKeyAtIndexByte(index));
        }

        // A fresh instance over the SAME wallet file (same seed) derives byte-identical keys.
        InstanceWalletKeyStoreBCED25519 w2 = new InstanceWalletKeyStoreBCED25519("ed-det", appRoot);
        for (int index : new int[]{0, 1, 5, 42}) {
            assertEquals(w.getPublicKeyAtIndexURL64(index), w2.getPublicKeyAtIndexURL64(index),
                    "independent instance must derive identical URL64 pubkey for index " + index);
            assertArrayEquals(w.getPublicKeyAtIndexByte(index), w2.getPublicKeyAtIndexByte(index),
                    "independent instance must derive identical byte pubkey for index " + index);
        }
    }

    @Test
    void rsa_derivationIsDeterministicAcrossInstances(@TempDir Path appRoot) throws Exception {
        // RSA-4096 is expensive; one index, two instances, is sufficient to prove
        // the refactor preserves the derived bytes.
        InstanceWalletKeyStoreBCRSA4096ENC w1 = new InstanceWalletKeyStoreBCRSA4096ENC("rsa-det", appRoot);
        InstanceWalletKeyStoreBCRSA4096ENC w2 = new InstanceWalletKeyStoreBCRSA4096ENC("rsa-det", appRoot);

        assertSame(w1.getKeyPairAtIndex(0), w1.getKeyPairAtIndex(0), "cache must be stable");
        assertEquals(w1.getPublicKeyAtIndexURL64(0), w2.getPublicKeyAtIndexURL64(0));
        assertArrayEquals(w1.getPublicKeyAtIndexByte(0), w2.getPublicKeyAtIndexByte(0));
    }

    // ---------------------------------------------------------------------
    // (2) NO DUPLICATE KEYGEN — same index derives exactly once under a race
    // ---------------------------------------------------------------------

    @Test
    void sameIndexRace_derivesExactlyOnce(@TempDir Path appRoot) throws Exception {
        final int threads = 8;
        final int index = 7;
        CountingRSAKeystore w = new CountingRSAKeystore("rsa-race-same", appRoot, 200L);

        List<AsymmetricCipherKeyPair> results = runConcurrent(threads, () -> w.getKeyPairAtIndex(index));

        assertEquals(threads, results.size(), "all threads completed");
        assertEquals(1, w.genCount(index),
                "the expensive derivation for a single index must run exactly once under a race");

        // Every caller observes the SAME memoized instance.
        AsymmetricCipherKeyPair canonical = results.get(0);
        assertNotNull(canonical);
        for (AsymmetricCipherKeyPair kp : results) {
            assertSame(canonical, kp, "every racing caller must receive the one memoized keypair");
        }
    }

    // ---------------------------------------------------------------------
    // (3) CROSS-INDEX PARALLELISM — distinct indices derive concurrently
    // ---------------------------------------------------------------------

    @Test
    void distinctIndices_deriveInParallel(@TempDir Path appRoot) throws Exception {
        Assumptions.assumeTrue(Runtime.getRuntime().availableProcessors() >= 2,
                "cross-index parallelism requires >= 2 CPU cores");

        final int threads = Math.min(4, Runtime.getRuntime().availableProcessors());
        final long workMillis = 500L;
        CountingRSAKeystore w = new CountingRSAKeystore("rsa-parallel", appRoot, workMillis);

        // Each thread derives a DISTINCT index simultaneously.
        AtomicInteger seq = new AtomicInteger();
        List<AsymmetricCipherKeyPair> results = runConcurrent(threads, () -> w.getKeyPairAtIndex(seq.getAndIncrement()));
        assertEquals(threads, results.size());

        // Each distinct index derived exactly once.
        for (int i = 0; i < threads; i++) {
            assertEquals(1, w.genCount(i), "index " + i + " derived exactly once");
        }

        // The derivation windows overlap: the LAST to start began before the
        // FIRST to finish. Under the old single instance-wide lock the windows
        // would be strictly disjoint (serialized), failing this assertion.
        long maxStart = Long.MIN_VALUE;
        long minEnd = Long.MAX_VALUE;
        for (int i = 0; i < threads; i++) {
            long[] window = w.window(i);
            assertNotNull(window, "missing derivation window for index " + i);
            maxStart = Math.max(maxStart, window[0]);
            minEnd = Math.min(minEnd, window[1]);
        }
        assertTrue(maxStart < minEnd,
                "derivation windows must overlap (concurrent), not serialize: maxStart=" + maxStart + " minEnd=" + minEnd);
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------

    /** Runs {@code task} on {@code threads} threads released simultaneously by a barrier. */
    private static <T> List<T> runConcurrent(int threads, ThrowingSupplier<T> task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier startGate = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);
        List<T> results = new CopyOnWriteArrayList<>();
        List<Throwable> errors = new CopyOnWriteArrayList<>();
        try {
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        startGate.await();           // release all threads together
                        results.add(task.get());
                    } catch (Throwable ex) {
                        errors.add(ex);
                    } finally {
                        done.countDown();
                    }
                });
            }
            assertTrue(done.await(60, TimeUnit.SECONDS), "concurrent tasks must finish within 60s");
        } finally {
            pool.shutdownNow();
        }
        if (!errors.isEmpty()) {
            throw new AssertionError("concurrent task(s) threw: " + errors.get(0), errors.get(0));
        }
        return new ArrayList<>(results);
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /**
     * RSA keystore whose derivation seam is instrumented: it counts invocations
     * per index, records each derivation's [start,end] window, optionally sleeps
     * to widen the observable window, and returns a cheap deterministic stub
     * (no real RSA-4096) so the concurrency behaviour is observed quickly.
     */
    static final class CountingRSAKeystore extends InstanceWalletKeyStoreBCRSA4096ENC {

        private final ConcurrentMap<Integer, AtomicInteger> counts = new ConcurrentHashMap<>();
        private final ConcurrentMap<Integer, long[]> windows = new ConcurrentHashMap<>();
        private final long sleepMillis;

        CountingRSAKeystore(String walletName, Path appRoot, long sleepMillis) throws UnlockWalletException {
            super(walletName, appRoot);
            this.sleepMillis = sleepMillis;
        }

        @Override
        protected AsymmetricCipherKeyPair generateKeyPairAtIndex(int index) {
            counts.computeIfAbsent(index, k -> new AtomicInteger()).incrementAndGet();
            long start = System.nanoTime();
            if (sleepMillis > 0) {
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            // Deterministic, distinct-per-index stub — exercises the cache/lock
            // path without the cost of a real RSA-4096 keygen. Ed25519 params
            // carry no modulus validation, so a cheap index-derived 32-byte seed
            // suffices to build a valid AsymmetricCipherKeyPair.
            byte[] seed32 = new byte[Ed25519PrivateKeyParameters.KEY_SIZE];
            seed32[0] = (byte) index;
            seed32[1] = (byte) (index >>> 8);
            Ed25519PrivateKeyParameters priv = new Ed25519PrivateKeyParameters(seed32, 0);
            AsymmetricCipherKeyPair stub = new AsymmetricCipherKeyPair(priv.generatePublicKey(), priv);
            windows.put(index, new long[]{start, System.nanoTime()});
            return stub;
        }

        int genCount(int index) {
            AtomicInteger c = counts.get(index);
            return c == null ? 0 : c.get();
        }

        long[] window(int index) {
            return windows.get(index);
        }
    }
}
