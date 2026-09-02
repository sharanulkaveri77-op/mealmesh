package com.mealmesh.delivery.repository;

import com.mealmesh.delivery.entity.DeliveryAssignment;
import com.mealmesh.delivery.entity.DeliveryLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryLocationRepository extends JpaRepository<DeliveryLocation, UUID> {

    Optional<DeliveryLocation> findFirstByDeliveryAssignmentOrderByRecordedAtDesc(DeliveryAssignment assignment);

    List<DeliveryLocation> findTop50ByDeliveryAssignmentOrderByRecordedAtDesc(DeliveryAssignment assignment);

    List<DeliveryLocation> findByDeliveryAssignmentOrderByRecordedAtAsc(DeliveryAssignment assignment);
}