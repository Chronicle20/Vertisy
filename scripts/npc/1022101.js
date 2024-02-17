
var mlg = 4000313;

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
			/*if (cm.haveItem(mlg)) {
			var text = "Hey #b#h ##k, I see you've been working on your skills!\r\n";
			text += "Please select what you'd like to purchase below.\r\n";
			text += "You currently have #b#c" + mlg + "##k #v" + mlg + "#\r\n";
			text += "#L0##v1003057##b 1 Hour of Elite Account Service  - 50 #v" + mlg +"##k#l\r\n";
			text += "#L1##v2022179##b 1 Onyx Apple - 35 #v" + mlg +"##k#l\r\n";
			text += "#L2##v2022283##b 1 Subani's Mystic Cauldron - 40 #v" + mlg +"##k#l\r\n";
			text += "#L3##v1112414##b Lilin's Ring - 75 #v" + mlg +"##k#l\r\n";
			text += "#L4##v1012070##b Strawberry Icecream bar - 150 #v" + mlg +"##k#l\r\n";
			text += "#L5##v1032048##b Crystal Leaf Earrings - 200 #v" + mlg +"##k#l\r\n";
			text += "#L6##v1122057##b Mind of Maple Necklace - 400 #v" + mlg +"##k#l\r\n";
			cm.sendSimple(text);
			} else {
				cm.sendOk("You seem to not have any #v" + mlg +"#.\r\nYou can obtain them by doing various skills.\r\nThe current ways to earn them are.\r\nFishing.");
				cm.dispose();
			}*/
			cm.dispose();
	} else if (status == 1) {
		if (selection == 0) {
			if (cm.haveItem(mlg, 50)) {
				cm.gainItem(mlg, -50);
				cm.getClient().addEliteHours(1);
				cm.sendOk("You have purchased #b1#k hour of Elite Account Service. You now have " + cm.getClient().getEliteTimeLeft() + " left of Elite.\r\nHave fun.");
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry #b#h ##k, but you don't have enough #v" + mlg +"#.\r\nWhat you have tried to purchase costs 50, however you only have #c" + mlg +"#.");
				cm.dispose();
			}
		} else if (selection == 1) {
			if (cm.haveItem(mlg, 35)) {
					cm.gainItem(mlg, -35);
					cm.gainItem(2022179, 1);
					cm.sendOk("You have purchased #b1#k Onyx Apple.");
					cm.dispose();
			} else {
				cm.sendOk("I'm sorry #b#h ##k, but you don't have enough #v" + mlg +"#.\r\nWhat you have tried to purchase costs 35, however you only have #c" + mlg +"#.");
				cm.dispose();
			}
		} else if (selection == 2) {
				if (cm.haveItem(mlg, 40)) {
					cm.gainItem(mlg, -40);
					cm.gainItem(2022283, 1);
					cm.sendOk("You have purchased #b1#k Subani's Mystic Cauldron.");
					cm.dispose();
				} else {
					cm.sendOk("I'm sorry #b#h ##k, but you're don't have enough #v" + mlg +"#.\r\nWhat you have tried to purchase costs 40, However you only have #c" + mlg +"#.");
					cm.dispose();
				}
		} else if (selection == 3) {
			if (cm.haveItem(mlg, 75)) {
					cm.gainItem(mlg, -75);
					cm.gainItem(1112414, 1);
					cm.sendOk("You have purchased #b1#k Lilin's Ring.");
					cm.dispose();
			} else {
				cm.sendOk("I'm sorry #b#h ##k, but you don't have enough #v" + mlg +"#.\r\nWhat you have tried to purchase is 75, however you only have #c" + mlg +"#.");
				cm.dispose();
			}
		} else if (selection == 4) {
			if (cm.haveItem(mlg, 150)) {
				cm.gainItem(mlg, -150);
				cm.gainItem(1012070, 1);
				cm.sendOk("You have purchased #b1#k Strawberry Icecream Bar.");
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry #b#h ##k, but you don't have enough #v" + mlg +"#.\r\nWhat you have tried to purchase costs 150, however you only have #c" + mlg +"#.");
				cm.dispose();
			}
		} else if (selection == 5) {
			if (cm.haveItem(mlg, 200)) {
				cm.gainItem(mlg, -200);
				cm.gainItem(1032048, 1);
				cm.sendOk("You have purchased #b1#k Crystal Leaf Earring.");
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry #b#h ##k, but you don't have enough #v" + mlg +"#.\r\nWhat you have tried to purchase costs 200, however you only have #c" + mlg +"#.");
				cm.dispose();
			}
		} else if (selection == 6) {
			if (cm.haveItem(mlg, 400)) {
				cm.gainItem(mlg, -400);
				cm.gainItem(1122057, 1);
				cm.sendOk("You have purchased #b1#k Maple Mind Necklace.");
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry #b#h ##k, but you do'nt have enough #v" + mlg +"#.\r\nWhat you have tried to purchase costs 400, however you only have #c" + mlg +"#.");
				cm.dispose();
			}
		}
	}
}