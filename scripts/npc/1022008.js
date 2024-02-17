/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

var status;

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
			var text = "Hey #b#H ##k, I'm the #eBurnt Sword of Vertisy#n.\r\n";
				text += "I help you achieve higher skill levels in the skills listed below.\r\n"
				text += "Simply click on the one in which you'd like to train.\r\n"
				text += "#L0##bPrayer#k#l";
				text += "#L1##bSlayer#k#l";
				text += "#L2##bSummoning#k#l";
				text += "#L3##bHealth#k#l";
				text += "#L4##bMana#k#l";
				text += "#L5##bAgility#k#l";
				text += "#L6##bFishing#k#l";
					cm.sendSimple(text);
		} else if (status == 1) {
				if (selection == 0) {
						cm.sendOk("You have selected Prayer.");
				} else if (selection == 1) {
						cm.sendOk("You have selected Slayer.");
				} else if (selection == 2) {
						cm.sendOk("You have selected Summoning.");
				} else if (selection == 3) {
						cm.sendOk("You have selected Health.");
				} else if (selection == 4) {
						cm.sendOk("You have selected Mana.");
				} else if (selection == 5) {
						cm.sendOk("You have selected Agility.");
				} else if (selection == 6) {
						cm.sendOk("You have selected Fishing.");
				}
		}
}