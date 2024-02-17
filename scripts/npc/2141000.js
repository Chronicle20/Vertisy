/**
 *	@NPC: Kirston
 *	@Map: Twilight of the Gods
 *	@Description: Summons Pink Bean
 *	@Author: iPoopMagic (David)
*/

function start() {
	if (cm.getEventManager("PinkBean").getProperty("kirston") == "false") {
		cm.sendYesNo("If only I had the Mirror of Goodness then I can re-summon the Black Wizard! \r\nWait! something's not right! Why is the Black Wizard not summoned? Wait, what's this force? I feel something... totally different from the Black Wizard Ahhhhh!!!!! \r\n\r\n #b(Places a hand on the shoulder of Kryston.)");
	} else {
		cm.sendOk("NO! I didn't mean for this to happen!");
		cm.dispose();
	}
}

function action(mode, type, selection) {
    if (mode > 0) {
		cm.getEventManager("PinkBean").setProperty("kirston", "true");
		cm.forceStartReactor(270050100, 2709000);
    }
    cm.dispose();
}