/**
 *	@Modified: iPoopMagic (David)
 */

function enter(pi) {
	if (pi.getPlayer().getJob().getId() >= 200 && pi.getPlayer().getJob().getId() <= 240) {
		pi.warp(610030521, 0);
	} else {
		pi.playerMessage(5, "Only Mages may enter this portal.");
	}
}