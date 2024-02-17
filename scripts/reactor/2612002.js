/**
 *	@Description: Box in Dark Lab 1
 *	@Author: iPoopMagic (David)
 */
function act() {
	rm.dropItems(); // drops Alcadno Experiment (4001134)
	rm.getPlayer().getEventInstance().getMapInstance(rm.getPlayer().getMapId()).getReactorById(2618003).setState(1);
}