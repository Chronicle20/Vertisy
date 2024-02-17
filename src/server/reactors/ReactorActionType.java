package server.reactors;

import java.util.HashMap;
import java.util.Map;

import server.reactors.actions.*;

/**
 * @author Tyler
 */
public enum ReactorActionType{
	HIT(0, HitAction.class),
	HITLEFT(1, HitLeftAction.class),
	HITRIGHT(2, HitRightAction.class),
	HITJUMP_LEFT(3, HitJumpLeftAction.class),
	HITJUMP_RIGHT(4, HitJumpRightAction.class),
	USE_SKILL(5, UseSkillAction.class),
	ITEM(100, ItemAction.class),
	TIME_OUT(101, TimeOutAction.class),
	BREAK(999, BreakAction.class);

	/**
	 * public class ReactorEventType {
	 * public static final int
	 * Hit = 0,
	 * HitLeft = 1,
	 * HitRight = 2,
	 * HitJump_Left = 3,
	 * HitJump_Right = 4,
	 * HitSkill_Check = 5,
	 * 6 and 7 might be a kms or something specific thing?
	 * Gather = 8,
	 * Click_Check = 9,
	 * Mob_Check = 10,
	 * CharacterAct = 11,
	 * RemoveReactor = 12,
	 * HitJump = 13,
	 * FindItem_Update = 100,
	 * Timeout_Reset = 101,
	 * Key_Check1 = 200,
	 * Key_Check2 = 201;
	 * }
	 */
	final int type;
	final Class<?> classType;
	private final static Map<Integer, ReactorActionType> map = new HashMap<>();

	private ReactorActionType(int type, Class<?> classType){
		this.type = type;
		this.classType = classType;
	}

	static{
		for(ReactorActionType type : ReactorActionType.values()){
			map.put(type.type, type);
		}
	}

	public static ReactorActionType valueOf(int type){
		return map.get(type);
	}

	public Class<?> getClassType(){
		return classType;
	}
}