package org.example.blockchain.impl.converter;

import org.example.blockchain.api.v1.model.TransactionDto;
import org.example.blockchain.impl.tree.Transaction;
import org.springframework.core.convert.converter.Converter;

public class TransactionDtoToTransactionConverter
    implements Converter<TransactionDto, Transaction> {
  @Override
  public Transaction convert(TransactionDto source) {
    return new Transaction(source.getId(), source.getData());
  }
}
