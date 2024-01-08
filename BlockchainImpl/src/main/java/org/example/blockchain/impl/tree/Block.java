package org.example.blockchain.impl.tree;

import java.util.Set;

public record Block(
    String hash,
    String previousHash,
    Set<Transaction> transactionSet,
    int depth,
    long timestamp,
    int nonce,
    String minedBy) {}
