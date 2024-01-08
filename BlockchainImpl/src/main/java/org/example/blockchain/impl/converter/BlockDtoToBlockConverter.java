package org.example.blockchain.impl.converter;

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.blockchain.api.v1.model.BlockDto;
import org.example.blockchain.impl.tree.Block;
import org.example.blockchain.impl.tree.Transaction;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class BlockDtoToBlockConverter implements Converter<BlockDto, Block> {

  private final ConversionService conversionService;

  @Override
  public Block convert(BlockDto source) {
    Set<Transaction> transactionList =
        ofNullable(source.getTransactions()).orElse(Collections.emptyList()).stream()
            .map(dto -> conversionService.convert(dto, Transaction.class))
            .collect(Collectors.toSet());
    return new Block(
        source.getHash(),
        source.getPreviousHash(),
        transactionList,
        source.getDepth(),
        source.getTimestamp(),
        source.getNonce(),
        source.getMinedBy());
  }
}
