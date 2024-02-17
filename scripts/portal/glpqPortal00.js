/**
 *	@Modified: iPoopMagic (David)
 */

function enter(pi) {
	if (pi.getPlayer().getJob().getId() >= 100 && pi.getPlayer().getJob().getId() <= 140) {
		pi.warp(610030510, 0);
	} else {
		pi.playerMessage(5, "Only Warriors may enter this portal.");
	}
}