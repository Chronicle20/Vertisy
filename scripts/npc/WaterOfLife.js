/* 
	Mar the Fairy: 1032102
	Water of life
 */

var status;
var pets;

function start(petsArg){
	pets = petsArg;
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
	
	if(pets.isEmpty()){//no need to check for the item.. They shouldn't be able to open this npc without the item
		cm.sendOk("You don't have any dolls for me to reawaken.");
		cm.dispose();
		return;
	}
	
	if(status == 0){
		cm.sendYesNo("I am Mar the Fairy. You have the #bWater of Life#k... With this, I can bring a doll back to life with my magic. What do you think? Do you want to use this item and reawaken your pet?");
	}else if(status == 1){
		cm.sendPets("So which pet do you want to reawaken? Please choose the pet you'd most like to reawaken", pets);
	}else if(status == 2){
		var petItem = pets.get(cm.getPlayer().getPetItemByPetId(selection));
		if(petItem != null){
			petItem.setExpiration(cm.getCurrentTime() + (90 * 24 * 60 * 60 * 1000));
			cm.getPlayer().forceUpdateItem(petItem);
			cm.gainItem(5180000, -1);
			cm.sendNext("Your doll has now reawakened as your pet! However, my magic isn't perfect, so I can't promise an eternal life for your pet... Please take care of that pet before the Water of Life dries. Well then, good bye.");
			cm.dispose();
		}
	}
}