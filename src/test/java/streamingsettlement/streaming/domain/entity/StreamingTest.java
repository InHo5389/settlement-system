package streamingsettlement.streaming.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StreamingTest {

    @Test
    @DisplayName("updateViewCount(view) 실행시 기존 조회수 + view로 증가한다.")
    void increaseView(){
        //given
        int totalViews = 50;
        Streaming streaming = Streaming.builder().streamingViews(totalViews).build();
        //when
        int view = 30;
        streaming.updateViewCount((long) view);
        //then
        Assertions.assertThat(streaming.getStreamingViews()).isEqualTo(totalViews+view);
    }

}