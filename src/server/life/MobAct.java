package server.life;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 13, 2017
 *        This shit from eric
 */
public enum MobAct{
	Move(0),
	Stand(1),
	Jump(2),
	Fly(3),
	Rope(4),
	Regen(5),
	Bomb(6),
	Hit1(7),
	Hit2(8),
	HitF(9),
	Die1(10),
	Die2(11),
	DieF(12),
	Attack1(13),
	Attack2(14),
	Attack3(15),
	Attack4(16),
	Attack5(17),
	Attack6(18),
	Attack7(19),
	Attack8(20),
	AttackF(21),
	Skill1(22),
	Skill2(23),
	Skill3(24),
	Skill4(25),
	Skill5(26),
	Skill6(27),
	Skill7(28),
	Skill8(29),
	Skill9(30),
	Skill10(31),
	Skill11(32),
	Skill12(33),
	Skill13(34),
	Skill14(35),
	Skill15(36),
	Skill16(37),
	Enabled(37),
	SkillF(38),
	Chase(39),
	Miss(40),
	Say(41),
	Eye(42),
	No(43);

	private int actid;

	private MobAct(int actid){
		this.actid = actid;
	}

	public int getActId(){
		return actid;
	}
}