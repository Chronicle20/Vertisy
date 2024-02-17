/**
 *	@Author: iPoopMagic (David)
 *	@NPC: Cody (9200000)
 *	@Description: Thanksgiving Event (Turkey Eggs)
 */
/** @TODO: Rewards: Pet Equip?
 */
var rewards = Array(0, 2020029, 2020029, 2020029, 2020030, 2020030, 2022634, 4031425, 2000004, 2000004, 2000004, 2000004, 2000004, 2000004, 2000005, 2000005, 2000005, 2000005, 2022179);
var scrolls = Array(2043201, 2044201, 2043101, 2044101, 2043001, 2044001, 2044501, 2044601, 2040701, 2043301, 2044301, 2044401, 2044801, 2044901);

function start() {
	var text = "Happy Thanksgiving #b#h0##k! \r\nI'm running out of eggs! Bring me 20 of the same colored Turkey Eggs, and I'll return the favor! #b";
	var hasItems = false;
	for (var i = 4032522; i < 4032525; i++) {
		if (cm.haveItem(i, 20)) {
			hasItems = true;
			break;
		}
	}
	if (hasItems) {
		text += "\r\n#L0#I'd like to give you my turkey eggs.#l";	
	}
	text += "\r\n#L1#Alright, I'll go get you some Turkey Eggs!#l";
	cm.sendSimple(text);
}

function action(mode, type, selection) {
	if (mode != 1 || selection == 1) {
		cm.sendOk("I need them as soon as possible!");
		cm.dispose();
		return;
	}
	var index = Math.floor(Math.random() * rewards.length());
	var reward = rewards[index];
	if (index == 0) {
		index = Math.floor(Math.random() * scrolls.length());
		reward = scrolls[index];
	}
	if (!cm.canHold(reward)) {
		cm.getPlayer().dropMessage(1, "Your inventory is full.");
		cm.dispose();
		return;
	}
	if (cm.getPlayer().getItemQuantity(4032522, false) > 19) {
		cm.gainItem(4032522, -20);
	} else if (cm.getPlayer().getItemQuantity(4032523, false) > 19) {
		cm.gainItem(4032523, -20);
	} else if (cm.getPlayer().getItemQuantity(4032524, false) > 19) {
		cm.gainItem(4032524, -20);
	} else {
		cm.sendOk("I'm sorry, but you do not current have enough turkey eggs to give to me.");
		cm.dispose();
		return;
	}
	cm.gainItem(reward, 1);
	cm.sendOk("Congratulations! You've won a #b#t" + reward + "#!");
	cm.dispose();	
}