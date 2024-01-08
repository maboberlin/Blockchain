package org.example.blockchain.impl.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.blockchain.api.v1.api.BlockchainApi;
import org.example.blockchain.api.v1.model.BlockDto;
import org.example.blockchain.api.v1.model.BlockList;
import org.example.blockchain.api.v1.model.TransactionDto;
import org.example.blockchain.api.v1.model.TransactionList;
import org.example.blockchain.impl.client.BlockchainNodesClient;
import org.example.blockchain.impl.service.BlockchainService;
import org.example.blockchain.impl.tree.Block;
import org.example.blockchain.impl.tree.Transaction;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class BlockchainApiV1Controller implements BlockchainApi {

  private final BlockchainService blockchainService;
  private final BlockchainNodesClient blockchainNodesClient;
  private final ConversionService conversionService;

  @Override
  public ResponseEntity<BlockList> getBlockList() {
    List<Block> blockChain = blockchainService.getBlockChain();
    BlockList blockList = buildBlockDtoList(blockChain);
    return ResponseEntity.of(Optional.of(blockList));
  }

  @Override
  public ResponseEntity<BlockDto> postBlock(BlockDto blockDto) {
    Block block = conversionService.convert(blockDto, Block.class);
    blockchainService.addBlock(block);
    return ResponseEntity.of(Optional.of(blockDto));
  }

  @Override
  public ResponseEntity<TransactionList> postTransactions(
      List<TransactionDto> transactionDtoList, Boolean reDispatch) {
    if (reDispatch == null || reDispatch) {
      blockchainNodesClient.dispatchTransactions(transactionDtoList);
    }
    Set<Transaction> txData = convertTransactions(transactionDtoList);
    blockchainService.mineBlock(txData);
    return ResponseEntity.of(Optional.of(buildTransactionList(transactionDtoList)));
  }

  private static TransactionList buildTransactionList(List<TransactionDto> transactionDtoList) {
    TransactionList transactionList = new TransactionList();
    transactionList.addAll(transactionDtoList);
    return transactionList;
  }

  private BlockList buildBlockDtoList(List<Block> blockChain) {
    List<BlockDto> blockDtoList =
        blockChain.stream().map(block -> conversionService.convert(block, BlockDto.class)).toList();
    BlockList blockList = new BlockList();
    blockList.addAll(blockDtoList);
    return blockList;
  }

  private Set<Transaction> convertTransactions(List<TransactionDto> transactionDtoList) {
    return transactionDtoList.stream()
        .map(dto -> conversionService.convert(dto, Transaction.class))
        .collect(Collectors.toSet());
  }
}
