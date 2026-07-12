package diplom.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodAnalyticsItemDto {
    private Long goodId;
    private String caption;
    private String imageFileName;
    private Long soldQuantity;
    private Double revenue;
}