/**
 *	@Author: iPoopMagic (David)
 *	@Description: End of Pirate PQ
 */
var status = -1;

function start() {
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
	if (cm.getPlayer().getMapId() == 925100600) {
		if (status == 0) {
			cm.removeAll(4001117);
			cm.removeAll(4001120);
			cm.removeAll(4001121);
			cm.removeAll(4001122); //anything else?
			cm.sendSimple("Thank you for saving me from #rLord Pirate#k. How may I help you? \r\n#b#L0#Get me out of here.#l\r\n#L1#I want a Pirate Hat.#l#k");
		} else if (status == 1) {
			if (selection == 0) {
				if (!cm.canHold(4032101)/* || !cm.canHold(2000000)*/) { // Lost Treasure; For some reason 4001455 - Hat Fragments don't exist in v.83
					cm.getPlayer().dropMessage(1, "Your USE and/or ETC. inventory is full.");
					return;
				}
				//giveRandomReward();
				cm.gainItem(4032101, 1);
				cm.warp(925100700, 0);
			} else {
				if (cm.haveItem(1002573, 1)) {
					if (cm.haveItem(4032101, 350)) {
						if (cm.canHold(1002574, 1)) {
							cm.gainItem(1002573, -1);
							cm.gainItem(4032101, -350);
							cm.gainItem(1002574, 1);
							cm.sendOk("I have given you the hat.");
						} else {
							cm.sendOk("Please make room in your inventory.");
						}
					} else {
						cm.sendOk("You need 350 Lost Treasures & the (Lv. 80) hat to obtain this (Lv. 90) hat.");
					}
				} else if (cm.haveItem(1002572, 1)) {
					if (cm.haveItem(4032101, 200)) {	
						if (cm.canHold(1002573, 1)) {
							cm.gainItem(1002572, -1);
							cm.gainItem(1002573, 1);
							cm.sendOk("I have given you the hat.");
						} else {
							cm.sendOk("Please make room in your inventory.");
						} 
					} else {
						cm.sendOk("You need 200 Lost Treasures & the (Lv. 70) hat to obtain this (Lv. 80) hat.");
					}
				} else if (cm.haveItem(1002571, 1)) {
					if (cm.haveItem(4032101, 80)) {	
						if (cm.canHold(1002572, 1)) {
							cm.gainItem(1002571, -1);
							cm.gainItem(1002572, 1);
							cm.sendOk("I have given you the hat.");
						} else {
							cm.sendOk("Please make room in your inventory.");
						} 
					} else {
						cm.sendOk("You need 80 Lost Treasures & the (Lv. 60) hat to obtain this (Lv. 70) hat.");
					}
				} else {
					if (cm.haveItem(4032101, 30)) {	
						if (cm.canHold(1002571, 1)) {
							cm.gainItem(1002571, 1);
							cm.sendOk("I have given you the hat.");
						} else {
							cm.sendOk("Please make room in your inventory.");
						}
					} else {
						cm.sendOk("You need 30 Lost Treasures to obtain this (Lv. 60) hat.");
					}
				}
				cm.dispose();
			}
			cm.dispose();
		}
	} else if (cm.getPlayer().getMapId() == 925100500) {
		if (status == 0) {
			cm.sendNext("Thank you so much for saving me. Now let's get you out of here!");
		} else if (status == 1) {
			cm.getPlayer().getEventInstance().finishPQ();
			cm.givePartyQuestExp("PiratePQ");
			cm.warp(925100600);
			cm.dispose();
		}
	}
}

function giveRandomReward() {
	var index = Math.floor(Math.random() * rewards.length);
	var reward;
	var quantity;
	if (index % 2 == 0){ //The index was an item id
		reward = rewards[index];
		quantity = rewards[index + 1];
	} else {
		reward = rewards[index - 1];
		quantity = rewards[index];
	}
	if(!cm.canHold(reward)){
		cm.getPlayer().dropMessage(1, "Your inventory is full.");
		return;
	}
	cm.gainItem(reward, quantity);
}