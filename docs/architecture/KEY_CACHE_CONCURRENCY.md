# Key-cache concurrency (DR-003)

**Scope:** the in-memory derived-key caches in the `InstanceWalletKeyStore*`
classes and how concurrent access to them is made correct and non-serializing.
**Decision of record:** DR-003 (`rschat-docs/roadmap/DECISION_REGISTER.md`).
**Landed:** commit `d464844` (Java), `1887198` (the Dart analogue in
`wallet-core-flutter`).

---

## 1. What these caches are

Each keystore derives a keypair for a wallet *index* on demand and memoizes it:

| Keystore | Derived per index | Cost |
|----------|-------------------|------|
| `InstanceWalletKeyStoreBCRSA4096ENC` / `…ENC256` | RSA-4096 keypair | **very expensive** (the system's heaviest op) |
| `InstanceWalletKeyStoreBCED25519` | Ed25519 keypair | cheap |
| `InstanceWalletKeyStoreBCCurve25519` | Curve25519 keypair | cheap |
| `InstanceWalletKeyStoreBCQTESLAPSSC1Round1` / `…Round2` | QTESLA keypair | moderate |

Derivation is a **pure, deterministic function of `(seed, index)`** — isolated in
the `generateKeyPairAtIndex(int)` test-seam method. Same inputs always produce a
byte-identical keypair. This idempotency is the safety property the whole design
leans on: a race can only ever *waste work*, never produce a divergent key.

## 2. The defect (before DR-003)

`getKeyPairAtIndex(int)` used double-checked locking **without the inner
re-check**, on a **single instance-wide lock**:

```java
if (!signKeys.containsKey(index)) {        // outer check
    synchronized (getKeyPairAtIndexLock) { // ONE lock for ALL indices
        // BUG: no re-check here
        signKeys.put(index, generate(index));
    }
}
```

Two failure modes:

1. **Duplicate keygen.** Two threads that both miss the same index each enter the
   lock in turn and *each* run the (expensive) generation; the second silently
   overwrites. Correct result, wasted RSA-4096 work.
2. **Cross-index serialization.** Because the lock is instance-wide, deriving
   index 5 **blocks** deriving index 7 — even though they are independent.

## 3. The fix — per-index monitor + inner re-check + lock-free fast read

A **per-index lock object** replaces the single lock. The `computeIfAbsent`
call allocates *only the cheap monitor* (`k -> new Object()`) — it does **not**
run the derivation inside the map's mapping function (which would hold a bin
lock for the whole compute).

```java
// lock-free fast path — already cached, no lock taken
AsymmetricCipherKeyPair cached = signKeys.get(index);
if (cached != null) return cached;

// DR-003: per-index monitor — distinct indices run in parallel; same index runs once.
synchronized (getKeyPairAtIndexLocks.computeIfAbsent(index, k -> new Object())) {
    cached = signKeys.get(index);          // inner re-check under the per-index lock
    if (cached != null) return cached;
    AsymmetricCipherKeyPair keyPair = generateKeyPairAtIndex(index);
    signKeys.put(index, keyPair);
    return keyPair;
}
```

Properties:

- **Cross-index parallelism.** Index 5 and index 7 obtain *different* monitor
  objects, so their derivations never contend. Index 5 no longer blocks index 7.
- **Exactly-once per index.** A second caller for the *same* index blocks on that
  index's monitor, then the inner re-check returns the first caller's result —
  generation runs **once**.
- **Lock-free hits.** An already-cached index returns via the leading
  `signKeys.get(index)` with no lock at all.

> **Note on same-index callers.** The synchronous Java API *blocks* a second
> same-index caller until the first finishes deriving (it holds the monitor for
> the duration of the keygen). That is the intended trade-off for a blocking API:
> it guarantees exactly-once without an extra indirection. A non-blocking
> (future-per-index) variant would let same-index callers *await* instead of
> *block* — unnecessary here, because wallet-core's `getKeyPairAtIndex` is a
> synchronous call and the win that mattered (cross-index parallelism) is already
> delivered by the distinct monitors.

### Derived public-key caches

`getPublicKeyAtIndexURL64` / `getPublicKeyAtIndexByte` derive the keypair via the
method above, then memoize the *encoded* form with a lock-free `putIfAbsent` on a
`ConcurrentMap` (`hexPublicKeys` / `bytePublicKeys`). Encoding is cheap and
deterministic, so accepting a rare duplicate encode is preferable to locking.

This shape is applied uniformly across all six keystores.

## 4. What lives in the library vs. what lives in the client

This is the boundary that DR-003 deliberately draws.

**The library (wallet-core) guarantees, and only this:**
- Derivation is **thread-safe** and **non-serializing across indices**.
- Each index is derived **exactly once**; concurrent same-index callers share the
  one result.
- Behaviour is **idempotent and output-identical** under any concurrency.

**The library deliberately does NOT:**
- Own a thread pool, spawn threads, or choose a degree of parallelism. The
  keygen runs on **whatever thread the caller invoked from**. The keystore is
  passive — it simply stops *getting in the way* of concurrent callers.

**The client owns the execution policy** — and it is runtime- and
machine-specific:
- *How many* indices to derive concurrently, on *which* scheduler/thread-pool,
  eager vs. on-demand, and how to bound memory and keep the UI responsive.
- chat-web-gui (Java/Reactor): per-index
  `Mono.fromCallable(...).subscribeOn(boundedElastic()).cache()`.
- Flutter: isolates / `compute` (e.g. `TkmWallet.loadAsync`).

The DR-003 library fix is the **enabling prerequisite**: it converts "parallel
derivation is unsafe / wasteful here" into "parallel derivation is safe and free
of duplicate work." Turning that *potential* into *actual* speedup is the
client's job and is not — and should not be — in wallet-core.

## 5. Verification

`src/test/java/io/takamaka/wallet/DR003KeyCacheConcurrencyTest.java` (4/4 green):

1. **Output-identical** — for several `(seed, index)`, the derived keypair is
   bit-for-bit the pre-fix result and stable across runs.
2. **No duplicate keygen** — a counter around `generateKeyPairAtIndex`; N threads
   racing first-access to the **same** index ⇒ generation count **exactly 1**.
3. **Cross-index parallelism** — N threads on **distinct** indices derive
   concurrently (observed overlap, not serialized).
4. The full wallet-core suite remains green (no behavioural change for consumers).

The Dart analogue (`wallet-core-flutter`: per-index in-flight `Future` map in
`ed25519_keystore` / `rsa4096_keystore`, plus `TkmWallet.loadAsync`) is verified
by `test/dr003_key_cache_concurrency_test.dart`.

## 6. References

- Decision: `rschat-docs/roadmap/DECISION_REGISTER.md` → **DR-003**.
- Code: `io.takamaka.wallet.InstanceWalletKeyStore*` — `getKeyPairAtIndex`,
  `getKeyPairAtIndexLocks`, `generateKeyPairAtIndex`, `getPublicKeyAtIndex*`.
- Commits: `d464844` (Java), `1887198` (Flutter).
