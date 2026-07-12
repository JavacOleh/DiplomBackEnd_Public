package diplom.repository;

import diplom.entity.order.OrderItem;
import diplom.model.GoodAnalyticsItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByGood_Id(Long goodId);

    @Query(
            value = """
        SELECT new diplom.model.GoodAnalyticsItemDto(
            oi.good.id,
            oi.good.caption,
            oi.good.imageFileName,
            SUM(oi.quantity),
            SUM(oi.quantity * oi.priceAtOrder)
        )
        FROM OrderItem oi
        WHERE oi.order.status != diplom.entity.order.OrderStatus.DECLINED
        GROUP BY oi.good.id, oi.good.caption, oi.good.imageFileName
        ORDER BY SUM(oi.quantity) DESC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT oi.good.id)
        FROM OrderItem oi
        WHERE oi.order.status != diplom.entity.order.OrderStatus.DECLINED
    """
    )
    Page<GoodAnalyticsItemDto> findGoodsAnalytics(Pageable pageable);
}