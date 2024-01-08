package org.example.blockchain.impl.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.blockchain.impl.tree.Block;

@Getter
@AllArgsConstructor
public class BlockNotAppendableException extends RuntimeException {
  private Block block;
}
