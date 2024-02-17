/**
 *	@Author: iPoopMagic (David)
 *	@NPC: Grandma Benson (9201029)
 *	@Description: Thanksgiving Event (Making Pumpkin Pies)
 */
var pie = 2022113;

function start() {
	var text = "Happy Thanksgiving #b#h0##k! \r\nNow that my pies in higher demand for this season, I need help getting all of the ingredients! #b";
	var hasItems = false;
	for (var i = 4031418; i < 4031422; i++) {
		if (cm.haveItem(i, 1)) {
			hasItems = true;
		} else {
			hasItems = false;
			break;
		}
	}
	if (hasItems) {
		text += "\r\n#L0#I'd like to give you the ingredients I scrounged up.#l";
	}
	text += "\r\n#L1#Alright, I will get you the ingredients!#l";
	cm.sendSimple(text);
}

function action(mode, type, selection) {
	if (mode != 1 || selection == 1) {
		cm.sendOk("I'll need #b1 Pie Crust#k, #b1 Pumpkin#k, #b1 Flour#k, #b1 Powder Sugar#k.");
		cm.dispose();
		return;
	}
	if (!cm.canHold(pie)) {
		cm.getPlayer().dropMessage(1, "Your inventory is full.");
		cm.dispose();
		return;
	}
	cm.gainItem(pie, 1);
	cm.sendOk("Thank you, grandchild! Here's a token of my appreciation for your help!");
	cm.dispose();	
}