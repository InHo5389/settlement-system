package streamingsettlement.adjustment.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import streamingsettlement.adjustment.domain.DailyStatisticRepository;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.adjustment.repository.jpa.DailyStatisticJpaRepository;

@Repository
@RequiredArgsConstructor
public class DailyStatisticRepositoryImpl implements DailyStatisticRepository {

    private final DailyStatisticJpaRepository dailyStatisticJpaRepository;

    public DailyStatistic save(DailyStatistic dailyStatistic){
        return dailyStatisticJpaRepository.save(dailyStatistic);
    }
}
