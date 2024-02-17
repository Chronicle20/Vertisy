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
		qm.sendSimple("What is your business this time?\r\n#b#L0#Do you know anything about a ghost tree?#l#k\r\n");
	} else if (selection == 0) {
		qm.sendSimple("A ghost tree... I guess you're talking about #b#o3220000##k.\r\n#b#L1#What is this #o3220000#?#l#k\r\n");  
	} else if (selection == 1) {
		qm.sendSimple("It's a very old tree survivng all the years ever since #m102000000# was covered with lush forests. As it weathered all tha pains and tough times, its anger grew large. It was infuriated at the people destroying forests, and at the scenes of forests falling barren.\r\n#b#L2#What happened next?#l#k\r\n");
	} else if (selection == 2) {
		qm.sendOk("In the end, the rage turned the tree into a monster. Now it's nothing but a horrendous monster gobbling nutrients from the soil. Don't go too far into there. I'd praise and encourage your curiosity, bu");
		qm.forceCompleteQuest();
		qm.gainExp(40, true);
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.dispose();
}
