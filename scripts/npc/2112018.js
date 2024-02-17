/**
 *	@Name: Romeo & Juliet
 *	@Author: iPoopMagic (David)
 */
var rewards = new Array(
	2000003, 100, // Blue Potion
	2000002, 100, // White Potion
	2000004, 20, // Elixir
	2000005, 10, // Power Elixir
	2022003, 50, // Unagi
	1032016, 1, // Metal Heart Earrings
	1032015, 1, // Metal Silver Earrings
	1032014, 1, // Pink-Flowered Earrings
	2041212, 1, // Rock of Wisdom
	2041020, 1, // Cape for DEX 10%
	2040502, 1, // Overal Armor for DEX 10%
	2041016, 1, // Cape for INT 60%
	2044701, 1, // Claw for ATT 60%
	2040301, 1, // Earring for INT 60%
	2043201, 1, // One-Handed BW for ATT 60%
	2040501, 1, // Overall Armor for DEX 60%
	2040704, 1, // Shoes for Jump 60%
	2044001, 1, // Two-Handed Sword for ATT 60%
	2043701, 1, // Wand for Magic ATT 60%
	2040803, 1, // Gloves for ATT 100%
	1102028, 1, // Red Seraph Cape
	1102026, 1, // Green Seraph Cape
	1102029, 1); // White Seraph Cape

function start() {
	for (var i = 0; i < 6; i++) {
	    cm.removeAll(4001130 + i);
	}
	var em = cm.getEventManager(cm.getPlayer().getMapId() == 926100600 ? "Romeo" : "Juliet");
    if (em != null) {
		var itemid = cm.getPlayer().getMapId() == 926100600 ? 4001160 : 4001159;
		if (!cm.canHold(itemid)) {
			cm.sendOk("Please clear 1 ETC slot.");
			cm.dispose();
			return;
		}
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
			cm.sendOk("Please make space in your inventory!");
			return;
		}
		cm.gainItem(itemid, 1);		
		cm.gainItem(reward, quantity);
		if (!em.getProperty("stage").equals("finished")) {
			cm.givePartyQuestExp("MagatiaPQComplete"); // 140,000
		}
		em.setProperty("stage", "finished");
    }
    cm.warp(cm.getPlayer().getMapId() == 926100600 ? 926100700 : 926110700, 0);
    cm.dispose();
}