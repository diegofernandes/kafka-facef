package br.com.facef.kafka.messaging.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class Order implements Serializable {

  private String orderId;
  private String paymentType;
  private BigDecimal value;
}
