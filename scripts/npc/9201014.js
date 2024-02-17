/**
 * NPC: 9201014 (Pila Present)
 * Description: Treasure/present exchange
 * Script: The Official Nexon MapleStory Pila Present NPC ported from "presentExchange" Nexon Script.
*/

function start() {
    if (cm.getMapId() == 680000100 || cm.getMapId() == 680000200) {
        cm.sendSimple("Who do you want to give your present for? \r\n#L0#I will give my present for #bhandsome Groom#k.#l\r\n#L1#I will give my present for #bpretty Bride#k.#l");
    } else if (cm.getMapId() == 680000000) {
        cm.dispose();
    } else {
        cm.dispose();
    }
}

function action(m,t,s) {
    if (cm.getMapId() == 680000100 || cm.getMapId() == 680000200) {
        // at the wating room for invited people
        cm.dispose();
    }
}

/*
field = self.field;
inventory = target.inventory;

    if (field.id == 680000100 or field.id == 680000200) {
    // at the wating room for invited people
    v1 = self.askMenu( "Who do you want to give your present for? \r\n#L0#I will give my present for #bhandsome Groom#k.#l\r\n#L1#I will give my present for #bpretty Bride#k.#l" );
        if (v1 == 0){
            ret = target.openWishList( 1, 0 );
            if (ret == 1) self.say ( "There's no wedding currently in progress.");
        }
    if (v1 == 1){
        ret = target.openWishList( 1, 1 );
        if (ret == 1) self.say ( "There's no wedding currently in progress.");
    }    
    }
    else if (field.id == 680000000 ){
    // at the amoria for wedding couple
    v1 = self.askMenu( "How do I help you? \r\n#b#L0#I am about to finish my wedding and want to pick my presents which my friends gave to me.#l\r\n#L1#I have an #rOnyx Chest#k and want to ask for you to open it.#l\r\n#L2#I have an #rOnyx Chest for Bride and Groom#k and want to ask for you to open it.#l#k" );
        if (v1 == 0 and target.nGender == 0 ){
            ret = target.openWishList( 2, 0 );
            if (ret == 2) self.say ( "You are currently not married.");
        }
        if (v1 == 0 and target.nGender == 1 ){
            ret = target.openWishList( 2, 1 );
            if (ret == 2) self.say ( "You are currently not married.");
        }
        if (v1 == 1){
        // Onyx Chest 
        nRet1 = self.askYesNo ( "I've got some fabulous items ready for you. Are you ready to pick them out?" );
        if ( nRet1 != 0 )    {    // answer : Yes        
        if ( inventory.itemCount( 4031423 ) < 1 )    // there's no Onyx Chest
        self.say ( "I don't think you have an Onyx Chest that I can open, kid..." );
        else { // there's Onyx Chest
            if ( inventory.slotCount( 2 ) > inventory.holdCount( 2 ) and inventory.slotCount( 4 ) > inventory.holdCount( 4 ) and inventory.slotCount( 1 ) > inventory.holdCount( 1 ) )
             { // there's  empty slot.
                rn1 = random( 1, 10000 );
                        // opened the onyx chect                        
                if (1 <= rn1 and rn1 <= 2000) {
                ret = inventory.exchange( 0, 4031423, -1, 2022011, 1 );

                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2022011# #b#t2022011# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");                
                }
                else if (2000 < rn1 and rn1 <= 3500)     {
                ret = inventory.exchange( 0, 4031423, -1, 2020020, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2020020# #b#t2020020# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (3500 < rn1 and rn1 <= 4700)     {
                ret = inventory.exchange( 0, 4031423, -1, 2022001, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2022001# #b#t2022001# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (4700 < rn1 and rn1 <= 5900)     {
                ret = inventory.exchange( 0, 4031423, -1, 2022015, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2022015# #b#t2022015# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (5900 < rn1 and rn1 <= 7100)     {
                ret = inventory.exchange( 0, 4031423, -1, 2012001, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2012001# #b#t2012001# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (7100 < rn1 and rn1 <= 8000) {
                ret = inventory.exchange( 0, 4031423, -1, 2020015, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2020015# #b#t2020015# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (8000 < rn1 and rn1 <= 8900) {
                ret = inventory.exchange( 0, 4031423, -1, 2022000, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2022000# #b#t2022000# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (8900 < rn1 and rn1 <= 9400) {
                ret = inventory.exchange( 0, 4031423, -1, 2002011, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2002011# #b#t2002011# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9400 < rn1 and rn1 <= 9670) {
                ret = inventory.exchange( 0, 4031423, -1, 4021007, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v4021007# #b#t4021007# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9670 < rn1 and rn1 <= 9770) {
                ret = inventory.exchange( 0, 4031423, -1, 2001002, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2001002# #b#t2001002# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9770 < rn1 and rn1 <= 9870) {
                ret = inventory.exchange( 0, 4031423, -1, 2048001, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2048001# #b#t2048001# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9870 < rn1 and rn1 <= 9920) {
                ret = inventory.exchange( 0, 4031423, -1, 4021008, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v4021008# #b#t4021008# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9920 < rn1 and rn1 <= 9945) {
                ret = inventory.exchange( 0, 4031423, -1, 1102024, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v1102024# #b#t1102024# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9945 < rn1 and rn1 <= 9965) {
                ret = inventory.exchange( 0, 4031423, -1, 2041007, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2041007# #b#t2041007# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9965 < rn1 and rn1 <= 9985) {
                ret = inventory.exchange( 0, 4031423, -1, 2041010, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2041010# #b#t2041010# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9985 < rn1 and rn1 <= 9990) {
                ret = inventory.exchange( 0, 4031423, -1, 4011007, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v4011007# #b#t4011007# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9990 < rn1 and rn1 <= 9995) {
                ret = inventory.exchange( 0, 4031423, -1, 4021009, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v4021009# #b#t4021009# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9995 < rn1 and rn1 <= 9998) {
                ret = inventory.exchange( 0, 4031423, -1, 2000004, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2000004# #b#t2000004# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (9998 < rn1 and rn1 <= 10000) {
                ret = inventory.exchange( 0, 4031423, -1, 2000005, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2000005# #b#t2000005# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }                                
            } 
            else // there's no empty slot.
                self.say("I'm sorry, but I can't open and give you this present if your inventory is full! Check your inventory and make the necessary adjustments~"); 
            
        }
        }    
        else // answer : No
        self.say ( "Awww, really? I'm the only one who can open your Onyx Chest! I will be here and wait for you~" );
        }
        if (v1 == 2){
        // Onyx Chest 
        nRet1 = self.askYesNo ( "I've got some fabulous items ready for you. Are you ready to pick them out?" );
        if ( nRet1 != 0 )    {    // answer : Yes        
        if ( inventory.itemCount( 4031424 ) < 1 )    // there's no Onyx Chest
        self.say ( "I don't think you have an #rOnyx Chest for Bride and Groom#k that I can open, kid..." );
        else { // there's Onyx Chest
            if ( inventory.slotCount( 2 ) > inventory.holdCount( 2 ) )
             { // there's  empty slot.
                rn1 = random( 1, 1000 );
                        // opened the onyx chect                        
                if (1 <= rn1 and rn1 <= 190) {
                ret = inventory.exchange( 0, 4031424, -1, 2000006, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2000006# #b#t2000006# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");                
                }
                else if (190 < rn1 and rn1 <= 390)     {
                ret = inventory.exchange( 0, 4031424, -1, 2000005, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2000005# #b#t2000005# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (390 < rn1 and rn1 <= 499)     {
                ret = inventory.exchange( 0, 4031424, -1, 2000004, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2000004# #b#t2000004# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (499 < rn1 and rn1 <= 599)     {
                ret = inventory.exchange( 0, 4031424, -1, 2022123, 2 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n 2 #v2022123# #b#t2022123# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (599 < rn1 and rn1 <= 619)     {
                ret = inventory.exchange( 0, 4031424, -1, 1102021, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v1102021# #b#t1102021# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (619 < rn1 and rn1 <= 639) {
                ret = inventory.exchange( 0, 4031424, -1, 1102024, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v1102024# #b#t1102024# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }            
                else if (639 < rn1 and rn1 <= 689) {
                ret = inventory.exchange( 0, 4031424, -1, 2022121, 2 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n 2 #v2022121# #b#t2022121# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (689 < rn1 and rn1 <= 699) {
                ret = inventory.exchange( 0, 4031424, -1, 1032027, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v1032027# #b#t1032027# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (699 < rn1 and rn1 <= 774) {
                ret = inventory.exchange( 0, 4031424, -1, 2041022, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2041022# #b#t2041022# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (774 < rn1 and rn1 <= 849) {
                ret = inventory.exchange( 0, 4031424, -1, 2041019, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2041019# #b#t2041019# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (849< rn1 and rn1 <= 924) {
                ret = inventory.exchange( 0, 4031424, -1, 2041016, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2041016# #b#t2041016# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }                
                else if (924< rn1 and rn1 <= 999) {
                ret = inventory.exchange( 0, 4031424, -1, 2041013, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v2041013# #b#t2041013# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }
                else if (999 < rn1 and rn1 <= 1000) {
                ret = inventory.exchange( 0, 4031424, -1, 1082148, 1 );
                if (ret == 1) self.say ( "No problem! Give my best to the happy couple. I'll open this present for you now. Voila! \r\n #v1082148# #b#t1082148# " );
                else self.say("I'm sorry, but I can't give you the present if your inventory is full! Check your inventory and make the necessary adjustments~");
                }                                        
            } 
            else // there's no empty slot.
                self.say("I'm sorry, but I can't open and give you this present if your inventory is full! Check your inventory and make the necessary adjustments~"); 
        }
        }    
        else // answer : No
        self.say ( "Awww, really? I'm the only one who can open your Onyx Chest! I will be here and wait for you~" );
        }    
}*/  