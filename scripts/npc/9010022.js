var status; 
var sel; 

function start() { 
    status = -1; 
    action(1, 0, 0); 
} 

function action(mode, type, selection) { 
    if (mode == -1) { 
        cm.dispose(); 
    } else { 
        if (mode == 0) { 
            cm.dispose(); 
            return; 
        } 
        if (mode == 1) 
            status++; 
        else 
            status--; 
            if (status == 0) {
            if (cm.getLevel() < 20) { 
                cm.sendDimensionalMirror("#-1# There is no place for you to transport to."); 
                cm.dispose(); 
            } else {
            	//0 & 1 = dojo
            	//2 & 3 is mcpq
            	//4 some pirate head
            	//5 is ariant
            	//6 is Construction Site
                var selStr = ""; 
                /*if (cm.getLevel() >= 20 && cm.getLevel() <= 30) { 
                    selStr += "#0# Ariant Coliseum"; 
                }*/

                if (cm.getLevel() >= 10) { 
                    selStr += "#1# Boss PQ"; 
                }
                
                /*if (cm.getLevel() >= 10) { 
                    selStr += "#5# EVENT - Maple Hill"; 
                }*/

                /*if (cm.getLevel() >= 30 && cm.getLevel() <= 50) { 
                    selStr += "#2# Monster Carnival 1"; 
                } 

                if (cm.getLevel() >= 51 && cm.getLevel() <= 70) { 
                    selStr += "#3# Monster Carnival 2"; 
                }

                if (cm.getLevel() >= 25 && cm.getLevel() <= 30) { 
                    selStr += "#6# Construction Site"; 
                }
				
				if (cm.getLevel() >= 35 && cm.getLevel() <= 50) {
					selStr += "#11# Dimensional Crack";
				}
				
				if (cm.getLevel() >= 55 && cm.getLevel() <= 100) {
					selStr += "#14# Lord Pirate";
				}
				
                if (cm.getLevel() >= 71 && cm.getLevel() <= 85) {
					selStr += "#15# Romeo and Juliet";
				}
				
                if (cm.getLevel() >= 75) {
					selStr += "#16# Resurrection of the Hoblin King";
				}

                if (cm.getLevel() >= 100) {
					selStr += "#17# Dragon's Nest";
				}*/
				cm.sendDimensionalMirror(selStr); 
            } 
        } else if (status == 1) { 
            cm.getPlayer().saveLocation("MIRROR"); 
            switch (selection) { 
                /*case 0: 
                    cm.warp(980010000, 3); 
                    break;*/
                case 1: 
                    cm.warp(970030000); 
                    break; 
                /*case 5: //Event Hall
                    cm.warp(970010000, 1); 
                    break;*/
                /*case 2:
                    cm.warp(980000000, 3); 
                    break; 
                case 3: 
                    cm.warp(980030000, 3); 
                    break; 
                case 6: 
                    cm.warp(910320000); 
                    break; 
				case 7:
					cm.warp(221024500);
					break;
				case 8:
					cm.warp(251010404);
					break;*/
            } 
            cm.dispose(); 
        } 
    } 
}  
