package streamingsettlement.batch.calculator;

import java.math.BigDecimal;

public class SettlementCalculator {

    private static class ViewThreshold {
        private static final long TIER_1 = 100_000;
        private static final long TIER_2 = 500_000;
        private static final long TIER_3 = 1_000_000;
    }

    private static class StreamingPrice {
        private static final BigDecimal BASE = BigDecimal.valueOf(1.0);
        private static final BigDecimal TIER_1 = BigDecimal.valueOf(1.1);
        private static final BigDecimal TIER_2 = BigDecimal.valueOf(1.3);
        private static final BigDecimal TIER_3 = BigDecimal.valueOf(1.5);
    }

    private static class AdPrice {
        private static final BigDecimal BASE = BigDecimal.valueOf(10);
        private static final BigDecimal TIER_1 = BigDecimal.valueOf(12);
        private static final BigDecimal TIER_2 = BigDecimal.valueOf(15);
        private static final BigDecimal TIER_3 = BigDecimal.valueOf(20);
    }

    public static BigDecimal calculateStreamingUnitPrice(long views) {
        if (views >= ViewThreshold.TIER_3) return StreamingPrice.TIER_3;
        if (views >= ViewThreshold.TIER_2) return StreamingPrice.TIER_2;
        if (views >= ViewThreshold.TIER_1) return StreamingPrice.TIER_1;
        return StreamingPrice.BASE;
    }

    public static BigDecimal calculateAdUnitPrice(long views) {
        if (views >= ViewThreshold.TIER_3) return AdPrice.TIER_3;
        if (views >= ViewThreshold.TIER_2) return AdPrice.TIER_2;
        if (views >= ViewThreshold.TIER_1) return AdPrice.TIER_1;
        return AdPrice.BASE;
    }
}