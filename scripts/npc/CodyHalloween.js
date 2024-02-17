/**
 *	@Author: iPoopMagic (David)
 *	@NPC: Cody (9200000)
 *	@Description: Halloween Event (Halloween Basket)
 */

// AP Reset, Halloween Broomstick Chair, Witch's Belt Scroll
var rewards = [5050000, 3010043, 2049114];
// White Scroll, Chaos Scroll, SP Reset, Pink Scroll, Onyx Apples
var luckyRewards = [2340000, 2049100, 5050003, 1, 1, 1, 1, 1, 2022179];
var quantity = [1, 2, 1, 1, 1, 1, 1, 1, 5];

var pinkScrolls = [2044817, 2044910, 2044713, 2044613, 2044513, 2044420, 2044320, 2044220, 2044120,
					2044028, 2043813, 2043713, 2043313, 2043220, 2043120, 2043022, 2041068, 2041069,
					2040943, 2040833, 2040834, 2040755, 2040756, 2040757, 2040629, 2040542, 2040543,
					2040429, 2040333, 2040045, 2040046];
 
function start() {
	var text = "Happy Halloween #b#h0##k! \r\nRemember the Halloween basket from 2006 #v1302062#? That's right, and if you bring me back 100 Halloween candies #v4031203#, I'll be sure to retrieve one for you! #b";
	if (cm.haveItem(4031203, 100)) {
		text += "\r\n#L0#I'd like to give you my #bHalloween candies#k #v4031203#.#l";
	}
	text += "\r\n#L1#Alright, I'll go get you some #bHalloween candies#k #v4031203#!#l";
	text += "\r\n#L2#I would like to obtain my rewards for the #rSpooky Roll Call#k event.#l";
	cm.sendSimple(text);
}

function action(mode, type, selection) {
	var player = cm.getPlayer();
	if (mode != 1 || selection == 1) {
		cm.sendOk("I need them as soon as possible!");
		cm.dispose();
		return;
	}
	if (selection == 2) {
		var rewardText = "";
		var amount = player.getRollCallAmount(player.getClient().getHWID());
		if (amount >= 15 && amount < 20) {
			// This prevents them from getting another reward in another time.
			player.setRollCall(player.getClient().getHWID(), amount = 20, java.time.LocalDateTime.now().getDayOfMonth());
			// Begin rewards
			player.gainNX(15000);
			for (var i = 0; i < rewards.length; i++) {
				cm.gainItem(rewards[i], 1);
				rewardText += ", a #t" + rewards[i] + "#";
			}
			// Begin lucky rewards
			var chance = Math.random() * 100;
			if (chance < 30) {
				var index = Math.random() * luckyRewards.length;
				if (luckyRewards[index] == 1) {
					var PSindex1 = Math.random() * pinkScrolls.length;
					var PSindex2 = Math.random() * 16; // ATT scrolls
					cm.gainItem(pinkScrolls[PSindex1], 1);
					rewardText += ", and #t" + pinkScrolls[PSindex1] + "#!";
					if (chance < 15) {
						cm.gainItem(pinkScrolls[PSindex2], 1);
						rewardText += "Oh, and a #t" + pinkScrolls[PSindex2] + "#!";
					}
				} else {
					cm.gainItem(luckyRewards[index], quantity[index]);
					rewardText += ", and " + quantity[index] + " #t" + rewards[i] + "#!";
				}
			}
			cm.sendOk("You have earned #r15,000 NX" + rewardText);
		}
		cm.dispose();
		return;
	}
	if (!cm.canHold(1302062)) {
		cm.getPlayer().dropMessage(1, "Your inventory is full.");
		cm.dispose();
		return;
	}
	if (cm.haveItem(1302062, 1) {
		cm.sendOk("Looks like you already received one! Thanks for the candy earlier though!");
	} else {
		cm.gainItem(1302062, 1);
	}
	cm.dispose();	
}