/**
 *	@Name: Santa
 *	@Description: Trade in items for presents (Cliff's Hut)
 *	@Author: iPoopMagic (David)
 */
var status = 0;
var idneeded = 4032176;
var items = Array(2212000, Toasty Muffler?, 3010045, 2022042, 2020000, 2020002, 2020003, 2022186);
var itemcost = Array(10, 1000, 2000, 300);
var cost = Array(150, 1000, 1000, 500, 320, 220, 550, 2300);

function start() {
	cm.sendNext("Here's a party gift for the Holidays! The Holidays remind all of us to share love and warmth with others. *cough cough* One of these presents... you will only be able to get it here.");
}

function action(mode, type, selection) {
	if (mode != 1) {
		cm.dispose();
		return;
	}
	status++;
	if (status == 1) {
		var text = "What kind of present would you like? #b";
		for (var i = 0; i < items.length; i++) {
			text += "\r\n#L" + i + "# #v" + items[i] + "# " + (i < 4 ? "#t" + idneeded + "# " + itemcost[i] + ", ": "") + cost[i] + " mesos";
		}
		cm.sendSimple(text);
	} else if (status == 2) {
		if (!cm.canHold(items[selection])) {
			cm.getPlayer().dropMessage(1, "Your inventory is full.");
			cm.dispose();
			return;
		}
		if (cm.getPlayer().getMeso() < cost[selection]) {
			cm.sendOk("You do not have enough mesos to receive this present.");
			cm.dispose();
			return;			
		}
		if (selection < 4) {
			if (!cm.haveItem(idneeded, itemcost[selection])) {
				cm.sendOk("You do not have enough Rascal Snowpieces to receive this present.");
				cm.dispose();
				return;
			}
			cm.gainItem(itemcost[selection], -1);
		}
		cm.gainMeso(-cost[selection]);
		cm.gainItem(items[selection], 1);
		cm.sendOk("Happy Holidays!");
		cm.dispose();
	}
}