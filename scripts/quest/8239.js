/*
 * @quest: Bounty Master (8239)
 * @npc: Lita Lawless
 */
var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendOk("Oh ok, let me know when you're good and ready to receive this honor!");
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendYesNo("Well, well, well! You've certainly proved yourself as an ardent hunter! In return for your efforts, I'm going to deputize you as an official protector of New Leaf City!");
	} else if (status == 1) {
		qm.sendOk("Ok, ok. Let's not go overboard here-this isn't some award you can hang on a wall-it's something that designates you as a guardian of the city, and whenever I summon you, you must answer the call.");
		qm.forceStartQuest();
		qm.dispose();
	}
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
		qm.sendOk("By the power vested in me as Sheriff of New Leaf City, I hereby deputize you as a Protector of New Leaf City!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i1122014# #t1122014#\r\n#fUI/UIWindow.img/QuestIcon/8/0# 46739 exp");
		qm.gainItem(1122014, 1);
		qm.gainExp(46739 * 5);
		qm.forceCompleteQuest();
		qm.dispose();
	}
}