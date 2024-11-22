//package streamingsettlement.streaming.domain.entity;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class PlayHistoryTest {
//
//    @Test
//    @DisplayName("광고는 영상 7분마다 1개가 들어가고 450초 부터 재생했을때 " +
//            "마지막 재생 시점이 15분이면 List는 840 1개가 들어가 있다.")
//    void calculateNewAdPositions(){
//        //given
//        int newPlayTime = 900;
//        int adInterval = 420;
//        PlayHistory playHistory = PlayHistory.builder().lastAdPlayTime(450).build();
//        //when
//        List<Integer> adPositions = playHistory.updatePlayTime(newPlayTime, adInterval);
//        //then
//        assertThat(adPositions.size()).isEqualTo(1);
//        assertThat(adPositions.get(0)).isEqualTo(840);
//    }
//
//    @Test
//    @DisplayName("광고는 영상 7분마다 1개가 들어가고 처음부터 재생했을때 " +
//            "마지막 재생 시점이 15분이면 List는 420,840 2개가 들어가 있다.")
//    void calculateNewAdPositions_AllPlay(){
//        //given
//        int newPlayTime = 900;
//        int adInterval = 420;
//        PlayHistory playHistory = PlayHistory.builder().lastAdPlayTime(0).build();
//        //when
//        List<Integer> adPositions = playHistory.updatePlayTime(newPlayTime, adInterval);
//        //then
//        assertThat(adPositions.size()).isEqualTo(2);
//        List<Integer> list = List.of(420, 840);
//        assertThat(adPositions.get(0)).isEqualTo(420);
//        assertThat(adPositions.get(1)).isEqualTo(840);
//        assertThat(adPositions).isEqualTo(list);
//    }
//
//    @Test
//    @DisplayName("광고는 영상 7분마다 1개가 마지막 재생 시점이 6분 50초이면 List는 비어있다.")
//    void calculateNewAdPositions_1(){
//        //given
//        int newPlayTime = 410;
//        int adInterval = 420;
//        PlayHistory playHistory = PlayHistory.builder().lastAdPlayTime(410).build();
//        //when
//        List<Integer> adPositions = playHistory.updatePlayTime(newPlayTime, adInterval);
//        //then
//        assertThat(adPositions.size()).isEqualTo(0);
//        assertThat(adPositions).isEmpty();
//    }
//
//}