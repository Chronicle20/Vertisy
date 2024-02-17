/**
 * @Author: iPoopMagic (David)
 * @Description: Ghost Ship 7 Entrance
 */
function enter(pi) {
    if (!pi.haveItem(4000381)) {
		pi.playerMessage(5, "You do not have White Essence.");
    } else {
		var timeData = pi.getPlayer().getProgressValue("gs7-time");
		var customData = pi.getPlayer().getProgressValue("gs7-kills");
		if (timeData == "") {
			timeData = "0";
		}
		var time = parseInt(timeData);
		if (customData == null) {
			customData = "0";
		}
		var amtFought = parseInt(customData);
		if (time + (24 * 60 * 60 * 1000) <= pi.getCurrentTime()) {
			amtFought = 0;
		}
		if (amtFought > 2 && (time + (24 * 60 * 60 * 1000)) >= pi.getCurrentTime()/* && !pi.getPlayer().isGM()*/) {
			pi.playerMessage(6, "You may only attempt Captain Latanica three times a day. Time left until allowed re-entry: " + pi.getReadableMillis(pi.getCurrentTime(), time + (24 * 60 * 60 * 1000)));
			return false;
		} else {
			if (pi.getPlayerCount(541010100) <= 0) { // Capt. Lac Map
				if(pi.getPlayer().isProgressValueSet("gs7-time")){
					pi.getPlayer().setProgressValue("gs7-time", pi.getCurrentTime());
				}else pi.getPlayer().addProgressValue("gs7-time", pi.getCurrentTime());
				
				if(pi.getPlayer().isProgressValueSet("gs7-kills")){
					pi.getPlayer().setProgressValue("gs7-kills", amtFought + 1);
				}else pi.getPlayer().addProgressValue("gs7-kills", amtFought + 1);
				
				var captMap = pi.getClient().getChannelServer().getMap(541010100);
				captMap.clearAndReset(true);
				pi.playPortalSound();
				pi.warp(541010100, "sp");
				pi.spawnMonster(9420513, -136, 225);
			} else {
				/*if (pi.getPlayer().getClient().getChannelServer().getMap(541010100).getMonsters().size() <= 0) {
					pi.playPortalSound();
					pi.warp(541010100, "sp");
				} else {*/
					pi.playerMessage(5, "The battle against the boss has already begun, so you may not enter this place.");
				//}
			}
		}
    }
}