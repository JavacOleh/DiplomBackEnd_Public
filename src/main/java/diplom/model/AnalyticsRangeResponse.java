package diplom.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnalyticsRangeResponse {

    private LocalDateTime minDate;
    private LocalDateTime maxDate;
}