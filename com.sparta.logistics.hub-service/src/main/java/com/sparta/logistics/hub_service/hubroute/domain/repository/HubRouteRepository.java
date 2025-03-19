package com.sparta.logistics.hub_service.hubroute.domain.repository;

import com.sparta.logistics.hub_service.hubroute.domain.entity.HubRoute;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRouteRepository extends JpaRepository<HubRoute, UUID> {

  boolean existsByStartHubId(UUID startHubId);

  boolean existsByEndHubId(UUID endHubId);

  List<HubRoute> findByStartHubIdOrEndHubId(UUID startHubId, UUID endHubId);

  List<HubRoute> findByStartHubIdAndEndHubId(UUID startHubId, UUID endHubId);
}
