package io.akikr.app.tracking.service;

public interface TrackingEventService {

    String generateEventHash(String payload) throws Exception;
}
