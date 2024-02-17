package server.reactors;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import server.reactors.actions.MapleReactorEvent;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * Class to hold many ReactorEvents and check/run them all.
 * 
 * @author Tyler
 */
public class ReactorState{

	private List<MapleReactorEvent> events = new ArrayList<>();
	private boolean repeat;

	public void addEvent(MapleReactorEvent event){
		events.add(event);
	}

	public List<MapleReactorEvent> getEvents(){
		return events;
	}

	public List<MapleReactorEvent> getEventsCloned(){
		List<MapleReactorEvent> ret = new ArrayList<>();
		events.forEach(mre-> ret.add(mre.clone()));
		return ret;
	}

	public boolean canRepeat(){
		return repeat;
	}

	public void setCanRepeat(boolean repeat){
		this.repeat = repeat;
	}

	/**
	 * Run the check function on all current {@link MapleReactorEvent}(s). Return true if any of them
	 * pass their check. This allows reactors with multiple events to succeed
	 * even if one of the checks fails for a specific event.
	 * 
	 * @param client The {@link MapleClient} object to check the event as.
	 * @param reactor The {@link MapleReactor} object to check the event as.
	 * @param type {@link ReactorHitType} used to show where the hit is coming from.
	 * @return {@code true} if any of the checks passed.
	 */
	public boolean checkEvents(MapleClient client, MapleReactor reactor, ReactorHitType type, ReactorHitInfo info){
		return events.stream().anyMatch(event-> event.check(client, reactor, type, info));
	}

	/**
	 * Runs all {@link MapleReactorEvent}(s) in the event list if they pass
	 * their test.
	 * 
	 * @param client The {@link MapleClient} object to run the event as.
	 * @param reactor The {@code MapleReactor} object to run the event as.
	 * @param type {@link ReactorHitType} used to show where the hit is coming from.
	 */
	public void runEvents(MapleClient client, MapleReactor reactor, ReactorHitType type, ReactorHitInfo info){
		events.stream().filter(event-> event.check(client, reactor, type, info)).forEach(event-> event.run(client, reactor, info));
	}

	@Override
	public int hashCode(){
		int hash = 7;
		hash = 97 * hash + events.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj){
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		final ReactorState other = (ReactorState) obj;
		return events.equals(other.getEvents());
	}

	@Override
	public ReactorState clone(){
		ReactorState ret = new ReactorState();
		ret.repeat = repeat;
		getEventsCloned().forEach(mre-> ret.addEvent(mre));
		return ret;
	}

	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeInt(events.size());
		for(MapleReactorEvent event : events){
			mplew.writeMapleAsciiString(event.getType().name());
			event.save(mplew);
		}
		mplew.writeBoolean(repeat);
	}

	public void load(LittleEndianAccessor slea){
		int size = slea.readInt();
		for(int i = 0; i < size; i++){
			ReactorActionType type = ReactorActionType.valueOf(slea.readMapleAsciiString());
			try{
				MapleReactorEvent event = (MapleReactorEvent) type.getClassType().getConstructor(LittleEndianAccessor.class).newInstance(slea);
				events.add(event);
			}catch(Exception e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
		repeat = slea.readBoolean();
	}
}