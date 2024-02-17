var ticket = new Array(4031047, 4031074, 4031331, 4031576);
var cost = new Array(5000, 6000, 30000, 6000);
var time = new Array(15, 10, 10, 10);
var mapNames = new Array("Ellinia of Victoria Island", "Ludibrium", "Leafre", "Ariant");
var mapName2 = new Array("Ellinia of Victoria Island", "Ludibrium", "Leafre of Minar Forest", "Nihal Desert");
//var maps = new Array(101000300, 220000100, 240000100, 260000100, 250000100);
var select;
var status = 0;

function start() {
//    var where = "Hello. I am in charge of warping people to certain maps. Would you like to purchase a warp? If so, where do you want to go?\r\n";
	var where = "Hello, I'm in charge of selling tickets for the ship ride for every destination. Which ticket would you like to purchase?\r\n";
    for (var i = 0; i < mapNames.length; i++)
        where += "\r\n#L" + i + "##b" + mapNames[i] + "#k#l";
    cm.sendSimple(where);
}

function action(mode, type, selection) {
    if(mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            select = selection;
            cm.sendYesNo("The ship to " + mapName2[select] + " leaves every " + time[select] + " minutes, starting on the hour and it will cost you #b" + cost[select] + " mesos#k. Are you sure you want to buy a #b#t" + ticket[select] + "#?");
        } else if(status == 2) {
			if (!cm.canHold(ticket[select]) || cm.getMeso() < cost[select]) {
                cm.sendOk("Are you sure you have #b"+cost[select]+" mesos#k? If so, then I urge you to check you etc. inventory, and see if it's full or not.");
            } else {
                cm.gainMeso(-cost[select]);
                cm.gainItem(ticket[select],1);
            }
            cm.dispose();
        }
    }
}