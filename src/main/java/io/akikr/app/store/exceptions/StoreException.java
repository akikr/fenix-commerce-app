package io.akikr.app.store.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StoreException extends RuntimeException {
    private int status;
    private String message;
    private String path;

    public StoreException(int status, Throwable error, String message, String path) {
        super(error);
        this.status = status;
        this.message = message;
        this.path = path;
    }
}
