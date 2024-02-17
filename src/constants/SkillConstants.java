package constants;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 15, 2016
 */
public class SkillConstants{

	public static boolean isEliteExempted(int skillid){
		switch (skillid){
			case 2022631:// Rose Scent
			case 2022632:// Freesia Scent
			case 2022633:// Lavender Scent
				return true;
		}
		return false;
	}

	public static boolean isLegendarySpirit(int skillID){
		return skillID % 10000000 == 1003;
	}
}
