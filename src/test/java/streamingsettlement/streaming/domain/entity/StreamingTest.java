package streamingsettlement.streaming.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StreamingTest {

    @Test
    @DisplayName("increaseView() 실행시 조회수 1이 증가한다.")
    void increaseView(){
        //given
        int totalViews = 50;
        Streaming streaming = Streaming.builder().totalViews(totalViews).build();
        //when
        streaming.increaseView();
        //then
        Assertions.assertThat(streaming.getTotalViews()).isEqualTo(totalViews+1);
    }

}