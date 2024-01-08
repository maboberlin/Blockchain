package org.example.blockchain.api.feign;

import feign.Headers;
import feign.RequestLine;
import java.net.URI;
import java.util.List;
import org.example.blockchain.api.v1.model.BlockDto;
import org.example.blockchain.api.v1.model.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "blockchain-client")
public interface BlockchainClient {

  @RequestLine("POST")
  @Headers({"Content-Type: application/json", "Accept: */*"})
  BlockDto postBlock(URI uri, BlockDto blockDto);

  @RequestLine("POST")
  @Headers({"Content-Type: application/json", "Accept: */*", "X-Re-Dispatch:false"})
  List<TransactionDto> postTransactions(URI uri, List<TransactionDto> transactionDtoList);

  @RequestLine("GET")
  @Headers({"Content-Type: application/json", "Accept: */*"})
  List<BlockDto> getBlockChain(URI uri);
}
