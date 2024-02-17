/**
* Paul for events in Southperry
*/

var status;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
		
	if(status == 0){
		if(cm.getClient().getChannelInstance().getEvent() == null){
			cm.sendOk("No event is currently in progress.");
		}else{
			if(!cm.getClient().getChannelInstance().getEvent().enter(cm.getPlayer())){
				cm.sendOk("Unable to enter the Event. It may have already started.");
			}	
		}
		cm.dispose();
	}
}