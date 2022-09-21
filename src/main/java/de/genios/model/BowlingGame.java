package de.genios.model;

import de.genios.helper.Constants;
import de.genios.helper.ExceptionConstants;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BowlingGame {
    private final List<Frame> frames = new ArrayList<>();
    private GameState gameState = GameState.FIRST_ROLL;

    //we need the reference to two previous frames in order to update their scores in case of strike or spare
    private Frame currentFrame = null;
    private Frame previousFrame = null;
    private Frame previousPreviousFrame = null;

    public void resetGame() {
        gameState = GameState.FIRST_ROLL;
        currentFrame = null;
        previousFrame = null;
        previousPreviousFrame = null;
        frames.clear();
    }

    public void roll(final int pins) {
        if (pins < 0) {
            throw new IllegalStateException(ExceptionConstants.INVALID_ROLL);
        }
        if (pins > Constants.TOTAL_NUMBER_OF_PINS) {
            throw new IllegalStateException(ExceptionConstants.PIN_COUNT_EXCEEDING);
        }
        switch (gameState) {
            case FIRST_ROLL:
                handleFirst(pins);
                break;
            case SECOND_ROLL:
                handleSecond(pins);
                break;
            case FIRST_ROLL_TENTH_FRAME:
                handleTenthFirst(pins);
                break;
            case SECOND_ROLL_TENTH_FRAME:
                handleTenthSecond(pins);
                break;
            case BONUS_ROLL_TENTH_FRAME:
                handleTenthFillBall(pins);
                break;
            case GAME_OVER:
                handleGameOver();
        }
    }

    private void handleFirst(final int pins) {
        currentFrame = new Frame(pins);
        frames.add(currentFrame);

        updatePreviousFramesOnFirstRoll();

        currentFrame.setPreviousFrameScoreSum(getPreviousFrameScoreAccumulation());

        if (pins == Constants.TOTAL_NUMBER_OF_PINS) {
            if (inSecondLastFrame()) {
                gameState = GameState.FIRST_ROLL_TENTH_FRAME;
            } else {
                gameState = GameState.FIRST_ROLL;
            }
        } else {
            gameState = GameState.SECOND_ROLL;
        }
    }

    private void handleSecond(final int pins) {
        currentFrame.setSecondRollScore(pins);
        currentFrame.setPreviousFrameScoreSum(getPreviousFrameScoreAccumulation());

        if (!inLastFrame() && currentFrame.getIndividualFrameScore() > Constants.TOTAL_NUMBER_OF_PINS) {
            currentFrame.setSecondRollScore(0);
            throw new IllegalStateException(ExceptionConstants.PIN_COUNT_EXCEEDING);
        }

        updatePreviousFrameOnSecondRoll();

        if (inSecondLastFrame()) {
            gameState = GameState.FIRST_ROLL_TENTH_FRAME;
        } else {
            gameState = GameState.FIRST_ROLL;
        }
    }


    private void handleTenthFirst(final int pins) {
        currentFrame = new Frame(pins);

        frames.add(currentFrame);
        updatePreviousFramesOnFirstRoll();

        currentFrame.setPreviousFrameScoreSum(getPreviousFrameScoreAccumulation());

        gameState = GameState.SECOND_ROLL_TENTH_FRAME;
    }

    private void handleTenthSecond(final int pins) {
        Frame tenthFrame = frames.get(9);
        tenthFrame.setSecondRollScore(pins);

        updatePreviousFrameOnSecondRoll();

        if (tenthFrame.getIndividualFrameScore() < Constants.TOTAL_NUMBER_OF_PINS) {
            gameState = GameState.GAME_OVER;
        } else {
            gameState = GameState.BONUS_ROLL_TENTH_FRAME;
        }
    }

    private void handleTenthFillBall(final int pins) {
        if (currentFrame.getFirstRollScore() == Constants.TOTAL_NUMBER_OF_PINS && currentFrame.getSecondRollScore() != Constants.TOTAL_NUMBER_OF_PINS && currentFrame.getSecondRollScore() + pins > Constants.TOTAL_NUMBER_OF_PINS) {
            throw new IllegalStateException(ExceptionConstants.PIN_COUNT_EXCEEDING);
        }
        currentFrame.setBonusRollScoreLastStrike(pins);
        gameState = GameState.GAME_OVER;
    }

    private void handleGameOver() {
        throw new IllegalStateException(ExceptionConstants.GAME_IS_OVER);
    }

    private boolean inSecondLastFrame() {
        return frames.size() == Constants.TOTAL_NUMBER_OF_FRAMES - 1;
    }

    private boolean inLastFrame() {
        return frames.size() == Constants.TOTAL_NUMBER_OF_FRAMES;
    }

    public boolean isGameOver() {
        return gameState == GameState.GAME_OVER;
    }

    public int score() {
        int result = frames.stream().mapToInt(Frame::getIndividualFrameScore).sum();

        return result;
    }

    /**
     * updates the "bonus" scores on the previous 2 frames
     */
    private void updatePreviousFramesOnFirstRoll() {
        previousFrame = frames.indexOf(currentFrame) > 0 ? frames.get(frames.indexOf(currentFrame) - 1) : null;
        previousPreviousFrame = frames.indexOf(currentFrame) > 1 ? frames.get(frames.indexOf(currentFrame) - 2) : null;

        //add the score of the first roll to the previous frame if the previous frame was strike
        //add the score of the first roll to the previous frame if the previous frame was spare
        if (previousFrame != null && (previousFrame.isSpare() || previousFrame.isStrike())) {
            previousFrame.addStrikeSpareBonus(currentFrame.getFirstRollScore());
        }

        //add the score of the first roll to the previous-previous frame if the last two frames BOTH were strike
        if (previousFrame != null && previousPreviousFrame != null && previousFrame.isStrike() && previousPreviousFrame.isStrike()) {
            previousPreviousFrame.addStrikeSpareBonus(currentFrame.getFirstRollScore());
        }
    }

    /**
     * updates the "bonus" scores on the previous frame
     */
    private void updatePreviousFrameOnSecondRoll() {

        //add the score of the second roll to the previous frame if the previous frame was strike
        if (previousFrame != null && previousFrame.isStrike()) {
            previousFrame.addStrikeSpareBonus(currentFrame.getSecondRollScore());
        }
    }

    private int getPreviousFrameScoreAccumulation() {
        int previousFrameScoreSum = previousFrame!=null ? previousFrame.getPreviousFrameScoreSum() + previousFrame.getIndividualFrameScore() : 0;
        return previousFrameScoreSum;
    }

    public List<String[]> calculateScoreboard() {
        List<String[]> scoreBoard = new ArrayList<>();
        int previousFrameScore = 0;
        Integer accumulatedScore = null;
        String individialRollScores = null;

        for (int n = 0; n < frames.size(); n++) {
            Frame frame = frames.get(n);

            previousFrameScore += n > 0 ? frames.get(n - 1).getIndividualFrameScore() : 0;
            accumulatedScore = frame.getIndividualFrameScore() + previousFrameScore;

            String appendFillBall = frame.bonusRollScoreFormatted();
            individialRollScores = frame.firstRollScoreFormatted() + " | " + frame.secondRollScoreFormatted() + (n == Constants.TOTAL_NUMBER_OF_FRAMES-1 ? appendFillBall : "");

            scoreBoard.add(new String[]{individialRollScores, accumulatedScore + ""});
        }
        return scoreBoard;
    }
}