package br.com.facef.kafka.messaging.producer;

import br.com.facef.kafka.messaging.dto.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

  @Value("${order.topic}")
  private String orderTopic;

  @Autowired private KafkaTemplate kafkaTemplate;

  public void sendToTopic(Order order) {
    this.kafkaTemplate.send(this.orderTopic, order.getOrderId(), order);
  }
}
