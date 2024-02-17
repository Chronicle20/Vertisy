var status;
var itemPos = 0;
var sel;

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
		
	var given = 0;
	
	if(cm.getClient().isProgressValueSet("nx-given")){
		given = parseInt(cm.getClient().getProgressValue("nx-given"));
	}
	
	var equipInv = cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP);
	
	if(status == 0){
		var totalNX = 0;
		
		var text = "Hello #b#h ##k I convert your unused NX Equips into points which you can later convert into Cash Shop Surprise Boxes.\r\nYou currently have given me " + given + " NX Equip" + (given == 1 ? "" : "s") + ".\r\n";
		
		text += "\r\n";
		
		var ii = Packages.server.ItemInformationProvider.getInstance();
		
		
		for(i = 0; i <= equipInv.getSlotLimit(); i++){
			var item = equipInv.getItem(i);
			if(item != null){
				var data = ii.getItemData(item.getItemId());
				if(data != null && data.isCash){
					text += "#L" + item.getPosition() + "##i" + item.getItemId() + "##l  ";
					totalNX++;
				}
			}
		}
		
		text += "\r\n\r\n";
		if(totalNX > 1){
			text += "#L499# Convert #r#eALL#n#k NX Equips into points.\r\n";
		}
		if(given >= 10){
			text += "#L500# Convert 10 points into a Cash Shop Surprise#l";
		}
		
		if(totalNX >= 1 || given >= 10)cm.sendSimple(text);
		else{
			cm.sendOk(text);
			cm.dispose();
		}
	}else if(status == 1){
		sel = selection;
		if(selection == 500){
			if(cm.canHold(5222000)){
				if(given >= 10){
					given -= 10;
					cm.getClient().setProgressValue("nx-given", "" + given);
					cm.gainItem(5222000, 1);
					cm.sendOk("Enjoy your Cash Shop Surprise Box.");
					cm.dispose();
				}else{
					cm.sendOk("You need to give me 10 nx equips before I can give you a Cash Shop Surprise.");
					cm.dispose();
				}
			}else{
				cm.sendOk("Please open a space in your Cash inventory.");
				cm.dispose();
			}
		}else if(selection == 499){
			cm.sendYesNo("Are you sure you want to convert #r#eALL#n#k NX Equips in your Equip Inventory into points?");
		}else{
			itemPos = selection;
			var item = equipInv.getItem(itemPos);
			if(item != null)cm.sendYesNo("Are you sure you would like to delete #i" + item.getItemId() + "#");
		}
	}else if(status == 2){
		if(sel == 499){
			var ii = Packages.server.ItemInformationProvider.getInstance();
			for(i = 0; i <= equipInv.getSlotLimit(); i++){
				var item = equipInv.getItem(i);
				if(item != null){
					var data = ii.getItemData(item.getItemId());
					if(data != null && data.isCash){
						equipInv.removeItem(i);
						cm.getClient().announce(Packages.tools.MaplePacketCreator.modifyInventory(false, Packages.java.util.Collections.singletonList(new Packages.client.inventory.ModifyInventory(3, item))));
						given += 1;
					}
				}
			}
			cm.getClient().setProgressValue("nx-given", "" + given);
		}else{
			var item = equipInv.getItem(itemPos);
			
			if(item != null){
				equipInv.removeItem(itemPos);
				cm.getClient().announce(Packages.tools.MaplePacketCreator.modifyInventory(false, Packages.java.util.Collections.singletonList(new Packages.client.inventory.ModifyInventory(3, item))));
				given += 1;
				cm.getClient().setProgressValue("nx-given", "" + given);
			}
		}
		cm.dispose();
	}
}