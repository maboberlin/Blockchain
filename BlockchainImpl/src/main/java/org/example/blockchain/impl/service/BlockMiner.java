package org.example.blockchain.impl.service;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.blockchain.impl.hash.HashFunction;
import org.example.blockchain.impl.hash.HashInputData;
import org.example.blockchain.impl.hash.HashVerifier;
import org.example.blockchain.impl.tree.Block;
import org.example.blockchain.impl.tree.Transaction;

@Slf4j
@RequiredArgsConstructor
public class BlockMiner implements Supplier<Block> {

  private final AtomicBoolean running = new AtomicBoolean(true);

  private final String previousHash;
  private final int depth;
  private final Set<String> allMinedHashes;
  private final Set<Transaction> txData;
  private final String minedBy;

  @Override
  public Block get() {
    // TODO: 04.11.23 implement more accurate bitcoin hashing algorithm according to
    // https://en.bitcoin.it/wiki/Block_hashing_algorithm
    int nonceStart = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    log.info("Start calculating hash with nonce: {}", nonceStart);
    int nonce = nonceStart;
    int hashVerifierMultiplier = calculateHashVerifierMultiplier();
    Predicate<String> hashVerifier = new HashVerifier(hashVerifierMultiplier);
    Function<HashInputData, String> hashFunction = new HashFunction();
    long timestamp = OffsetDateTime.now().toEpochSecond();
    do {
      String hash = calculateHash(previousHash, txData, nonce, timestamp, hashFunction);
      if (hashVerifier.test(hash) && !allMinedHashes.contains(hash)) {
        return new Block(hash, previousHash, txData, depth, timestamp, nonce, minedBy);
      }
    } while (running.get() && ++nonce != nonceStart);
    log.info("Calculating hash has been interrupted.");
    return null;
  }

  public void interrupt() {
    running.set(false);
  }

  public static boolean verifyBlock(Block block) {
    Function<HashInputData, String> hashFunction = new HashFunction();
    String hash =
        calculateHash(
            block.previousHash(),
            block.transactionSet(),
            block.nonce(),
            block.timestamp(),
            hashFunction);
    int hashVerifierMultiplier = calculateHashVerifierMultiplier();
    Predicate<String> hashVerifier = new HashVerifier(hashVerifierMultiplier);
    return hash.equals(block.hash()) && hashVerifier.test(hash);
  }

  private static String calculateHash(
      String previousHash,
      Set<Transaction> txData,
      int nonce,
      long timestamp,
      Function<HashInputData, String> hashFunction) {
    HashInputData hashInputData = new HashInputData(previousHash, txData, timestamp, nonce);
    return hashFunction.apply(hashInputData);
  }

  private static int calculateHashVerifierMultiplier() {
    return 4;
  }
}
