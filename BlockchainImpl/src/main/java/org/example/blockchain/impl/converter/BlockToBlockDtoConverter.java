package org.example.blockchain.impl.converter;

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.blockchain.api.v1.model.BlockDto;
import org.example.blockchain.api.v1.model.TransactionDto;
import org.example.blockchain.impl.tree.Block;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class BlockToBlockDtoConverter implements Converter<Block, BlockDto> {

  private final ConversionService conversionService;

  @Override
  public BlockDto convert(Block source) {
    List<TransactionDto> transactionList =
        ofNullable(source.transactionSet()).orElse(Collections.emptySet()).stream()
            .map(dto -> conversionService.convert(dto, TransactionDto.class))
            .collect(Collectors.toList());
    BlockDto blockDto = new BlockDto();
    blockDto.setTransactions(transactionList);
    blockDto.setTimestamp(source.timestamp());
    blockDto.setHash(source.hash());
    blockDto.setPreviousHash(source.previousHash());
    blockDto.setNonce(source.nonce());
    blockDto.setDepth(source.depth());
    blockDto.setMinedBy(source.minedBy());
    return blockDto;
  }
}
