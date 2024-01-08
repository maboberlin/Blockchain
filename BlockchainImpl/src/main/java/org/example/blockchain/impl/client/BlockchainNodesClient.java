package org.example.blockchain.impl.client;

import java.util.List;
import java.util.Set;
import org.example.blockchain.api.v1.model.TransactionDto;
import org.example.blockchain.impl.tree.Block;

public interface BlockchainNodesClient {
  void publishBlock(Block block);

  void dispatchTransactions(List<TransactionDto> transactionDtoList);

  Set<Block> getBlockChain();
}
