package org.example.blockchain.impl.converter;

import org.example.blockchain.api.v1.model.TransactionDto;
import org.example.blockchain.impl.tree.Transaction;
import org.springframework.core.convert.converter.Converter;

public class TransactionToTransactionDtoConverter
    implements Converter<Transaction, TransactionDto> {
  @Override
  public TransactionDto convert(Transaction source) {
    TransactionDto transactionDto = new TransactionDto();
    transactionDto.setId(source.id());
    transactionDto.setData(source.data());
    return transactionDto;
  }
}
