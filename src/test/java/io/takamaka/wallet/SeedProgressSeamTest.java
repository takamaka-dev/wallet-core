/*
 * DR-011 — progress-emission seam for seed derivation (SPIKE-0).
 */
package io.takamaka.wallet;

import io.takamaka.wallet.utils.SeedGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the additive progress seam on {@link SeedGenerator#generateSeedPWH}:
 * <ol>
 *   <li><b>Output-identical</b> — the seed derived with a progress listener is
 *       byte-for-byte the same as the no-arg form (the callback must never touch
 *       the hash computation; parity-safe).</li>
 *   <li><b>Emission contract</b> — the listener fires exactly once per word
 *       (25), with monotonically increasing {@code completedWords} and a stable
 *       {@code totalWords == 25}.</li>
 * </ol>
 */
public class SeedProgressSeamTest {

    @Test
    void progressOverload_isOutputIdentical_andFiresPerWord() throws Exception {
        final List<String> words = SeedGenerator.generateWords();
        assertNotNull(words);
        assertEquals(25, words.size());

        final String baseline = SeedGenerator.generateSeedPWH(words);

        final AtomicInteger ticks = new AtomicInteger(0);
        final AtomicInteger lastCompleted = new AtomicInteger(0);
        final List<Integer> totals = new ArrayList<>();

        final String withProgress = SeedGenerator.generateSeedPWH(words, (completed, total) -> {
            ticks.incrementAndGet();
            // monotonic, contiguous progress
            assertEquals(lastCompleted.get() + 1, completed, "completedWords must increment by 1");
            lastCompleted.set(completed);
            totals.add(total);
        });

        // (1) parity-safe: identical seed with or without the listener
        assertEquals(baseline, withProgress, "progress listener must not change the derived seed");

        // (2) emission contract: one tick per word, total stable at 25
        assertEquals(25, ticks.get(), "listener must fire once per word");
        assertEquals(25, lastCompleted.get(), "final completedWords must equal word count");
        assertTrue(totals.stream().allMatch(t -> t == 25), "totalWords must be stable at 25");
    }

    @Test
    void nullListener_behavesLikeNoArg() throws Exception {
        final List<String> words = SeedGenerator.generateWords();
        assertEquals(
                SeedGenerator.generateSeedPWH(words),
                SeedGenerator.generateSeedPWH(words, null),
                "null listener must match the no-arg overload");
    }
}
