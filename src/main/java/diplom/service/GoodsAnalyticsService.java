package diplom.service;

import diplom.model.GoodAnalyticsItemDto;
import diplom.model.PageResponse;
import diplom.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class GoodsAnalyticsService {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public GoodsAnalyticsService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public PageResponse<GoodAnalyticsItemDto> getGoodsAnalytics(int page, int size) {
        page = Math.max(page, 0);
        size = Math.max(1, Math.min(size, 50));

        var result = orderItemRepository.findGoodsAnalytics(
                PageRequest.of(page, size)
        );

        return PageResponse.from(result);
    }
}