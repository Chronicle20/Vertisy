/**
 *	@Author: iPoopMagic (David)
 *	@Description: Investigation (find the button) - Romeo
 */
function enter(pi) {
    var em = pi.getEventManager("Romeo");
    if (em != null && em.getProperty("stage1").equals("finished")) {
		pi.warp(926100001, 0);
    } else {
		pi.playerMessage(5, "The portal has not been opened yet.");
    }
}