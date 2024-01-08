package org.example.blockchain.impl.hash;

import java.util.Set;
import org.example.blockchain.impl.tree.Transaction;

public record HashInputData(
    String previousHash, Set<Transaction> txData, long timestamp, int nonce) {
  @Override
  public String toString() {
    return this.previousHash() + this.timestamp() + this.nonce() + this.txData();
  }
}
