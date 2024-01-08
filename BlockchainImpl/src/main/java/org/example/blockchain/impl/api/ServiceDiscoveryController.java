package org.example.blockchain.impl.api;

import com.netflix.discovery.EurekaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/discovery")
@RequiredArgsConstructor
public class ServiceDiscoveryController {

  private final EurekaClient eurekaClient;

  @RequestMapping("/service-name")
  public String serviceName() {
    String instanceId = eurekaClient.getApplicationInfoManager().getInfo().getInstanceId();
    return "Service name: %s".formatted(instanceId);
  }
}
