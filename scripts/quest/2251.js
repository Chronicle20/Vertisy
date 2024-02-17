var status = -1;

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		if (type == 1 && mode == 0)
			status -= 2;
		else {
			qm.sendOk("I am sorry. I must be working you too hard. I understand.");
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendNext("Okay, now I need you to go slay some Zombie Mushrooms. The sound of the dying mushrooms should be recorded and that should cause them to drop #bRecording Charms#k upon their death.");
	} else if (status == 1) {
		qm.sendOk("Collect 20 #bRecording Charms#k for me. You'll be able to gather them by hunting Zombie Mushrooms.");
		qm.forceStartQuest();
		qm.dispose();
	}
}
function end(mode, type, selection) {
	status++;
	if (mode != 1) {
		if (type == 1 && mode == 0) 
			status -= 2;
		else {
			qm.sendOk("That's not the sound...");
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendNext("Ah, this is the sound! Just hold on a second!");
	} else if (status == 1) {
		if (!qm.haveItem(4032399, 20)) {
			qm.sendOk("That's not the sound...");
			qm.dispose();
			return;
		} else {
			qm.sendOk("#b#e 'Mushking of the Mushroom Castle is in danger. Watch out for King Pepe! Request help from the courageous heroes of Maple World who are at least Level 30.' #n#k\r\n\r\nMushroom Castle? I've never heard of the place...");
			qm.forceCompleteQuest();
			qm.gainItem(4032399, -20);
			qm.gainExp(8000 * 4, true);
			qm.dispose();
		}
	}
}
