/**
 *	@Description: Box in Dark Lab 2
 *	@Author: iPoopMagic (David)
 */
function act() {
	rm.dropItems(); // drops Zenumist Experiment (4001135)
	rm.getPlayer().getEventInstance().getMapInstance(rm.getPlayer().getMapId()).getReactorById(2618004).setState(1);
}