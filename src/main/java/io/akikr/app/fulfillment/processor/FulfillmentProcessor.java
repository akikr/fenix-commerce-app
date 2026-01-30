package io.akikr.app.fulfillment.processor;

import io.akikr.app.fulfillment.entity.Fulfillment;
import io.akikr.app.fulfillment.repository.FulfillmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FulfillmentProcessor {

  private final FulfillmentRepository fulfillmentRepository;

  public FulfillmentProcessor(FulfillmentRepository fulfillmentRepository) {
    this.fulfillmentRepository = fulfillmentRepository;
  }

  @Transactional(rollbackFor = Exception.class)
  public Fulfillment createFulfillmentOrder(Fulfillment fulfillment) {
    return fulfillmentRepository.save(fulfillment);
  }
}
