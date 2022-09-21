package de.genios;

import de.genios.helper.Constants;
import de.genios.helper.ExceptionConstants;
import de.genios.model.BowlingGame;
import de.genios.model.Frame;
import de.genios.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

class BowlingGameTest {
    static BowlingGame bowlingGame;

    @BeforeEach
    public void beforeEach() {
        bowlingGame = new BowlingGame();
    }

    @Test
    public void findFrames_WhenNotRolled_ShouldReturnTheEmptyList() {
        // ACT
        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5, 8})
    public void findFrames_WhenARegularFirstRoll_ShouldReturnTheCorrectScores(int pins) {
        // ACT
        bowlingGame.roll(pins);

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isNotEmpty();
        assertThat(frames.get(0).getFirstRollScore()).isNotZero().isEqualTo(pins);
        assertThat(frames.get(0).getSecondRollScore()).isZero();

        assertThat(frames.get(0).isStrike()).isFalse();
        assertThat(frames.get(0).isSpare()).isFalse();
        assertThat(frames.get(0).getStrikeSpareBonusScore()).isZero();
        assertThat(frames.get(0).getBonusRollScoreLastStrike()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"1,2,3", "3,4,7", "5,3,8", "8,1,9"})
    public void findFrames_WhenARegularSecondRolled_ShouldReturnTheCorrectScores(int roll1, int roll2, int expectedFrameScore) {
        //ACT
        bowlingGame.roll(roll1);
        bowlingGame.roll(roll2);

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isNotEmpty();
        assertThat(frames.get(0).getFirstRollScore()).isNotZero().isEqualTo(roll1);
        assertThat(frames.get(0).isStrike()).isFalse();
        assertThat(frames.get(0).isSpare()).isFalse();
        assertThat(frames.get(0).getSecondRollScore()).isNotZero().isEqualTo(roll2);
        assertThat(frames.get(0).getIndividualFrameScore()).isNotZero().isEqualTo(expectedFrameScore);
    }


    @ParameterizedTest
    @CsvSource({"1,9", "3,7", "5,5", "8,2"})
    public void findFrames_WhenASpareRolled_ShouldReturnTheCorrectScores(int roll1, int roll2) {
        //ACT
        bowlingGame.roll(roll1);
        bowlingGame.roll(roll2);

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isNotEmpty();
        assertThat(frames.get(0).getFirstRollScore()).isNotZero().isEqualTo(roll1);
        assertThat(frames.get(0).isStrike()).isFalse();
        assertThat(frames.get(0).isSpare()).isTrue();
        assertThat(frames.get(0).getSecondRollScore()).isNotZero().isEqualTo(roll2);
        assertThat(frames.get(0).getIndividualFrameScore()).isNotZero().isEqualTo(Constants.TOTAL_NUMBER_OF_PINS);
    }


    @Test
    public void findFrames_WhenAStrikeRolled_ShouldReturnTheCorrectScores() {
        //ACT
        bowlingGame.roll(Constants.TOTAL_NUMBER_OF_PINS);

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isNotEmpty();
        assertThat(frames.get(0).getFirstRollScore()).isNotZero().isEqualTo(Constants.TOTAL_NUMBER_OF_PINS);
        assertThat(frames.get(0).isStrike()).isTrue();
        assertThat(frames.get(0).isSpare()).isFalse();
        assertThat(frames.get(0).getSecondRollScore()).isZero();
        assertThat(frames.get(0).getIndividualFrameScore()).isNotZero().isEqualTo(Constants.TOTAL_NUMBER_OF_PINS);
    }

    @ParameterizedTest
    @CsvSource({"1,9,2,1,12", "3,7,3,1,13", "5,5,4,1,14", "8,2,5,1,15"})
    public void findFrames_WhenASpareRolled_ShouldAddScoreFromNextRoll(int roll1, int roll2, int nextFrameRoll1, int nextFrameRoll2, int expectedFrameScore) {
        //ACT
        bowlingGame.roll(roll1);
        bowlingGame.roll(roll2);

        bowlingGame.roll(nextFrameRoll1);
        bowlingGame.roll(nextFrameRoll2);

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isNotEmpty();
        assertThat(frames.get(0).isSpare()).isTrue();

        assertThat(frames.get(0).getStrikeSpareBonusScore()).isNotZero().isEqualTo(nextFrameRoll1);
        assertThat(frames.get(0).getIndividualFrameScore()).isNotZero().isEqualTo(expectedFrameScore);

    }

    @ParameterizedTest
    @CsvSource({"2,1,13", "3,1,14", "4,1,15", "5,1,16"})
    public void findFrames_WhenAStrikeRolled_ShouldAddScoreFromNextTwoRoll(int nextFrameRoll1, int nextFrameRoll2, int expectedFrameScore) {
        //ACT
        bowlingGame.roll(Constants.TOTAL_NUMBER_OF_PINS);

        bowlingGame.roll(nextFrameRoll1);
        bowlingGame.roll(nextFrameRoll2);

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isNotEmpty();
        assertThat(frames.get(0).getStrikeSpareBonusScore()).isNotZero().isEqualTo(nextFrameRoll1 + nextFrameRoll2);
        assertThat(frames.get(0).getIndividualFrameScore()).isNotZero().isEqualTo(expectedFrameScore);
    }

    @ParameterizedTest
    @CsvSource({"2,2,12,22", "3,1,13,23", "4,1,14,24", "5,1,15,25"})
    public void findFrames_WhenAStrikeRolledTwice_ShouldAddScoreFromNextTwoRoll(int nextNextFrameRoll1, int nextNextFrameRoll2, int expectedFrameBonus, int expectedFrameScore) {
        //ACT
        bowlingGame.roll(Constants.TOTAL_NUMBER_OF_PINS);

        bowlingGame.roll(Constants.TOTAL_NUMBER_OF_PINS);

        bowlingGame.roll(nextNextFrameRoll1);
        bowlingGame.roll(nextNextFrameRoll2);

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isNotEmpty();
        assertThat(frames.get(0).getStrikeSpareBonusScore()).isNotZero().isEqualTo(expectedFrameBonus);
        assertThat(frames.get(0).getIndividualFrameScore()).isNotZero().isEqualTo(expectedFrameScore);
    }

    @ParameterizedTest
    @CsvSource({"9,2", "8,4"})
    public void findFrames_WhenPinCountInvalid_ShouldThrowException(int roll1, int roll2) {
        // ASSERT
        assertThatThrownBy(() -> {
            // ACT
            bowlingGame.roll(roll1);
            bowlingGame.roll(roll2);
        }).isInstanceOf(IllegalStateException.class).hasMessage(ExceptionConstants.PIN_COUNT_EXCEEDING);


        assertThatThrownBy(() -> {
            // ACT
            bowlingGame.roll(11);
        }).isInstanceOf(IllegalStateException.class).hasMessage(ExceptionConstants.PIN_COUNT_EXCEEDING);
    }

    @ParameterizedTest
    @CsvSource({"11", "18", "100", "20"})
    public void findFrames_WhenFirstRollPinCountInvalid_ShouldThrowException(int roll) {
        //ASSERT
        assertThatThrownBy(() -> {
            // ACT
            bowlingGame.roll(roll);
        }).isInstanceOf(IllegalStateException.class).hasMessage(ExceptionConstants.PIN_COUNT_EXCEEDING);
    }

    @ParameterizedTest
    @CsvSource({"-11", "-18", "-100", "-20"})
    public void findFrames_WhenFirstRollInvalidCount_ShouldThrowException(int roll) {
        // ASSERT
        assertThatThrownBy(() -> {
            // ACT
            bowlingGame.roll(roll);
        }).isInstanceOf(IllegalStateException.class).hasMessage(ExceptionConstants.INVALID_ROLL);
    }

    @ParameterizedTest
    @CsvSource({"2,3", "4,5"})
    public void findFrames_WhenGaneReset_ShouldClearFrames(int roll1, int roll2) {
        //ARRANGE
        bowlingGame.roll(roll1);
        bowlingGame.roll(roll2);

        //ACT
        bowlingGame.resetGame();

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isEmpty();
        assertThat(bowlingGame.getGameState()).isEqualTo(GameState.FIRST_ROLL);
        assertThat(bowlingGame.getCurrentFrame()).isNull();
        assertThat(bowlingGame.getPreviousFrame()).isNull();
        assertThat(bowlingGame.getPreviousPreviousFrame()).isNull();
    }


    @Test
    public void findFrames_WhenAStrikeRolledTwice_ShouldCalculateCorrectScoreboard() {
        //ACT
        bowlingGame.roll(8);
        bowlingGame.roll(0);

        bowlingGame.roll(7);
        bowlingGame.roll(0);

        bowlingGame.roll(5);
        bowlingGame.roll(3);

        bowlingGame.roll(9);
        bowlingGame.roll(1);

        bowlingGame.roll(9);
        bowlingGame.roll(1);

        bowlingGame.roll(10);

        bowlingGame.roll(8);
        bowlingGame.roll(0);

        bowlingGame.roll(5);
        bowlingGame.roll(1);

        bowlingGame.roll(3);
        bowlingGame.roll(7);

        bowlingGame.roll(9);
        bowlingGame.roll(0);

        List<Frame> frames = bowlingGame.getFrames();

        // ASSERT
        assertThat(frames).isNotEmpty();
        assertThat(frames.get(0).getAccumulatedFrameScore()).isNotZero().isEqualTo(8);
        assertThat(frames.get(1).getAccumulatedFrameScore()).isNotZero().isEqualTo(15);
        assertThat(frames.get(2).getAccumulatedFrameScore()).isNotZero().isEqualTo(23);
        assertThat(frames.get(3).getAccumulatedFrameScore()).isNotZero().isEqualTo(42);
        assertThat(frames.get(4).getAccumulatedFrameScore()).isNotZero().isEqualTo(62);
        assertThat(frames.get(5).getAccumulatedFrameScore()).isNotZero().isEqualTo(80);
        assertThat(frames.get(6).getAccumulatedFrameScore()).isNotZero().isEqualTo(88);
        assertThat(frames.get(7).getAccumulatedFrameScore()).isNotZero().isEqualTo(94);
        assertThat(frames.get(8).getAccumulatedFrameScore()).isNotZero().isEqualTo(113);
        assertThat(frames.get(9).getAccumulatedFrameScore()).isNotZero().isEqualTo(122);
    }
}
