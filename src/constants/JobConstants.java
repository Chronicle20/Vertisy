package constants;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 6, 2017
 */
public class JobConstants{

	public static int getSkillBookIndex(int job){
		if(is_beginner_job(job)) return 0;
		else if(is_evan_job(job)) return getJobIndex(job);
		// else if(is_dualblade_job(job))return getJobIndex(job);
		else return 1;
	}

	public static int getJobIndex(int job){// get_job_level
		int v1;
		int v2;
		int result;
		if(job % 100 != 0 && job != 2001){
			if(job / 10 == 43) v1 = (job - 430) / 2;
			else v1 = job % 10;
			v2 = v1 + 2;
			if(v2 >= 2 && (v2 <= 4 || v2 <= 10 && is_evan_job(job))) result = v2;
			else result = 0;
		}else{
			if(is_beginner_job(job)) result = 0;
			else result = 1;
		}
		return result;
	}

	public static boolean is_beginner_job(int job){
		return (job % 1000 == 0) || job == 2001;
	}

	public static boolean is_evan_job(int job){
		return job / 100 == 22 || job == 2001;
	}

	public static boolean is_dualblade_job(int job){
		return job / 10 == 43;
	}
}
