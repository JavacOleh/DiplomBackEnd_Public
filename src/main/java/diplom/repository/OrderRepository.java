package diplom.repository;


import diplom.entity.order.Order;
import diplom.entity.order.OrderStatus;
import diplom.model.AnalyticsRangeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    List<Order> findByUser_Id(UUID userId);

    List<Order> findByManager_Id(UUID managerId);

    List<Order> findByRider_Id(UUID riderId);

    @Query("""
                SELECT new diplom.model.AnalyticsRangeResponse(
                    MIN(o.createdDate),
                    MAX(o.createdDate)
                )
                FROM Order o
            """)
    AnalyticsRangeResponse getRange();

    @Query("""
             SELECT o
             FROM Order o
             WHERE o.status IN :statuses
             AND o.createdDate BETWEEN :from AND :to
            """)
    List<Order> getAnalytics(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Transactional
    @Modifying
    @Query("DELETE FROM Order o WHERE o.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Transactional
    @Modifying
    @Query("UPDATE Order o SET o.manager = NULL WHERE o.manager.id = :managerId")
    void detachManager(@Param("managerId") UUID managerId);

    @Transactional
    @Modifying
    @Query("UPDATE Order o SET o.rider = NULL WHERE o.rider.id = :riderId")
    void detachRider(@Param("riderId") UUID riderId);
}
