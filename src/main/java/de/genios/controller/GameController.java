package de.genios.controller;

import de.genios.config.PropertyConfig;
import de.genios.helper.Util;
import de.genios.model.BowlingGame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
public class GameController {

    @Autowired
    PropertyConfig propertiesConfig;

    @Autowired
    BowlingGame bowlingGame;

    @GetMapping(value = "/${start-controller.path}")
    public String start(Model model) {
        bowlingGame.resetGame();
        log.info("Game started");

        return propertiesConfig.getIndexPage();
    }

    @GetMapping(value = ("/${bowl-controller.path}"))
    public String bowl(Model model, @RequestParam("pins") String pins) {
        if (!Util.validateInput(pins)) {
            log.info("Input not valid. pins: " + pins);

            model.addAttribute("message", propertiesConfig.getWrongInput());
            return propertiesConfig.getIndexPage();
        }

        //ROLL
        bowlingGame.roll(NumberUtils.parseNumber(pins, Integer.class));

        //Game over check
        if (bowlingGame.isGameOver()) {
            log.info("Game ended!");
            model.addAttribute("message", propertiesConfig.getGameOver());
            model.addAttribute("finalScore", propertiesConfig.getFinalScore() + bowlingGame.score());
        }

        //update the scoreboard
        refreshScoreBoard(model);

        return propertiesConfig.getIndexPage();
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public String error(Model model) {

        model.addAttribute("message", propertiesConfig.getPinExceeding());
        refreshScoreBoard(model);

        return propertiesConfig.getIndexPage();
    }

    public void refreshScoreBoard(Model model) {
        List<String[]> scoreBoard = bowlingGame.calculateScoreboard();

        for (int n = 0; n < bowlingGame.getFrames().size(); n++) {
            model.addAttribute("frame" + (n + 1), scoreBoard.get(n)[0]);
            model.addAttribute("score" + (n + 1), scoreBoard.get(n)[1]);
        }
    }
}