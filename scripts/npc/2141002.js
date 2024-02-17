/** 
 *	@Name: The Forgotten Temple Manager
 *	@Map: Deep Place of Temple - Twilight of Gods
 *	@Description: Pink Bean Expedition
 *	@Author: iPoopMagic (David)
*/

function start() {
	cm.sendYesNo("If you leave now, you won't be able to return. Are you sure you want to leave?");
}

function action(mode, type, selection) {
	var expedition = cm.getExpedition(Packages.server.expeditions.MapleExpeditionType.PINKBEAN);
	if (mode < 1) {
		cm.dispose();
	} else {
		if (cm.getPlayer().getMap().getCharacters().size() < 2){
			cm.getPlayer().getMap().killAllMonsters();
			cm.getPlayer().getMap().resetReactors();
			if (expedition != null){
				cm.endExpedition(expedition);
			}
		}
		if (expedition != null) {
			expedition.removeMember(cm.getPlayer());
		}
		if (cm.getPlayer().getEventInstance() != null) {
			cm.getPlayer().getEventInstance().removePlayer(cm.getPlayer());
		} else {
			cm.warp(270050000);
		}
		cm.dispose();
	}
}