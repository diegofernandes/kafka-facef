package br.com.facef.kafka.messaging.controller;

import br.com.facef.kafka.messaging.dto.Order;
import br.com.facef.kafka.messaging.producer.OrderProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
public class OrderController {

  @Autowired private OrderProducer producer;

  @PostMapping
  public ResponseEntity placeOrder(@RequestBody Order order) {
    producer.sendToTopic(order);
    return ResponseEntity.accepted().build();
  }
}
