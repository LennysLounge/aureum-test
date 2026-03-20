# Aureum

A golden master testing library for Java.

## Multiple verifications per test

When calling `verify` more than once from the same test method, a unique `name` must be provided for each call. Without it, all calls resolve to the same file path.

This is especially important for parameterized tests running in parallel — invocation order is non-deterministic, so the parameter value itself should be used as the name.