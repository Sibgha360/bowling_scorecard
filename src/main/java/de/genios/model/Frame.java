package de.genios.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Frame {
    private int firstRollScore;
    private int secondRollScore;
    private int strikeSpareBonusScore;

    private int bonusRollScoreLastStrike;
    private boolean isStrike;
    private boolean isSpare;

    private int previousFrameScoreSum;

    public Frame(final int first) {
        this.firstRollScore = first;
        this.secondRollScore = 0;

        if(first == 10)
        {
            isStrike=true;
        }
    }

    public int getIndividualFrameScore() {
        return firstRollScore + secondRollScore + bonusRollScoreLastStrike + strikeSpareBonusScore;
    }
    public int getAccumulatedFrameScore() {
        return getIndividualFrameScore()+getPreviousFrameScoreSum();
    }

    public boolean isStrike() {
//        isStrike = firstRollScore == 10;
        return isStrike;
    }

    public boolean isSpare() {
//        isSpare = !isStrike() && getFrameScore() == 10;
        return isSpare;
    }

    public void setSecondRollScore(int second) {
        this.secondRollScore = second;
        if(!isStrike() && (second+firstRollScore) == 10)
        {
            isSpare=true;
        }
    }

    public void addStrikeSpareBonus(int points) {
        this.strikeSpareBonusScore += points;
    }

    public String secondRollScoreFormatted() {
        String secondString;

        if (secondRollScore == 10) {
            secondString = "X";
        } else if (firstRollScore < 10 && firstRollScore + secondRollScore == 10) {
            secondString = "/";
        } else {
            secondString = String.valueOf(secondRollScore);
        }
        return secondString;
    }

    public String firstRollScoreFormatted() {
        String firstString;

        if (firstRollScore == 10) {
            firstString = "X";
        } else {
            firstString = String.valueOf(firstRollScore);
        }
        return firstString;
    }

    public String bonusRollScoreFormatted() {
        String fillString = " | ";

        if (bonusRollScoreLastStrike == 10) {
            fillString += "X";
        } else {
            fillString += String.valueOf(bonusRollScoreLastStrike);
        }

        if (!(firstRollScore == 10 || firstRollScore + secondRollScore == 10)) {
            fillString = "";
        }

        return fillString;
    }

    @Override
    public String toString() {
        return "(" + firstRollScore + "," + secondRollScore + (bonusRollScoreLastStrike == 0 ? "" : (", " + bonusRollScoreLastStrike)) + ')';
    }
}