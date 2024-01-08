package org.example.blockchain.impl.config;

import static feign.Feign.builder;

import feign.Logger;
import feign.Target;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.example.blockchain.api.feign.BlockchainClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableDiscoveryClient
@Configuration
public class BlockchainApplicationConfiguration {

  @Bean
  public BlockchainClient blockchainClient() {
    return builder()
        .client(new OkHttpClient())
        .encoder(new GsonEncoder())
        .decoder(new GsonDecoder())
        .logger(new Slf4jLogger(BlockchainClient.class))
        .logLevel(Logger.Level.FULL)
        .target(Target.EmptyTarget.create(BlockchainClient.class));
  }
}
