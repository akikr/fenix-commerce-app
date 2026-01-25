package io.akikr.app.tracking.repository;

import io.akikr.app.tracking.entity.TrackingEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, UUID> {}
