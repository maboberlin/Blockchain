package org.example.blockchain.impl.tree;

import static java.util.Collections.synchronizedList;

import java.util.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlockchainNode {

  @Getter private final BlockchainNode parent;

  private final List<BlockchainNode> children = synchronizedList(new ArrayList<>());

  @Getter private final Block block;

  public void addToChildren(BlockchainNode blockchainNode) {
    children.add(blockchainNode);
  }
}
