package org.example.blockchain.impl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class BlockchainApplication {

  public static void main(String[] args) {
    String s = UUID.randomUUID().toString();

    SpringApplication.run(BlockchainApplication.class, args);
  }
}
