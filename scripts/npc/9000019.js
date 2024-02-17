/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

function start(){
	cm.getClient().announce(Packages.tools.MaplePacketCreator.openRPSNPC());
	cm.dispose();
}