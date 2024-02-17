/**
 *	@Author: iPoopMagic (David)
 *	@NPC: Joko
 *	@Function: Crimsonwood Exchange Quest
 */

var status;
var badges = new Array(4032007, 4032006, 4032009, 4032008);
// Windraider Badges (4032007)
var windraiderRewards = new Array(1002801, 1462052, 1462006, 1462009, 1452012, 1472031, 1482010, 2044701, 2044501, 2044801);
// Stormbreaker Badges (4032006)
var stormbreakerRewards = new Array(1332077, 1322062, 1302068, 4032016, 2043001, 2043201, 2044401, 2044301);
// Nightshadow Badges (4032009)
var nightshadowRewards = new Array(1472072, 1332077, 1402048, 1302068, 4032017, 4032015, 2043023, 2043101, 2043301, 2044901);
// Firebrand Badges (4032008)
var firebrandRewards = new Array(1002801, 1382008, 1382006, 4032016, 4032015, 2043701, 2043801, 2022245);

function start() {
	if (!(cm.isQuestCompleted(4914) && cm.isQuestCompleted(4915) &&
			cm.isQuestCompleted(4916) && cm.isQuestCompleted(4917) &&
			cm.isQuestCompleted(8224) && cm.isQuestCompleted(8219) &&
			cm.isQuestCompleted(8227) && cm.isQuestCompleted(8228) &&
			cm.isQuestCompleted(8229) && cm.isQuestCompleted(8225))) {
		cm.sendOk("Strangers are not welcome here.");
		cm.dispose();
		return;
	} else {
		//cm.sendSimple("Oh, huh-lo! #b\r\n#L1#Phantom Forest#l\r\n#L2#Badge Redemption#l\r\n#L3#<Raven Ninjas>#l#k");
		cm.sendOk("Welcome to the #bRaven Ninja Clan#k.");
		cm.dispose();
	}
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	}
	if (mode == 0) {
		cm.dispose();
		return;
	} else {
		if (selection == 1) {
			cm.sendOk("Not completed.");
			cm.dispose();
		} else if (selection == 2) {
			cm.sendSimple("Yes, Taggrin put me in charge of handling bounties. And yes, I'm authorized to speak to you about them, so, um.. what do you need? #b" +
						"\r\n#L20#What do I need to collect?#l" +
						"\r\n#L21#What do I get for turning in badges?#l" +
						"\r\n#L22#I'd like to turn in items for a bounty.#l");
		} else if (selection == 3) {
			cm.sendOk("Not completed.");
			cm.dispose();
		} else if (selection == 22) {
			text = "You've collected the items? Wow, you must be pretty tough! I'm impressed! What would you like to turn in? #b";
			for (var i = 0; i < badges.length; i++) {
				text += "\r\n#L20" + i + "#50 #t" + badges[i] + "#s#l";
			}
			text += "\r\n#L204#";
			for (var i = 0; i < badges.length; i++) {
				text += "25 #t" + badges[i] + "#s, ";
			}
			cm.sendSimple(text);
		} else if (selection == 200) {
			while (i < 0.5) {
				var randmm = (Math.random() * windraiderRewards.length) | 0;
				if (randmm == 0) {
					cm.getPlayer().gainMeso(750000 * cm.getPlayer().getMesoRate(), false);
				}
				cm.gainItem(windraiderRewards[randmm], 1);
				i = (Math.random() * 2) | 0;
			}
			cm.sendOk("There you go! Thanks!");
			cm.dispose();
        } else if (selection == 201) {
			while (i < 0.5) {
				var randmm = (Math.random() * stormbreakerRewards.length) | 0;
				if (randmm == 0) {
					cm.getPlayer().gainMeso(1250000 * cm.getPlayer().getMesoRate(), false);
				}
				cm.gainItem(stormbreakerRewards[randmm], 1);
				i = (Math.random() * 2) | 0;
			}
			cm.sendOk("There you go! Thanks!");
			cm.dispose();
        } else if (selection == 202) {
			while (i < 0.5) {
				var randmm = (Math.random() * nightshadowRewards.length) | 0;
				if (randmm == 0) {
					cm.getPlayer().gainMeso(2500000 * cm.getPlayer().getMesoRate(), false);
				}
				cm.gainItem(nightshadowRewards[randmm], 1);
				i = (Math.random() * 2) | 0;
			}
			cm.sendOk("There you go! Thanks!");
			cm.dispose();
        } else if (selection == 203) {
			while (i < 0.5) {
				var randmm = (Math.random() * firebrandRewards.length) | 0;
				if (randmm == 0) {
					cm.getPlayer().gainMeso(1750000 * cm.getPlayer().getMesoRate(), false);
				}
				cm.gainItem(firebrandRewards[randmm], 1);
				i = (Math.random() * 2) | 0;
			}
			cm.sendOk("There you go! Thanks!");
			cm.dispose();
        } else if (selection == 204) {
			cm.getPlayer().gainMeso(3500000 * cm.getPlayer().getMesoRate(), false);
			cm.sendOk("There you go! Thanks!");
			cm.dispose();
		}
    }
}