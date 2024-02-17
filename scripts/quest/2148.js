var status = -1; 

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		if (type == 1 && mode == 0) {
			status -= -2;
		} else {
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendSimple("What's the matter?\r\n#b#L0#Have you heard about the Ghost Tree?#l#k\r\n");
	} else if (selection == 0) {
		qm.sendSimple("A ghost tree? Ah, you must be talking about the giant stump, right? I heard from my father that his father used to see such trees when he was young. The story says that each branch of the tree bears a red piece of cloth, which was dyed in the blood of ghosts. I have never seen it in person, so I don't know whether or not it's true.\r\n#b#L1#You didn't catch anything else?#l#k\r\n");  
	} else if (selection == 1) {
		qm.sendOk("I'm sorry, but I'm not really good at following rumors.");
		qm.forceCompleteQuest();
		qm.gainExp(40, true);
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.dispose();
}
