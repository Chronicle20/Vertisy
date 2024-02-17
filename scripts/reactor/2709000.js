/**
 *	@Map: Twilight of the Gods
 *	@Description: Summons Pink Bean
 *	@Author: iPoopMagic (David)
*/

function act() {
    rm.spawnMonster(8820008);
	rm.spawnMonster(8820000);
	rm.spawnMonster(8820003);
	rm.spawnMonster(8820019);
	rm.spawnMonster(8820024);
	rm.spawnMonster(8820025);
	rm.spawnMonster(8820026);
	rm.spawnMonster(8820010);
	rm.removeNpc(2141000);
	rm.getReactor().getMap().getPinkBeanDeadMobs().clear();
}