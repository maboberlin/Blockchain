package org.example.blockchain.impl.hash;

import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HashVerifier implements Predicate<String> {

  private final int hashVerifierMultiplier;
  private static final String PREFIX_CHARACTER = "0";

  @Override
  public boolean test(String hash) {
    String prefixString = PREFIX_CHARACTER.repeat(hashVerifierMultiplier);
    return hash.startsWith(prefixString);
  }
}
