package diplom.model.entity.order;

import diplom.model.entity.food.Food;
import diplom.model.entity.rider.Rider;
import diplom.model.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne
    //@NotNull(message = "Rider is required")
    private Rider rider;

    /*
    Изучить почему предудщий вариант выдавал ошибку
    2025-05-13T16:39:35.830+03:00  WARN 18572 --- [webapi] [nio-8080-exec-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23502
2025-05-13T16:39:35.831+03:00 ERROR 18572 --- [webapi] [nio-8080-exec-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: null value in column "foods_order" of relation "orders_foods" violates not-null constraint
  Подробности: Failing row contains (3a74aa4a-8b86-40e9-874a-a7d6d88f3693, 7, null).
2025-05-13T16:39:35.865+03:00 ERROR 18572 --- [webapi] [nio-8080-exec-4] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.dao.DataIntegrityViolationException: could not execute statement [ERROR: null value in column "foods_order" of relation "orders_foods" violates not-null constraint
  Подробности: Failing row contains (3a74aa4a-8b86-40e9-874a-a7d6d88f3693, 7, null).] [insert into orders_foods (order_id,foods_id) values (?,?)]; SQL [insert into orders_foods (order_id,foods_id) values (?,?)]; constraint [foods_order" of relation "orders_foods]] with root cause

org.postgresql.util.PSQLException: ERROR: null value in column "foods_order" of relation "orders_foods" violates not-null constraint
  Подробности: Failing row contains (3a74aa4a-8b86-40e9-874a-a7d6d88f3693, 7, null).
	at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2733) ~[postgresql-42.7.5.jar:42.7.5]
	at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2420) ~[postgresql-42.7.5.jar:42.7.5]
	at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:372) ~[postgresql-42.7.5.jar:42.7.5]
	at org.postgresql.jdbc.PgStatement.executeInternal(PgStatement.java:517) ~[postgresql-42.7.5.jar:42.7.5]
	at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:434) ~[postgresql-42.7.5.jar:42.7.5]
	at org.postgresql.jdbc.PgPreparedStatement.executeWithFlags(PgPreparedStatement.java:194) ~[postgresql-42.7.5.jar:42.7.5]
	at org.postgresql.jdbc.PgPreparedStatement.executeUpdate(PgPreparedStatement.java:155) ~[postgresql-42.7.5.jar:42.7.5]
	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61) ~[HikariCP-6.3.0.jar:na]

	Предыдущий вариант:
	    @OneToMany
    @NotNull(message = "foods is required")
    private List<Food> foods;
     */

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @NotNull(message = "foods is required")
    private List<Food> foods;

    @NotNull(message = "Price is required")
    private double price;

    @Size(max = 255, message = "Comment should be less than 255 characters")
    private String comment;

    @NotNull(message = "CreatedDate is required")
    private String createdDate;
    private String deliveredDate;

    @NotNull(message = "restaurantName is required")
    private String restaurantName;

    @NotNull(message = "restaurantAddress is required")
    private String restaurantAddress;

    @Version
    private Long version;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    /*
    0 - created
    1 - received,
    2 - accepted(preparing),
    3 - delivering
    4 - delivered
    5 - returned
    */
}

/*
Пример запроса с клиента:

POST /api/addOrder HTTP/1.1
Content-Type: application/json

{
  "user": { "id": "3" },
  "rider": { "id": "3" },
  "price": 100.5,
  "comment": "Test Order",
  "createdDate": "2025-05-12",
  "deliveredDate": "2025-05-13",
  "restaurantName": "China Food",
  "restaurantAddress": "Ulica pushkina Dom Kolotushkina",
  "status": 0,
  "foods": [
    {
        "id": 10,
        "caption": "Cheeseburger",
        "price": 5.99,
        "imageFileName": "Test.tmp",
        "description": "A delicious cheeseburger with a juicy patty, cheese, lettuce, and tomato.",
        "version": 0
    }
  ]
}

*/
