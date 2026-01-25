package io.akikr.app.tracking.repository;

import io.akikr.app.tracking.entity.Tracking;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingRepository extends JpaRepository<Tracking, UUID> {}
