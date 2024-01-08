package org.example.blockchain.impl.service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.blockchain.impl.client.BlockchainNodesClient;
import org.example.blockchain.impl.tree.Block;
import org.example.blockchain.impl.tree.BlockchainTree;
import org.example.blockchain.impl.tree.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BlockchainService {

  private final String serviceIdentifier;
  private final BlockchainTree blockchainTree;
  private final BlockchainNodesClient blockchainNodesClient;
  private final ExecutorService executorService;
  private final Map<String, BlockMiner> blockMinerMap;

  @Autowired
  public BlockchainService(
      @Value("${eureka.instance.instance-id}") String instanceId,
      @Value("${server.port}") String portNumber,
      BlockchainTree blockchainTree,
      BlockchainNodesClient blockchainNodesClient) {
    this.serviceIdentifier = "%s:%s".formatted(instanceId, portNumber);
    this.blockchainTree = blockchainTree;
    this.blockchainNodesClient = blockchainNodesClient;
    this.executorService = Executors.newSingleThreadExecutor();
    this.blockMinerMap = new ConcurrentHashMap<>();
  }

  public CompletableFuture<Block> mineBlock(Set<Transaction> transactionDtoList) {
    log.info("Mining block");
    if (blockchainTree.isTxDataAlreadyProcessed(transactionDtoList)) {
      throw new DataAlreadyMinedException();
    }
    String minerKey = generateTransactionsKey(transactionDtoList);
    if (blockMinerMap.containsKey(minerKey)) {
      throw new MiningForDataAlreadyRunningException();
    }

    BlockchainTree.BlockchainData blockchainData = blockchainTree.getBlockchainData();
    Block lastBlock = blockchainData.lastBlock();
    BlockMiner blockMiner =
        new BlockMiner(
            lastBlock.hash(),
            lastBlock.depth() + 1,
            blockchainData.allHashes(),
            transactionDtoList,
            serviceIdentifier);
    blockMinerMap.put(minerKey, blockMiner);
    return CompletableFuture.supplyAsync(blockMiner, executorService)
        .whenComplete((block, exception) -> handleMiningResult(minerKey, block, exception));
  }

  private void handleMiningResult(String previousHash, Block block, Throwable exception) {
    if (exception != null) {
      log.warn("Error occurred during mining block.", exception);
    } else if (block != null) {
      log.info("Successfully mined block: {}", block);
      try {
        blockchainTree.addBlock(block);
        blockchainNodesClient.publishBlock(block);
      } catch (Exception e) {
        log.warn("Error adding or publishing mined block.", e);
      }
    }
    blockMinerMap.remove(previousHash);
  }

  public void addBlock(Block block) {
    log.info("Adding block with hash: {}", block.hash());
    BlockchainTree.BlockchainData blockchainData = blockchainTree.getBlockchainData();
    if (blockchainData.allHashes().contains(block.hash())) {
      log.warn("BLock with hash already exists.");
      return;
    }

    if (!BlockMiner.verifyBlock(block)) {
      throw new BlockNotAppendableException(block);
    }

    String transactionKey = generateTransactionsKey(block.transactionSet());
    BlockMiner blockMiner = blockMinerMap.get(transactionKey);
    if (blockMiner != null) {
      blockMiner.interrupt();
    }

    blockchainTree.addBlock(block);
  }

  public List<Block> getBlockChain() {
    return blockchainTree.getBlockchain().stream()
        .sorted(Comparator.comparingInt(Block::depth))
        .collect(Collectors.toList());
  }

  private String generateTransactionsKey(Set<Transaction> transactionSet) {
    return transactionSet.stream()
        .map(Transaction::id)
        .map(UUID::toString)
        .sorted(String::compareTo)
        .collect(Collectors.joining());
  }
}
