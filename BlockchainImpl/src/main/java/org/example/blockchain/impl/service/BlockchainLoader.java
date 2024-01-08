package org.example.blockchain.impl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.blockchain.impl.client.BlockchainNodesClient;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BlockchainLoader {

  private final BlockchainNodesClient blockchainNodesClient;
  private final BlockchainService blockchainService;

  @EventListener(InstanceRegisteredEvent.class)
  public void loadInitialBLockChain() {
    blockchainNodesClient.getBlockChain().forEach(blockchainService::addBlock);
  }
}
