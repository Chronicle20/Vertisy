/**
 *	@Modified: iPoopMagic (David)
 */

function enter(pi) {
	if (pi.getPlayer().getJob().getId() >= 500 && pi.getPlayer().getJob().getId() <= 540) {
		pi.warp(610030550, 0);
	} else {
		pi.playerMessage(5, "Only Pirates may enter this portal.");
	}
}