/**
 *	@Modified: iPoopMagic (David)
 */

function enter(pi) {
	if (pi.getPlayer().getJob().getId() >= 300 && pi.getPlayer().getJob().getId() <= 340) {
		pi.warp(610030540, 0);
	} else {
		pi.playerMessage(5, "Only Bowmen may enter this portal.");
	}
}