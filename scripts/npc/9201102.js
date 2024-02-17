/**
 *	@Name: Stirgeman
 *	@Description: Get his items! (GMS-like)
 *	@Author: iPoopMagic (David)
 */

var status = 0;
var rewards0 = new Array (1060128, 1060129, 1060130, 1060131); // Stirgeman Utility Pants Mk
var rewards1 = new Array (1061150, 1061151, 1061152, 1061153); // Stirgeman Utility Skirts Mk
var rewards2 = new Array (1102177, 1102178, 1102179, 1102180, 1102181, 1102182, 1102183); // Stirgeman Capes

function start() {
	// 12 Stretchy Material, 5 Duct Tape, 8 Coat Hanger = Cape
	if (cm.haveItem(4032030, 12) && cm.haveItem(4032029, 5) && cm.haveItem(4032027, 8)) {
		cm.sendYesNo("Hm, seems like you have the correct items to get one of my capes. Would you like to trade in your items now?");
	// 8 Stretchy Material, 8 Wad of Gum, 15 Loaded Spring, 20 Screw (?) = Bottom
	} else if (cm.haveItem(4032030, 8) && cm.haveItem(4032028, 8) && cm.haveItem(4000399, 15) && cm.haveItem(4003000, 20)) {
		status = 1;
		cm.sendYesNo("Hm, seems like you have the correct items to get one of my " + (cm.getPlayer().getGender() == 0 ? "pants" : "skirts") + ". Would you like to trade in your items now?");
	} else {
		cm.sendOk("In order to obtain one of my equipment items, you'll need some tradeable goods.");
		cm.dispose();
	}
}

function action(mode, type, selection) {
	if (mode < 0) {
		cm.sendOk("Have a Stirge-tastic day!!");
		cm.dispose();
		return;
	}
	if (status == 0) { // Cape
		giveRandomReward(2);
	} else if (status == 1) { // Bottom
		giveRandomReward(cm.getPlayer().getGender());
	}
	cm.dispose();
}

function giveRandomReward(type) {
	var index;
	if (type == 0) {
		index = Math.floor(Math.random() * rewards0.length);
	} else if (type == 1) {
		index = Math.floor(Math.random() * rewards1.length);
	} else if (type == 2) {
		index = Math.floor(Math.random() * rewards2.length);
	} else {
		cm.dispose();
		return;
	}
	
	var reward;
	var rewards;
	if (type == 0) {
		rewards = rewards0;
	} else if (type == 1) {
		rewards = rewards1;
	} else if (type == 2) {
		rewards = rewards2;
	} else {
		cm.dispose();
		return;
	}
	
	if (index % 2 == 0) {
		reward = rewards[index];
	} else {
		reward = rewards[index - 1];
	}
	if (!cm.canHold(reward)){
		cm.getPlayer().dropMessage(1, "Your inventory is full.");
		return;
	}
	cm.gainItem(reward, 1);
}