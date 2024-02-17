/**
* Vertisy 2016 Halloween NPC
*/
var candy = 4031203;
var crystal = 4032056;


var status;
var category;
var itemSel;

var itemAmount;


var items = {
	//itemid, candies, crystals, required item
	Caps: [
		[1003789, 20000, 0, 0],
		[1004313, 20000, 0, 0],
		[1004314, 20000, 0, 0],
		[1004315, 20000, 0, 0],
		[1004316, 20000, 0, 0],
		[1004317, 20000, 0, 0],
		[1004318, 20000, 0, 0],
		[1004319, 20000, 0, 0],
		[1004320, 20000, 0, 0],
		[1004321, 20000, 0, 0]
	],
	Accessory: [
		[1012338, 20000, 0, 0]
	],
	Belts: [
		[1132014, 15000, 5, 0],
		[1132015, 30000, 10, 1132014],
		[1132016, 60000, 15, 1132015]
	],
	Capes: [
		[1102868, 20000, 0, 0]
	],
	Overalls: [
		[1050248, 20000, 0, 0],
		[1051376, 20000, 0, 0]
	],
	Use: [
        [2022245, 5000, 0, 0],
        [2022273, 4500, 0, 0],
        [2049002, 4000, 0, 0],
        [2022105, 300, 0, 0, 100],
        [2022107, 600, 0, 0, 100],
        [2022247, 30, 0, 0, 100],
        [2022250, 20, 0, 0, 100],
        [2022248, 10, 0, 0, 100],
        [2022249, 10, 0, 0, 100]
    ]
};

var categories = ['Caps', 'Accessory', 'Belts', 'Capes', 'Overalls', 'Use'];

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
		if(cm.haveItem(candy, 1)){
			var text = "I see you've been hunting this Halloween season, I have a few rewards for you. What would you like?\r\n";
			var index = 0;
			for(type in items){
				text += "#L" + index + "#" + type + "#l\r\n";
				index++;
			}
			cm.sendSimple(text);
		}else{
			cm.sendOk("You don't have the Halloween Candies I need! Get back hunting.");
			cm.dispose();
		}
	}else if(status == 1){
		category = selection;
		var type = categories[category];
		var text = "";
		for(i = 0; i < items[type].length; i++){
			text += "#L" + i + "##b#v" + items[type][i][0] + "# #t" + items[type][i][0] + "#: #k";
			if(items[type][i][1] > 0){
				text += cm.getFormattedInt(items[type][i][1]) + "#v" + candy + "#  "; 
			}
			if(items[type][i][2] > 0){
				text += cm.getFormattedInt(items[type][i][2]) + "#v" + crystal + "#"; 
			}
			text += "#l\r\n";
		}
		cm.sendSimple(text);
	}else if(status == 2){
		itemSel = selection;
		var type = categories[category];
		if(itemSel > items[type].length){
			cm.dispose();
			return;
		}
		
		var maxAmount = items[type][itemSel][4];
		
		if(maxAmount == undefined || itemAmount != undefined){
			if(itemAmount == undefined)itemAmount = 1;
			if(cm.canHold(items[type][itemSel][0], itemAmount)){
				var candies = items[type][itemSel][1];
				var crystals = items[type][itemSel][2];
				var requiredItem = items[type][itemSel][3];
				
				if(candies > 0){
					if(!cm.haveItem(candy, candies * itemAmount)){
						cm.sendOk("You need " + (candies * itemAmount) + " #t" + candy + "# to purchase this item.");
						cm.dispose();
						return;
					}
				}
				if(crystals > 0){
					if(!cm.haveItem(crystal, crystals * itemAmount)){
						cm.sendOk("You need " + (crystals * itemAmount) + " Magic Crystals to purchase this item.");
						cm.dispose();
						return;
					}
				}
				
				if(requiredItem > 0){
					if(cm.haveItem(requiredItem, itemAmount)){
						cm.gainItem(requiredItem, -itemAmount);
					}else{
						cm.sendOk("You need a #t" + requiredItem + "# to purchase this item.");
						cm.dispose();
						return;
					}
				}
				
				cm.gainItem(candy, -(candies * itemAmount));
				cm.gainItem(crystal, -(crystals * itemAmount));
				cm.gainItem(items[type][itemSel][0], itemAmount);
				cm.dispose();
			}else{
				cm.sendOk("You cannot buy that item. Please open clear a spot in your inventory.");
				cm.dispose();
			}
		}else{
			cm.sendGetNumber("How many #t" + items[type][itemSel][0] + "# do you want to buy?", 1, 1, maxAmount);
		}
	}else if(status == 3){
		itemAmount = selection;
		status = status - 2;
		action(mode, type, itemSel);
	}
}