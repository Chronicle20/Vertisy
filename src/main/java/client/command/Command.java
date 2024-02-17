package client.command;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import client.PlayerGMRank;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 18, 2016
 */
public abstract class Command{

	private final String name;
	private String label;
	private List<String> aliases;
	protected String description = "";
	protected String usageMessage = "";
	private int permissionLevel;
	private String aliasUsed = "";
	private String alia;

	public Command(String name){
		this(name, "", "/" + name, "");
	}

	public Command(String name, String description, String usageMessage, String aliases){
		this.name = name;
		this.label = name;
		this.description = description;
		this.usageMessage = usageMessage;
		List<String> alias = new ArrayList<String>();
		if(aliases != null && aliases.length() > 0){
			String[] ali = aliases.split(", ");
			for(String s : ali)
				alias.add(s);
		}
		this.alia = aliases;
		this.aliases = alias;
		CommandHandler.commands.add(this);
	}

	public abstract boolean execute(MapleClient c, String commandLabel, String[] args);

	public void setGMLevel(int level){
		permissionLevel = level;
	}

	public void setGMLevel(PlayerGMRank level){
		permissionLevel = level.getLevel();
	}

	public int getGMLevel(){
		return permissionLevel;
	}

	public void setUsuage(String usa){
		usageMessage = usa;
	}

	public String getUsage(){
		return usageMessage;
	}

	public List<String> getAliases(){
		return aliases;
	}

	public String getLabel(){
		return label;
	}

	public String getName(){
		return name;
	}

	public boolean isAlias(String s){
		if(getAliases().size() > 0){
			for(String alias : getAliases()){
				if(alias.equalsIgnoreCase(s)) return true;
			}
		}
		return false;
	}

	public void usedAlias(String alias){
		aliasUsed = alias;
	}

	public String getAliasUsed(){
		return aliasUsed;
	}

	public boolean aliasUsed(){
		return aliasUsed.length() > 0;
	}

	public String getDescription(){
		return description;
	}

	public String getAlia(){
		return alia;
	}
}
