package diplom.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    public List<Item> items;

    public static class Item {
        public Long goodId;
        public int quantity;
    }
}