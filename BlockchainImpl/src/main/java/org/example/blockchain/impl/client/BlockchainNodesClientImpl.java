package org.example.blockchain.impl.client;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import java.net.URI;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.blockchain.api.feign.BlockchainClient;
import org.example.blockchain.api.v1.model.BlockDto;
import org.example.blockchain.api.v1.model.TransactionDto;
import org.example.blockchain.impl.tree.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class BlockchainNodesClientImpl implements BlockchainNodesClient {

  private static final String BLOCK_PATH = "/api/v1/blockchain";
  private static final String POST_TRANSACTIONS_PATH = "/api/v1/blockchain/transactions";

  private final String appName;
  private final EurekaClient eurekaClient;
  private final ConversionService conversionService;
  private final BiFunction<URI, List<TransactionDto>, List<TransactionDto>> sendTransactions;
  private final BiFunction<URI, BlockDto, BlockDto> sendBlock;
  private final BiFunction<URI, Void, List<BlockDto>> getBlockChain;

  @Autowired
  public BlockchainNodesClientImpl(
      @Value("${spring.application.name}") String appName,
      EurekaClient eurekaClient,
      BlockchainClient blockchainClient,
      ConversionService conversionService) {
    this.appName = appName;
    this.eurekaClient = eurekaClient;
    this.conversionService = conversionService;
    this.sendTransactions = blockchainClient::postTransactions;
    this.sendBlock = blockchainClient::postBlock;
    this.getBlockChain = (uri, unused) -> blockchainClient.getBlockChain(uri);
  }

  @Override
  public void publishBlock(Block block) {
    log.info("Publishing block to other nodes");
    BlockDto blockDto = conversionService.convert(block, BlockDto.class);
    sendRequest(sendBlock, blockDto, BLOCK_PATH, false);
  }

  @Override
  public void dispatchTransactions(List<TransactionDto> transactionDtoList) {
    log.info("Dispatching transactions to other nodes");
    sendRequest(sendTransactions, transactionDtoList, POST_TRANSACTIONS_PATH, false);
  }

  @Override
  public Set<Block> getBlockChain() {
    log.info("Fetching blockchain from other nodes");
    Map<String, List<BlockDto>> blockChainMap = sendRequest(getBlockChain, null, BLOCK_PATH, true);
    return blockChainMap.values().stream().findFirst().orElse(emptyList()).stream()
        .map(blockDto -> conversionService.convert(blockDto, Block.class))
        .collect(Collectors.toSet());
  }

  private <S, T> Map<String, T> sendRequest(
      BiFunction<URI, S, T> function, S postBody, String path, boolean firstSuccessfulNode) {
    String thisInstanceId = eurekaClient.getApplicationInfoManager().getInfo().getInstanceId();
    Map<String, T> responseForEachInstance = new HashMap<>();
    List<InstanceInfo> instances =
        ofNullable(eurekaClient.getApplication(appName))
            .map(Application::getInstances)
            .orElse(emptyList());
    for (InstanceInfo instanceInfo : instances) {
      T response =
          sendRequestToOtherInstance(function, postBody, path, thisInstanceId, instanceInfo);
      if (response != null) {
        responseForEachInstance.put(instanceInfo.getInstanceId(), response);
        if (firstSuccessfulNode) {
          break;
        }
      }
    }
    return responseForEachInstance;
  }

  private <S, T> T sendRequestToOtherInstance(
      BiFunction<URI, S, T> function,
      S postBody,
      String path,
      String thisInstanceId,
      InstanceInfo instanceInfo) {
    if (!thisInstanceId.equalsIgnoreCase(instanceInfo.getInstanceId())) {
      String hostName = instanceInfo.getHostName();
      int port = instanceInfo.getPort();
      UriComponents uriComponents =
          UriComponentsBuilder.newInstance()
              .scheme("http")
              .host(hostName)
              .port(port)
              .path(path)
              .build();
      URI uri = uriComponents.toUri();
      log.info("Sending request to node: {}", uri);
      try {
        return function.apply(uri, postBody);
      } catch (Exception exception) {
        log.warn("Request to node '{}' failed.", uri, exception);
      }
    }
    return null;
  }
}
