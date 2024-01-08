package org.example.blockchain.impl.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

public class HashFunction implements Function<HashInputData, String> {

  private final MessageDigest messageDigest;

  public HashFunction() {
    try {
      this.messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String apply(HashInputData hashInputData) {
    byte[] bytes =
        this.messageDigest.digest(hashInputData.toString().getBytes(StandardCharsets.UTF_8));
    StringBuilder stringBuffer = new StringBuilder();
    for (byte b : bytes) {
      stringBuffer.append(String.format("%02x", b));
    }
    return stringBuffer.toString();
  }
}
