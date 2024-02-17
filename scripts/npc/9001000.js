/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

var status;
var sel = 0;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
		if (status == 0) {
			var text = "Hey #b#h ##k, I'm the #eSkill Manager#n.\r\n";
				text += "I help you achieve higher skill levels in the skills listed below.\r\n"
				text += "Simply click on the one in which you'd like to train.\r\n"
				text += "#L0##bPrayer#k#l\r\n"; //Puma Bone + Gruesom Bone 4000514 + 4032473
				text += "#L1##bSlayer#k#l\r\n";
				text += "#L2##bSummoning#k#l\r\n";
				text += "#L3##bHealth#k#l\r\n";
				text += "#L4##bMana#k#l\r\n";
				text += "#L5##bAgility#k#l\r\n";
				text += "#L6##bFishing#k#l\r\n";
					cm.sendSimple(text);
		} else if (status == 1) {
				if (selection == 0) {
						cm.sendSimple("You have selected Prayer.\r\nWhich bones would you like to bury?\r\n#L7##v4000514##l\r\n#L8##v4032473##l");
				} else if (selection == 1) {
						cm.sendOk("This is currently unavailable.");
						cm.dispose();
				} else if (selection == 2) {
						cm.sendOk("You have selected Summoning.");
				} else if (selection == 3) {
						cm.sendOk("This is currently unavailable.");
						cm.dispose();
				} else if (selection == 4) {
						cm.sendOk("This is currently unavailable.");
						cm.dispose();
				} else if (selection == 5) {
						cm.sendOk("This is currently unavailable.");
						cm.dispose();
				} else if (selection == 6) {
						cm.sendOk("This is currently unavailable.");
						cm.dispose();
				}
		} else if (status == 2) {
			sel = selection;
				if (selection == 7) {
						cm.sendGetNumber("How many would you like to bury?\r\n#v4000514# x#e#c4000514##n", 1, 1, 1000);
				} else if (selection == 8) {
						cm.sendGetNumber("How many would you like to bury?\r\n#v4032473# x#e#c4032473##n", 1, 1, 1000);
				}
		} else if (status == 3) {
				if (sel == 7) {
					if (cm.haveItem(4000514, selection)) {
						cm.gainItem(4000514, -selection);
						//cm.Prayerexphere();
						cm.sendOk("You have buried #e" + selection + "#n #v4000514#.");
						cm.dispose();
					} else if (cm.haveItem(4000514) < 1) {
						cm.sendOk("You don't have that many bones.\r\nYou selected #e" + selection + "#n but you have none.");
						cm.dispose();
					} else {
						cm.sendOk("You don't have that many bones.\r\nYou selected #e" + selection + "#n however you only have #c4000514#.");
						cm.dispose();
					}
				} else if (sel == 8) {
					if (cm.haveItem(4032473, selection)) {
						cm.gainItem(4032473, -selection);
						//cm.Prayerexphere();
						cm.sendOk("You have buried #e" + selection + "#n #v4032473#.");
						cm.dispose();
					} else if (cm.haveItem(4032473) < 1) {
						cm.sendOk("You don't have that many bones.\r\nYou selected #e" + selection + "#n but you have none.");
						cm.dispose();
					} else {
						cm.sendOk("You don't have that many bones.\r\nYou selected #e" + selection + "#n however you only have #c4032473#.");
						cm.dispose();
					}
				}
		}
}
		