var status = -1;

function end(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        qm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		qm.sendSay("Finally... I have my father's Diary. Thank you. I am starting to trust you even more. Your current position doesn't seem to suit your great abilities. I think you have the qualifications to advance to a #bBlade Acolyte#k. I will advance you to a Blade Acolyte now.", false, true);
	}else if(status == 1){
		if(qm.hasItem(4032617) && !qm.isQuestCompleted()){
			if(qm.canHold(1052244)){
				qm.gainItem(4032616, -1);
				qm.gainItem(1052244, 1);
				qm.forceCompleteQuest();
				qm.changeJobById(431);
				qm.gainSP(2);
				qm.teachSkill(4311003, 0, 20);
				qm.sendSay("My father's diary... Father would often write in a code that only he and I could understand. Wait, in the last chapter... This!", false, true);
			}else qm.sendSay("Please make room in your Equip Inventory.");
		}else if(qm.isQuestCompleted()){
			qm.sendSay("My father's diary... Father would often write in a code that only he and I could understand. Wait, in the last chapter... This!", false, true);
		}
	}else if(status == 2){
		qm.sendSay("This can't be! It's a lie! Jin! How dare you lay a finger on my father's diary!\r\n\r\n#b(Lady Syl drops the diary and it falls to the ground.)#k", false, true);
	}else if(status == 3){
		qm.sendSay("#b(You pick up the book and start reading it.)\r\n\r\n- Date: XX-XX-XXXX -\r\nTeacher has passed away... Three days ago, teacher left for the Cursed Sanctuary at the request of Tristan. Syl seemed worried so I decided to go look for him. When I arrived at the entrance of the Sanctuary, I heard a shriek that made me shiver...", false, true);
	}else if(status == 4){
		qm.sendSay("#bWhen I jumped into the darkness of the sanctuary, I came face to face with a red-eyed monster spewing evil energy. Teacher was nowhere to be seen. The monster started attacking. After a fierece battle, I finally succeeded in killing it. However, the fallen monster soon turned into... teacher.", false, true);
	}else if(status == 5){
		qm.sendSay("#bI attempted to help teacher, but he passed in my arms. Before he passed, he whispered, My soul was trapped within the Balrog. You freed me... Now, take care of Kerning City and Syl.... and... please don't tell a soul about this. I can't forgive myself for allowing the demon to steal my soul.", false, true);
	}else if(status == 6){
		qm.sendSay("#bAs he wished, I will never reveal what happened. His secrets--along with his diary-\r\n-will forever be sealed.- Jin -", false, false);
	}
}