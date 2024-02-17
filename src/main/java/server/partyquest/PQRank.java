package server.partyquest;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 18, 2016
 */
public enum PQRank{
	S,
	A,
	B,
	C,
	D,
	F,
	z; // z = undefined

	public static PQRank getPQRank(String rank){
		rank = rank.toUpperCase();
		for(PQRank pqRank : PQRank.values()){
			if(pqRank.name().equals(rank)){ return pqRank; }
		}
		return z;
	}
}