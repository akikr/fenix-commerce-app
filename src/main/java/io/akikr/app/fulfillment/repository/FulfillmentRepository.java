package io.akikr.app.fulfillment.repository;

import io.akikr.app.fulfillment.entity.Fulfillment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, UUID> {}
