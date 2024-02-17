/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

var status;
var questions = 21;
var pointMod = 20;
var points = 0;
var started = false;
var text = "";
var questionList = [[0, 1012111, "bruce"], [0, 1010100, "rina"], [0, 9030000, "fredrick"], [0, 9000021, "gaga"], [2, 2000000, "red potion"], [2, 2050004, "all cure potion"], [1, 100100, "snail"],
                    [1, 3210800, "lupin"], [1, 4230101, "zombie lupin"], [1, 2230105, "seacle"], [2, 4000223, "cucumber"], [2, 4020000, "garnet ore"], [0, 2012012, "lisa"], [0, 2010006, "trina"], [0, 2042000, "spiegelmann"],
                    [0, 1012000, "regular cab"], [0, 9200000, "cody"], [0, 1201000, "lilin"], [0, 1201002, "maha"], [0, 2020006, "jade"], [1, 3100101, "sand dwarf"], [1, 8142000, "phantom watch"], [1, 1210103, "bubbling"],
                    [1, 4230119, "mateon"], [1, 4230116, "barnard gray"], [1, 8140600, "bone fish"], [1, 7130020, "goby"], [1, 7130400, "yellow king goblin"]];
var lastQuestion = -1;
var timeLimit = 10;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if(!started) {
            if (mode >= 1)
                status++;
            else
                status--;
                
            if(status == 0 && mode == 1) {
                cm.sendYesNo("Would you like to try the Speed Quiz?");
                started = true;
            } 
        } else {
            text = cm.getText();
            if(text == "") { // Skip
                doQuiz(true);
            } else if (text == "__GIVEUP__") {
                cm.sendOk("Better luck next time! You scored " + points + " points before you quit.");
                cm.dispose();
            } else {
                doQuiz(false);
            }
        }
    }
}

function doQuiz(skip) {
    if(!skip && lastQuestion != -1)
        correct = checkAnswer(lastQuestion, text);
    else {
        correct = true;
        skip = true;
    }
    if(correct) {
        if(!skip) {
            points = points + 100 - pointMod * 4;
            pointMod--;
        }
        questions--;
        if(questions > 0) {
            lastQuestion = Math.floor(Math.random() * questionList.length);
            var q = questionList[lastQuestion];
            cm.sendSpeedQuiz(0, q[0], q[1], points, questions, timeLimit);
        } else {
            // They finished all of the questions, wow
            points += 200;
            cm.sendOk("Congrats! You completed the speed quiz and scored " + points + " on it!");
            cm.dispose();
        }
    } else {
        cm.sendOk("Oops! That's not the correct answer. You still scored " + points + " points though!");
        cm.dispose();
    }
}

function checkAnswer(questionNum, answer) {
    var q = questionList[questionNum];
    var str = q[2];
    cm.getPlayer().dropMessage(5, str);
    if(str.toLowerCase() == answer.toLowerCase())
        return true;
    else
        return false;
}