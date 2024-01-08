package org.example.blockchain.impl.config;

import org.example.blockchain.impl.converter.BlockDtoToBlockConverter;
import org.example.blockchain.impl.converter.BlockToBlockDtoConverter;
import org.example.blockchain.impl.converter.TransactionDtoToTransactionConverter;
import org.example.blockchain.impl.converter.TransactionToTransactionDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired @Lazy private ConversionService conversionService;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(getBlockDtoToBlockConverter());
    registry.addConverter(getBlockToBlockDtoConverter());
    registry.addConverter(getTransactionDtoToTransactionConverter());
    registry.addConverter(getTransactionToTransactionDtoConverter());
  }

  @Bean
  public BlockDtoToBlockConverter getBlockDtoToBlockConverter() {
    return new BlockDtoToBlockConverter(conversionService);
  }

  @Bean
  public BlockToBlockDtoConverter getBlockToBlockDtoConverter() {
    return new BlockToBlockDtoConverter(conversionService);
  }

  @Bean
  public TransactionDtoToTransactionConverter getTransactionDtoToTransactionConverter() {
    return new TransactionDtoToTransactionConverter();
  }

  @Bean
  public TransactionToTransactionDtoConverter getTransactionToTransactionDtoConverter() {
    return new TransactionToTransactionDtoConverter();
  }
}
