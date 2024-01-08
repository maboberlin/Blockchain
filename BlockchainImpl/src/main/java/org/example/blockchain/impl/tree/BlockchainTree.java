package org.example.blockchain.impl.tree;

import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static java.util.Comparator.comparingInt;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.example.blockchain.api.BlockchainConstants;
import org.example.blockchain.impl.service.BlockNotAppendableException;
import org.springframework.stereotype.Component;

@Component
public class BlockchainTree {

  private final ReadWriteLock blockchainLock = new ReentrantReadWriteLock();
  private final Lock blockchainWriteLock = blockchainLock.writeLock();
  private final Lock blockchainReadLock = blockchainLock.readLock();

  private final BlockchainNode genesisNode;
  private BlockchainNode headNode;
  private final Map<String, List<BlockchainNode>> hashToNodesMapping;
  private final Set<UUID> txData;

  public BlockchainTree() {
    hashToNodesMapping = new ConcurrentHashMap<>();
    txData = ConcurrentHashMap.newKeySet();
    genesisNode = buildGenesisNode();
  }

  public void addBlock(Block newBlock) {
    executeInWriteLock(
        () -> {
          BlockchainNode previousBlockchainNode = getPreviousBlockchainNode(newBlock);
          BlockchainNode newBlockchainNode =
              buildAndLinkNewBlockchainNode(newBlock, previousBlockchainNode);
          addTransactions(newBlock);
          setNewHeadNode(newBlockchainNode);
        });
  }

  public Set<Block> getBlockchain() {
    return executeInReadLock(
        () -> {
          Set<Block> result = new HashSet<>();
          BlockchainNode nextBlockchainNode = headNode != null ? headNode : genesisNode;
          while (nextBlockchainNode != null) {
            result.add(nextBlockchainNode.getBlock());
            nextBlockchainNode = nextBlockchainNode.getParent();
          }
          return result;
        });
  }

  public BlockchainData getBlockchainData() {
    return executeInReadLock(
        () -> {
          Block lastBlock = headNode != null ? headNode.getBlock() : genesisNode.getBlock();
          Set<String> allHashes =
              getBlockchain().stream().map(Block::hash).collect(Collectors.toSet());
          return new BlockchainData(lastBlock, allHashes);
        });
  }

  public boolean isTxDataAlreadyProcessed(Set<Transaction> data) {
    return executeInReadLock(() -> data.stream().map(Transaction::id).anyMatch(txData::contains));
  }

  private BlockchainNode buildGenesisNode() {
    Block genesisBlock =
        new Block(
            BlockchainConstants.GENESIS_HASH,
            null,
            null,
            0,
            OffsetDateTime.now().toEpochSecond(),
            0,
            EMPTY);
    BlockchainNode genesisNode = new BlockchainNode(null, genesisBlock);
    hashToNodesMapping.put(
        BlockchainConstants.GENESIS_HASH, buildSynchronizedModifiableList(genesisNode));
    return genesisNode;
  }

  private BlockchainNode getPreviousBlockchainNode(Block newBlock) {
    String previousHash = newBlock.previousHash();
    return Optional.ofNullable(hashToNodesMapping.get(previousHash))
        .orElse(Collections.emptyList())
        .stream()
        .max(comparingInt(node -> node.getBlock().depth()))
        .orElseThrow(() -> new BlockNotAppendableException(newBlock));
  }

  private BlockchainNode buildAndLinkNewBlockchainNode(
      Block newBlock, BlockchainNode previousBlockchainNode) {
    BlockchainNode newBlockchainNode = new BlockchainNode(previousBlockchainNode, newBlock);
    previousBlockchainNode.addToChildren(newBlockchainNode);
    String hash = newBlock.hash();
    hashToNodesMapping.computeIfPresent(
        hash, (p, collection) -> addBlockChainNode(newBlockchainNode, collection));
    hashToNodesMapping.computeIfAbsent(
        hash, p -> buildSynchronizedModifiableList(newBlockchainNode));
    return newBlockchainNode;
  }

  private void addTransactions(Block newBlock) {
    newBlock.transactionSet().stream().map(Transaction::id).forEach(txData::add);
  }

  private void setNewHeadNode(BlockchainNode newBlockchainNode) {
    if (headNode == null || newBlockchainNode.getBlock().depth() > headNode.getBlock().depth()) {
      headNode = newBlockchainNode;
    }
  }

  private List<BlockchainNode> buildSynchronizedModifiableList(BlockchainNode node) {
    return synchronizedList(new ArrayList<>(singletonList(node)));
  }

  private static List<BlockchainNode> addBlockChainNode(
      BlockchainNode newBlockchainNode, List<BlockchainNode> collection) {
    collection.add(newBlockchainNode);
    return collection;
  }

  @SneakyThrows
  private <T> T executeInReadLock(Callable<T> callable) {
    try {
      this.blockchainReadLock.lock();
      return callable.call();
    } finally {
      this.blockchainReadLock.unlock();
    }
  }

  @SneakyThrows
  private void executeInWriteLock(Runnable runnable) {
    try {
      this.blockchainWriteLock.lock();
      runnable.run();
    } finally {
      this.blockchainWriteLock.unlock();
    }
  }

  public record BlockchainData(Block lastBlock, Set<String> allHashes) {}
}
