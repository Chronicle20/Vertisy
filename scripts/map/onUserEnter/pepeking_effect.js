
function start(ms) {
    if (ms.getPlayer().getMap().countMonster(3300007) >= 1) {
	ms.unlockUI();   	       
	ms.showIntro("Effect/Direction2.img/mushCatle/pepeKing0");
    } else if (ms.getPlayer().getMap().countMonster(3300006) >= 1) {
	ms.unlockUI();   	       
	ms.showIntro("Effect/Direction2.img/mushCatle/pepeKing1");
    } else if (ms.getPlayer().getMap().countMonster(3300005) >= 1) {
	ms.unlockUI();   	       
	ms.showIntro("Effect/Direction2.img/mushCatle/pepeKing2");
    }
}
