package io.akikr.app.fulfillment.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FulfillmentException extends RuntimeException {
  private int status;
  private String message;
  private String path;

  public FulfillmentException(int status, Throwable error, String message, String path) {
    super(error);
    this.status = status;
    this.message = message;
    this.path = path;
  }
}
