/**
 *	@Name: Yulete (Magatia PQ)
 */
function start() {
    var em = cm.getEventManager("Juliet");
    if (em == null) {
		cm.sendOk("The event is not working, please contact a GM immediately.");
		cm.dispose();
		return;
    }
    if (em.getProperty("stage").equals("1") && cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getMonstersEvent(cm.getPlayer()).size() > 1) {
		cm.sendOk("What... a suspicious conspiracy? This can't be...");
		em.setProperty("stage", "2");
    } else if (em.getProperty("stage5").equals("1") && cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getMonstersEvent(cm.getPlayer()).size() < 1) {
		cm.sendOk("Mwahaha!!! The end awaits you on the other side.");	
		em.setProperty("stage5", "2");
		cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).setReactorState();
    } else {
		cm.sendOk("Mwahaha!!! The end awaits you on the other side.");	
    }
    cm.dispose();
}