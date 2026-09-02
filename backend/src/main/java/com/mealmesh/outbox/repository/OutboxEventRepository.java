package com.mealmesh.outbox.repository;

import com.mealmesh.outbox.entity.OutboxEvent;
import com.mealmesh.outbox.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Fetch PENDING events ordered by creation time, for the publisher to process.
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);

    /**
     * Fetch FAILED events that have not exceeded the retry limit, ordered by creation time.
     */
    List<OutboxEvent> findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            OutboxStatus status, int maxRetryCount, Pageable pageable);

    /**
     * Combined query: pending OR (failed with retries remaining).
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' " +
           "OR (e.status = 'FAILED' AND e.retryCount < :maxRetries) " +
           "ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPublishableEvents(@Param("maxRetries") int maxRetries, Pageable pageable);

    /**
     * Find outbox events for a specific aggregate.
     */
    List<OutboxEvent> findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(
            String aggregateType, UUID aggregateId);
}
