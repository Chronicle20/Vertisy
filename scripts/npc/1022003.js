var status = -1;
function start() {
    status = -1;
    action(1, 0, 0);
}

var pos = -1;

function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	
	var equipInv = cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP);
	var ii = Packages.server.ItemInformationProvider.getInstance();
	
	if(status == 0){
		
		if(pos == -1){
			var totalGear = 0;
			
			var text = "Hello #b#h ##k I can repair your damaged equipment for a small price! Please Select what you would like repaired.\r\n";
			
			text += "\r\n";
			
			for(i = 1; i <= 96; i++){
				var item = equipInv.getItem(i);
				if(item != null){
					var data = ii.getItemData(item.getItemId());
					if(data != null && data.exists){
						var equip = cm.itemToEquip(item);
						if(equip.needsRepair()){
							//if(equip.getDurability() < 30000){
								text += "#L" + item.getPosition() + "##i" + item.getItemId() + ":#  " + equip.getDurability() + " durability.#l\r\n";
								totalGear++;
							//}
						}
					}
				}
			}
			
			text += "\r\n\r\n";
			
			if(totalGear > 0)cm.sendSimple(text);
			else cm.sendSay("Looks like you don't need anything repaired! Get killing those monsters!", false, false);
		}else{
			cm.sendSay("Maybe next time!", false, false);
		}
	}else if(status == 1){
		pos = selection;
		var item = equipInv.getItem(pos);
		cm.sendYesNo("Would you like to repair #i" + item.getItemId() + ":# for " + getPrice() + " mesos?");
	}else if(status == 2){
		var item = equipInv.getItem(pos);
		var equip = cm.itemToEquip(item);
		
		cm.gainMeso(-getPrice());
		equip.setDurability(30000);
		cm.getPlayer().forceUpdateItem(equip);
		cm.sendSay("Should be all ready to kill more of those nasty monsters! Come back whenever you need more gear repaired!", false, false);
	}
}

function getPrice(){
	var equipInv = cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP);
	var ii = Packages.server.ItemInformationProvider.getInstance();
	
	var item = equipInv.getItem(pos);
	var equip = cm.itemToEquip(item);
	
	var data = ii.getItemData(item.getItemId());
	
	var itemLevel = data.reqLevel;
	//cm.println("itemLevel: " + itemLevel);
	var stars = equip.getChuc();
	
	var price = (itemLevel * 1000);
	if(stars > 0)price += (stars * 1.15);
	//cm.println("Price: " + price);
	var durability = equip.getDurability();
	//cm.println("Durability: " + durability);
	var reductionAmount = durability / 30000;
	
	//cm.println("Price Reduction: " + reductionAmount);
	var reduced = price * reductionAmount;
	price -= reduced;
	
	
	return Math.round(price);
}