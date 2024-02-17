/**
 * @Author: iPoopMagic (David)
 * @Name: Gathering Up the Lacking Ingredients (Mr. Ku)
 */
var status = -1;

function start(mode, type, selection) {
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
            status -= 2;
        else{
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        if (!qm.haveItem(4000294)) {
            qm.dispose();
			return;
		} else if (!qm.canHold(2000000)) {
			qm.sendOk("I'm sorry, but you're USE inventory is full. Please clear it before speaking to me.");
			qm.dispose();
			return;
		}
		if (qm.haveItem(4000294, 1000)) {
            qm.sendOk("Wow, 1000! You are truly dedicated. Thank you deeply, and your rewards are great. " +
						"\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2040501# 1 #t2040501#\r\n#v2000005# 50 #t2000005#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 54000 exp");
            qm.gainExp(54000);
            qm.gainItem(2040501, 1);
            qm.gainItem(2000005, 50);
        } else if (qm.haveItem(4000294, 600) && !qm.haveItem(4000294, 1000)) {
            qm.sendOk("Wow, I see you have quite a number of bellflower roots for me, thank you. The more you get, the better the rewards!! " +
						"\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2020013# 50 #t2020013#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 54000 exp");
            qm.gainExp(54000);
            qm.gainItem(2020013, 50);
        } else if (qm.haveItem(4000294, 500) && !qm.haveItem(4000294, 600)) {
            qm.sendOk("Wow, I see you have quite a number of bellflower roots for me, thank you. The more you get, the better the rewards!! " +
						"\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 54000 exp");
            qm.gainExp(54000);
        } else if (qm.haveItem(4000294, 100) && !qm.haveItem(4000294, 500)) {
            qm.sendOk("Ah, I see you have some bellflower roots for me, thank you. The more you get, the better the rewards!! " + 
						"\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 45000 exp");
            qm.gainExp(45000);
        } else if (qm.haveItem(4000294, 50) && !qm.haveItem(4000294, 100)) {
            qm.sendOk("Ah, I see you have some bellflower roots for me, thank you. The more you get, the better the rewards!! " + 
						"\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2020007# 50 #t2020007#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 10000 exp");
            qm.gainExp(10000);
            qm.gainItem(2020007, 50);
		} else if (qm.haveItem(4000294, 1) && !qm.haveItem(4000294, 50)) {
            qm.sendOk("Ah, I see you have a bellflower root for me, thank you. The more you get, the better the rewards!! " +
						"\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2000000# 1 #t2000000#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 10 exp");
            qm.gainExp(10);
            qm.gainItem(2000000, 1);
		}
		qm.removeAll(4000294);
		qm.forceCompleteQuest();
        qm.dispose();
    }
}