
function start(text) {
	var info = text;
	cm.sendSimple(info);
}

function action(mode, type, selection) {
	if(mode == 1){
		cm.gainItem(selection, 1);
	}
	cm.dispose();
}