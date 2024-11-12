package streamingsettlement.adjustment.domain;

import streamingsettlement.adjustment.domain.entity.DailyStatistic;

public interface DailyStatisticRepository {
    DailyStatistic save(DailyStatistic dailyStatistic);
}
