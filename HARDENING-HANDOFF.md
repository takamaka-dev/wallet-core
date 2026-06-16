# HANDOFF — wallet-core key hardening (DR-003 + DR-009)

**Purpose:** delegate two **wallet-core** implementation tasks to a dedicated agent, with **extensive
cross-platform compatibility testing**, so the chat-web-gui *design* thread can continue in parallel.
**Created:** 2026-06-11 · **Type:** implementation delegation (not design).

You are implementing **two accepted decisions**. Do **not** redesign them — read the authoritative
sources, implement, and test hard. Surface any genuine design gap back rather than inventing.

---

## 0. Read first (authoritative sources)

- `rschat-docs/roadmap/DECISION_REGISTER.md` → **DR-003** (key-cache fix) and **DR-009** (versioned
  `.wallet` keystore). These are the contracts.
- `chat-web-gui/docs/ux/IDENTITY_ONBOARDING.md` → **§5.1** (DR-003 detail: per-index memoization),
  **§1** (ground-truth file:line table), **§4.3/§4.4** (the chat-web-gui import/export consumer).
- `CLAUDE.md` → security + cross-platform test discipline. **"NEVER modify cryptographic algorithms
  without thorough cross-platform testing."** Java is the reference; Flutter must match byte-for-byte.

> **All file:line refs below come from a 2026-06-11 inventory — verify them against current code
> before editing (code may have drifted).**

## 1. Repos & branches (verified 2026-06-11)

| Repo | Branch @ HEAD | Role |
|------|---------------|------|
| `wallet-core` | `feature/app-root-overload` @ `23cd035` | **primary** — `InstanceWalletKeyStore*`, `WalletHelper`, `KeyContexts` |
| `wallet-core-flutter` | `chat` @ `0382017` | Flutter parity (key derivation cache + keystore format) |
| `shell` | `feature/user-options` @ `fb927a2` | consumer (create/unlock/recovery) — must keep working |
| `tkmChat` | `chat` @ `cc5c4a4` | Flutter consumer |
| `chat-web-gui` | `main` | design consumer (no code yet) — **do not touch its design docs** |

Branch as appropriate off the primary; commit reference (Java) + port (Flutter) **together** for the
parity-gated parts. Author `Giovanni Antino <tech@takamaka.io>`, footer
`Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`. **Commit/push only on explicit user OK.**

---

## 2. Work-stream A — DR-003: per-index concurrent memoization (DO FIRST)

**Smaller, lower-risk, output-identical. Land it first.**

### Defect
`InstanceWalletKeyStoreBCRSA4096ENC.getKeyPairAtIndex(int)` (≈`:341`) and `…ENC256` use
double-checked locking **without the inner re-check**, on a **single instance-wide lock**
(`getKeyPairAtIndexLock`, ≈`:57`). Two threads racing the same index each run RSA-4096 keygen
(the system's most expensive op); and **all indices serialize on the one lock** (index 5 blocks index 7).

### Fix (scope = per-index concurrency, not just the inner re-check)
- Replace the single lock with **per-index memoization**: a `ConcurrentHashMap` keyed per index holding
  a **`CompletableFuture` per index** — `computeIfAbsent(i, k -> new CompletableFuture<>())` installs the
  slot cheaply/atomically; run the **heavy RSA work outside any shared lock**; late callers for the same
  index await the same future. **Do not run the long compute inside `computeIfAbsent`'s mapping
  function** (it holds the bin lock for the duration).
- Ed25519 / Curve25519 are cheap — a lock-free `putIfAbsent` (accept a rare duplicate) is fine; fix for
  correctness, not cost.
- Apply across **all** keystores (`…BCED25519`, `…BCCurve25519`, `…BCQTESLAPSSC1Round1/2`,
  `…BCRSA4096ENC`, `…BCRSA4096ENC256`) and the public-key getters (`getPublicKeyAtIndex*`) that share
  the missing-re-check shape.
- **Idempotency is the safety property:** derivation is a pure function of `(seed, domain, index)`, so a
  race can only waste work — **the derived key MUST be byte-identical before and after.**

### Tests (compatibility = output-identical + concurrency-correct)
1. **Output-identical:** for several `(seed, index)`, assert the keypair is bit-for-bit the same as the
   pre-fix implementation (and unchanged across runs).
2. **No duplicate keygen:** a spy/counter around RSA generation; N threads racing first-access to the
   **same** index ⇒ generation count **exactly 1**.
3. **Cross-index parallelism:** N threads each on a **distinct** index derive concurrently (assert they
   overlap — e.g. a latch/timing or thread-id capture), not serialized.
4. **Existing wallet-core + shell tests pass** (no behavioural change for consumers).
5. **Flutter:** locate the Dart key-derivation cache in `wallet-core-flutter` (the `WalletCacheService`
   equivalent — find it; the inventory's name may be stale) and verify it does **not** share the
   missing-re-check pattern; fix if it does. Confirm Java↔Flutter still derive identical keys (existing
   parity must hold).

**Acceptance A:** all of the above green; derived keys provably unchanged.

---

## 3. Work-stream B — DR-009: versioned `.wallet` keystore + extensive compat testing

**Bigger, cross-platform, format change. Preserve backward compatibility absolutely.**

### Design (from DR-009)
- **Unversioned / `KeyContexts.WALLET_JSON_AES` ⇒ legacy v1** — read with today's hardcoded params
  (`AES/CBC/PKCS5Padding`, **fixed salt `"TakamakaWallet"`**, **1 PBKDF2 iteration** — `WalletHelper`
  ≈`:109`). **READ PATH NEVER CHANGES.**
- **`WALLET_JSON_AES_V2` ⇒ v2** — **AES-GCM**, **random per-file salt**, **configurable iteration count**,
  carried as **authenticated cleartext header params** (GCM AAD = the header, so params can't be
  tampered/downgraded). Unlock reads salt+iterations from the header and enforces a **minimum-security
  floor** (reject below-floor iterations). Default iterations ≈ the vault regime (~10⁶) — **pick and
  document** the exact value.

### Implementation
- Extend `KeyContexts` with the v2 discriminator and the `EncKeyBean`/header schema (version, kdf algo,
  random salt, iterations, GCM IV/tag) — **decide the exact header serialization and document it.**
- `WalletHelper` read **dispatches on the discriminator** (absent/`WALLET_JSON_AES` → v1 reader;
  `…_V2` → v2 reader). Write path produces **v2** (capability — see guardrail).
- Mirror in **`wallet-core-flutter`**: read v1+v2, write v2 — **byte-identical** format.

### Extensive compatibility testing (this IS the acceptance gate)
- **Backward-compat:** every existing v1 `.wallet` still opens — Java **and** Flutter. (Use real
  fixtures.)
- **Round-trip:** write v2 → read v2 for Java↔Java, Flutter↔Flutter, **and Java↔Flutter both
  directions** (write in one platform, read in the other) — the core parity requirement.
- **Parity vectors:** extend `TestVectorGenerator` with v2 keystore vectors; Flutter reproduces
  **byte-identical**. **Determinism note:** v2 uses a *random* salt, so the vector must **inject a fixed
  salt (and IV) as a test input** to be deterministic; production keeps them random. Vector = (password,
  fixed salt, fixed IV, iterations, plaintext) → exact ciphertext + tag.
- **Negative / robustness:** tampered header ⇒ **GCM auth failure → reject**; below-floor iterations ⇒
  reject; wrong password ⇒ clean failure, **no partial state**; corrupt/unknown version ⇒ safe error.
- **No-loss migration:** if you re-encrypt a v1 file to v2, do it only on explicit save/password-change,
  **after** verifying the v2 output re-reads to the identical seed+words — never delete/overwrite the v1
  until the v2 is proven readable.

### Guardrails (critical)
- **v1 read path is frozen** — existing wallets must always open.
- **Do NOT flip the production default to WRITE v2.** Ship v2-WRITE behind a flag/capability. Enabling
  it by default is a release decision (read-everywhere-before-write-anywhere across wallet-core + shell +
  Flutter + the chat-web-gui consumer) — **return to the user.**
- **Rollout order:** land v2-READ everywhere first; v2-WRITE capability + parity gate next; default flip
  later, by user OK.

**Acceptance B:** backward-compat + round-trip + parity vectors (Java reference + Flutter byte-identical,
committed together) + negative tests, all green; v2-WRITE behind a flag.

---

## 4. Boundaries (what NOT to do)

- Don't change the **v1 read** format or any **derivation output** (DR-003 is concurrency-only).
- Don't enable **v2-write by default**; don't auto-migrate destructively.
- Don't touch **chat-web-gui design docs** (that thread continues separately) or merge any **held
  branches** (e.g. DR-001 `feature/color-scheme-v2`) — out of scope.
- Crypto changes obey the `CLAUDE.md` cross-platform discipline; no shortcuts (no weakening KDFs to mask
  perf).

## 5. Report back

1. **DR-003:** done + test evidence (output-identical proof, no-duplicate-keygen, parallelism).
2. **DR-009:** v2 read+write capability + parity vectors + backward-compat/negative results; **list the
   impl choices you made** (header schema, default iteration count, GCM AAD construction) for
   design-thread review; v2-write-default **left pending user OK**.
3. Anything that turned out under-specified → flag it, don't paper over it.

**Suggested agents:** `takamaka-blockchain-architect` (Java crypto + cross-module) for the
implementation; `flutter-rsocket-compatibility-tester` for the cross-platform parity/test matrix. A
`bug-hunter-patcher` can own the focused DR-003 fix. One orchestrator may sequence A → B.
