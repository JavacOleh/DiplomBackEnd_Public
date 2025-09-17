package diplom.model.entity.food;

import diplom.model.entity.order.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Foods")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "caption is required")
    private String caption;

    @NotNull(message = "price is required")
    private double price;

    @NotNull(message = "imageFileName is required")
    private String imageFileName; //Проверки на то существует ли такая картинка при добавлении еды - нету.

    private String description;

    @Version
    private Long version;
}

/*
Пример запроса с клиента:

POST /api/addOrder HTTP/1.1
Content-Type: application/json

{
  "caption": "Cheeseburger",
  "price": 5.99,
  "imageFileName" : "Test.tmp",
  "description": "A delicious cheeseburger with a juicy patty, cheese, lettuce, and tomato."
}

*/
