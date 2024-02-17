/**
 *	@Modified: iPoopMagic (David)
 */
 
function enter(pi) {
	if (pi.getPlayer().getJob().getId() >= 400 && pi.getPlayer().getJob().getId() <= 440) {
		pi.warp(610030530, 0);
	} else {
		pi.playerMessage(5, "Only Thieves may enter this portal.");
	}
}