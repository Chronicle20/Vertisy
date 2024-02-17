/**
 *	@Name: Pam (Concentrated Formula)
 *	@Author: iPoopMagic (David)
 */
var itemID = new Array (4032196, 4032197, 4032198);
var cost = new Array (2000000, 3000000, 4000000);
var item1 = new Array (4000236, 4000237, 4000238);
var item2 = new Array (4000239, 4000241, 4000242);
var item3 = new Array (4000262, 4000263, 4000265);

function start() {
	var text = "Welcome to my home. I can make some concentrated formula for you, if you'd like. Which would you like? #b";
	for (var i = 0; i < itemID.length; i++) {
		text += "\r\n#L" + i + "#Concentrated Formula: Step " + (i + 1) + " = 30 #i" + item1[i] + "#s + 30 #i" + item2[i] + "#s + 30 #i" + item3[i] + "#s + " + cost[i] + " mesos.#l";
	}
	cm.sendSimple(text);
}

function action(mode, type, selection) {
	var i = selection;
	if (mode < 1) {
		cm.dispose();
		return;
	} else {
		if (cm.canHold(itemID[i]) && cm.haveItem(item1[i], 30) && cm.haveItem(item2[i], 30) && cm.haveItem(item3[i], 30) && cm.getPlayer().getMeso() >= cost[i]) {
			cm.sendNext("Here's Concentrated Formula: Step " + (i + 1) + ". Please put it to good use.");
			cm.gainItem(itemID[i], 1);
		} else {
			cm.sendNext("Wait, hold on. Either you have way too many ingredients, too little, or you're lacking some mesos. Unfortunately, I can't give you Concentrated Formula: Step " + (i + 1) + ".");
		}
		cm.dispose();
	}
}