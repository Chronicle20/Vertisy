/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import constants.ServerConstants;
import tools.ExternalCodeTableGetter;
import tools.IntValueHolder;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public enum RecvOpcode implements IntValueHolder{
	LOGIN_PASSWORD,
	GUEST_LOGIN,
	SERVERLIST_REREQUEST,
	CHARLIST_REQUEST,
	SERVERSTATUS_REQUEST,
	ACCEPT_TOS,
	SET_GENDER,
	AFTER_LOGIN,
	REGISTER_PIN,
	SERVERLIST_REQUEST,
	PLAYER_DC,
	VIEW_ALL_CHAR,
	PICK_ALL_CHAR,
	CHAR_SELECT,
	PLAYER_LOGGEDIN,
	CHECK_CHAR_NAME,
	CREATE_CHAR,
	DELETE_CHAR,
	PONG,
	CLIENT_START_ERROR,
	CLIENT_ERROR,
	RELOG,
	REGISTER_PIC,
	CHAR_SELECT_WITH_PIC,
	VIEW_ALL_PIC_REGISTER,
	VIEW_ALL_WITH_PIC,
	PACKET_ERROR,
	CHANGE_MAP,
	CHANGE_CHANNEL,
	ENTER_CASHSHOP,
	MOVE_PLAYER,
	CANCEL_CHAIR,
	USE_CHAIR,
	CLOSE_RANGE_ATTACK,
	RANGED_ATTACK,
	MAGIC_ATTACK,
	TOUCH_MONSTER_ATTACK,
	TAKE_DAMAGE,
	GENERAL_CHAT,
	CLOSE_CHALKBOARD,
	FACE_EXPRESSION,
	USE_ITEMEFFECT,
	USE_DEATHITEM,
	MONSTER_BOOK_COVER,
	NPC_TALK,
	REMOTE_STORE,
	NPC_TALK_MORE,
	NPC_SHOP,
	STORAGE,
	HIRED_MERCHANT_REQUEST,
	FREDRICK_ACTION,
	DUEY_ACTION,
	OWL_OPEN,
	OWL_WARP,
	ADMIN_SHOP_REQUEST,
	GATHER_ITEM,
	SORT_ITEM,
	ITEM_MOVE,
	USE_ITEM,
	CANCEL_ITEM_EFFECT,
	STATE_CHANGE_BY_PORTABLE_CHAIR_REQUEST,
	USE_SUMMON_BAG,
	PET_FOOD,
	USE_MOUNT_FOOD,
	SCRIPTED_ITEM,
	USE_CASH_ITEM,
	USE_CATCH_ITEM,
	USE_SKILL_BOOK,
	USE_TELEPORT_ROCK,
	USE_RETURN_SCROLL,
	USE_UPGRADE_SCROLL,
	DISTRIBUTE_AP,
	AUTO_DISTRIBUTE_AP,
	HEAL_OVER_TIME,
	HyperUpgradeItemUse,
	ItemOptionUpgradeItemUse,
	ItemReleaseRequest,
	DISTRIBUTE_SP,
	SPECIAL_MOVE,
	CANCEL_BUFF,
	SKILL_EFFECT,
	MESO_DROP,
	GIVE_FAME,
	CHAR_INFO_REQUEST,
	SPAWN_PET,
	CANCEL_DEBUFF,
	CHANGE_MAP_SPECIAL,
	USE_INNER_PORTAL,
	TROCK_ADD_MAP,
	REPORT,
	QUEST_ACTION,
	USER_CALC_DAMAGE_STAT_SET_REQUEST,
	SKILL_MACRO,
	SPOUSE_CHAT,
	USE_ITEM_REWARD,
	MAKER_SKILL,
	USE_REMOTE,
	USE_WATER_OF_LIFE,
	UserFollowCharacterRequest,
	UserFollowCharacterWithdraw,
	SetPassenserResult,
	ADMIN_CHAT,
	PARTYCHAT,
	WHISPER,
	MESSENGER,
	PLAYER_INTERACTION,
	PARTY_REQUEST,
	PARTY_RESULT,
	GUILD_OPERATION,
	DENY_GUILD_REQUEST,
	ADMIN_COMMAND,
	ADMIN_LOG,
	BUDDYLIST_MODIFY,
	NOTE_ACTION,
	USE_DOOR,
	CHANGE_KEYMAP,
	RPS_ACTION,
	RING_ACTION,
	WEDDING_ACTION,
	ITEM_VAC_ALERT,
	OPEN_FAMILY,
	ADD_FAMILY,
	ACCEPT_FAMILY,
	USE_FAMILY,
	ALLIANCE_REQUEST,
	ALLIANCE_OPERATION,
	BBS_OPERATION,
	ENTER_MTS,
	USE_SOLOMON_ITEM,
	USE_GACHA_EXP,
	CLICK_GUIDE,
	ARAN_COMBO_COUNTER,
	MOB_CRC_KEY_CHANGED_REPLY,
	ACCOUNT_MORE_INFO,
	FIND_FRIEND,
	MOVE_PET,
	PET_CHAT,
	PET_COMMAND,
	PET_LOOT,
	PET_AUTO_POT,
	PET_EXCLUDE_ITEMS,
	MOVE_SUMMON,
	SUMMON_ATTACK,
	DAMAGE_SUMMON,
	BEHOLDER,
	MOVE_DRAGON,
	QUICKSLOT_CHANGE,
	MOVE_LIFE,
	AUTO_AGGRO,
	MOB_DAMAGE_MOB_FRIENDLY,
	MOB_SELF_DESTRUCT,
	MOB_DAMAGE_MOB,
	MOB_SKILL_DELAY_END,
	NPC_ACTION,
	ITEM_PICKUP,
	DAMAGE_REACTOR,
	TOUCHING_REACTOR,
	REQUIRE_FIELD_OBSTACLE_STATUS,
	TEMP_SKILL,
	MAPLETV,
	SNOWBALL,
	LEFT_KNOCKBACK,
	COCONUT,
	MATCH_TABLE,
	MONSTER_CARNIVAL,
	PARTY_SEARCH_REGISTER,
	PARTY_SEARCH_START,
	CANCEL_INVITE_PARTY_MATCH,
	CHECK_CASH,
	CASHSHOP_OPERATION,
	COUPON_CODE,
	OPEN_ITEMUI,
	CLOSE_ITEMUI,
	USE_ITEMUI,
	MTS_OPERATION,
	USE_MAPLELIFE,
	USE_HAMMER,
	CRC_STATE_RESPONSE,
	CASH_ITEM_GACHAPON_REQUEST,
	THROW_GRENADE;

	private int code = -2;

	@Override
	public void setValue(int code){
		this.code = code;
	}

	@Override
	public final int getValue(){
		return code;
	}

	public static RecvOpcode getOpcodeByOp(int op){
		for(RecvOpcode opcode : values()){
			if(opcode.getValue() == op) return opcode;
		}
		return null;
	}

	public static Properties getDefaultProperties() throws FileNotFoundException, IOException{
		Properties props = new Properties();
		try(FileInputStream fis = new FileInputStream(new File("recvops-" + ServerConstants.VERSION + ".properties"))){
			props.load(fis);
		}
		return props;
	}

	static{
		try{
			ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
		}catch(IOException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}
}
