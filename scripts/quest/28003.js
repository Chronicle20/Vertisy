/**
 *	@Name: Alcaster's Reply (Holiday PQ Pre-Quest)
 *	@Author: iPoopMagic (David)
 */
var status = -1;

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		qm.sendOk("Are you plotting against us with Scrooge?");
		qm.dispose();
		return;
	}
	if (status == 0) {
		qm.sendAcceptDecline("Oh, this is not good. Scrooge is back?! We cannot let him steal the hopes and dreams of the good residents of Happyville... That just cannot happen... now hold on for a minute... here's the letter I want you to give to Snow Spirit. Please help us one more time by seeing her immediately and hand her this letter. Will you?");
	} else if (status == 1) {
		qm.forceStartQuest();
		qm.gainItem(4032091, 1);
		qm.sendOk("Thank you. I'll be counting on you. The fate of Happyville and Maplemas lies in your hands. Good luck.");
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.sendOk("I have been waiting for you, #b#h0##k! How was Alcaster doing? Oh... okay... yes... I knew he would write back! Let me see what he wrote. \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v4032092# 1 #t4032092#");
	qm.gainItem(4032091, -1);
	qm.gainItem(4032092, 1);
	qm.forceCompleteQuest();
	qm.dispose();
}

// Thank you so much for your help. We will be able to protect Happyville with this. Let me see what it says. (After finishing the letter, she starts thinking for a bit...) #b#h0##k... I want you to listen carefully to what I am about to say. First, take this Force of the Spirit. This is the key to defeating Scrooge.