
var status;
var test = Array();

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
	cm.sendNext("NPC: " + cm.getNpc() + " is not found, please report this.");
	while(status >= 0){
		test.push(status);
	}
}