package org.nectarframework.base.service.nanohttp;

import org.nectarframework.base.exception.NectarException;

public final class ResponseException extends NectarException {

    private static final long serialVersionUID = 6569838532917408380L;

    private final Status status;

    public ResponseException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public ResponseException(Status status, String message, Exception e) {
        super(message, e);
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }
}