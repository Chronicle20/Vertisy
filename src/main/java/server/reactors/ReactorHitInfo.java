package server.reactors;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 5, 2017
 */
public class ReactorHitInfo{

	public int skillid;
	public int bMoveAction;// 0 == right, 1 == left
	public int m_pfh;// 1 == onground, 0 == jumping

	@Override
	public String toString(){
		return "ReactorHitInfo [skillid=" + skillid + ", bMoveAction=" + bMoveAction + ", m_pfh=" + m_pfh + "]";
	}
}
