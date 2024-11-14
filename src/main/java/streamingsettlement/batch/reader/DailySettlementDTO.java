package streamingsettlement.batch.reader;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailySettlementDTO {
    private final Long streamingId;
    private final Long viewCount;
}
