package io.akikr.app.order.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderException extends RuntimeException {

  private int status;
  private String message;
  private String path;

  public OrderException(int status, Throwable error, String message, String path) {
    super(error);
    this.status = status;
    this.message = message;
    this.path = path;
  }
}
