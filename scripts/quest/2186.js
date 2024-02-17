/*
    Author: BubblesDev 0.75
    Quest: Abel Glasses Quest
*/
var status = -1;

function start(mode, type, selection) {
	qm.dispose();
}
function end(mode, type, selection) {
	if (qm.haveItem(4031854) || qm.haveItem(4031855)){ //When I figure out how to make a completance with just a pickup xD
		if (qm.haveItem(4031854))
			qm.gainItem(4031854, -1);
		else
			qm.gainItem(4031855, -1);
		qm.sendNext("Sorry, those aren't my glasses.");
		qm.dispose();
		return;
	}
	status++;
	if (status == 0) {
		qm.sendNext("What? You found my glasses? I better put it on first, to make sure that it''s really mine. Oh, it really is mine. Thank you so much!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2030019# 5 #t2030019#s\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#  1000 EXP");
	} else {
		qm.gainItem(2030019,5);
		qm.gainExp(1000);
		qm.forceCompleteQuest();
		qm.dispose();
	}
}