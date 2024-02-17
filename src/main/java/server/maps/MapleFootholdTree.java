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
package server.maps;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @author Matze
 */
public class MapleFootholdTree{

	private MapleFootholdTree nw = null;
	private MapleFootholdTree ne = null;
	private MapleFootholdTree sw = null;
	private MapleFootholdTree se = null;
	private List<MapleFoothold> footholds = new LinkedList<MapleFoothold>();
	private Point p1;
	private Point p2;
	private Point center;
	private int depth = 0;
	private static int maxDepth = 8;
	private int maxDropX;
	private int minDropX;

	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeBoolean(nw != null);
		if(nw != null) nw.save(mplew);
		mplew.writeBoolean(ne != null);
		if(ne != null) ne.save(mplew);
		mplew.writeBoolean(sw != null);
		if(sw != null) sw.save(mplew);
		mplew.writeBoolean(se != null);
		if(se != null) se.save(mplew);
		mplew.writeInt(footholds.size());
		for(MapleFoothold fh : footholds){
			fh.save(mplew);
		}
		mplew.writePos(p1);
		mplew.writePos(p2);
		mplew.writePos(center);
		mplew.writeInt(depth);
		mplew.writeInt(minDropX);
		mplew.writeInt(maxDropX);
	}

	public void load(LittleEndianAccessor slea){
		if(slea.readBoolean()){
			nw = new MapleFootholdTree();
			nw.load(slea);
		}
		if(slea.readBoolean()){
			ne = new MapleFootholdTree();
			ne.load(slea);
		}
		if(slea.readBoolean()){
			sw = new MapleFootholdTree();
			sw.load(slea);
		}
		if(slea.readBoolean()){
			se = new MapleFootholdTree();
			se.load(slea);
		}
		int size = slea.readInt();
		for(int i = 0; i < size; i++){
			MapleFoothold fh = new MapleFoothold();
			fh.load(slea);
			footholds.add(fh);
		}
		p1 = slea.readPos();
		p2 = slea.readPos();
		center = slea.readPos();
		depth = slea.readInt();
		minDropX = slea.readInt();
		maxDropX = slea.readInt();
	}

	public MapleFootholdTree(){
		super();
	}

	public MapleFootholdTree(Point p1, Point p2){
		this.p1 = p1;
		this.p2 = p2;
		center = new Point((p2.x - p1.x) / 2, (p2.y - p1.y) / 2);
	}

	public MapleFootholdTree(Point p1, Point p2, int depth){
		this.p1 = p1;
		this.p2 = p2;
		this.depth = depth;
		center = new Point((p2.x - p1.x) / 2, (p2.y - p1.y) / 2);
	}

	public MapleFoothold getFootholdByID(int id){
		for(MapleFoothold fh : footholds){
			if(fh.getId() == id) return fh;
		}
		return null;
	}

	public void insert(MapleFoothold f){
		if(depth == 0){
			if(f.getX1() > maxDropX){
				maxDropX = f.getX1();
			}
			if(f.getX1() < minDropX){
				minDropX = f.getX1();
			}
			if(f.getX2() > maxDropX){
				maxDropX = f.getX2();
			}
			if(f.getX2() < minDropX){
				minDropX = f.getX2();
			}
		}
		if(depth == maxDepth || (f.getX1() >= p1.x && f.getX2() <= p2.x && f.getY1() >= p1.y && f.getY2() <= p2.y)){
			footholds.add(f);
		}else{
			if(nw == null){
				nw = new MapleFootholdTree(p1, center, depth + 1);
				ne = new MapleFootholdTree(new Point(center.x, p1.y), new Point(p2.x, center.y), depth + 1);
				sw = new MapleFootholdTree(new Point(p1.x, center.y), new Point(center.x, p2.y), depth + 1);
				se = new MapleFootholdTree(center, p2, depth + 1);
			}
			if(f.getX2() <= center.x && f.getY2() <= center.y){
				nw.insert(f);
			}else if(f.getX1() > center.x && f.getY2() <= center.y){
				ne.insert(f);
			}else if(f.getX2() <= center.x && f.getY1() > center.y){
				sw.insert(f);
			}else{
				se.insert(f);
			}
		}
	}

	public final List<MapleFoothold> getAllRelevants(){
		return getAllRelevants(new LinkedList<MapleFoothold>());
	}

	private List<MapleFoothold> getAllRelevants(final List<MapleFoothold> list){
		for(MapleFoothold fh : footholds){
			if(!list.contains(fh)) list.add(fh);
		}
		if(nw != null){
			nw.getAllRelevants(list);
			ne.getAllRelevants(list);
			sw.getAllRelevants(list);
			se.getAllRelevants(list);
		}
		return list;
	}

	public List<MapleFoothold> getRelevants(Point p){
		return getRelevants(p, new LinkedList<MapleFoothold>());
	}

	public List<MapleFoothold> getRelevants(Point p, List<MapleFoothold> list){
		list.addAll(footholds);
		if(nw != null){
			if(p.x <= center.x && p.y <= center.y){
				nw.getRelevants(p, list);
			}else if(p.x > center.x && p.y <= center.y){
				ne.getRelevants(p, list);
			}else if(p.x <= center.x && p.y > center.y){
				sw.getRelevants(p, list);
			}else{
				se.getRelevants(p, list);
			}
		}
		return list;
	}

	private MapleFoothold findWallR(Point p1, Point p2){
		MapleFoothold ret;
		for(MapleFoothold f : footholds){
			if(f.isWall() && f.getX1() >= p1.x && f.getX1() <= p2.x && f.getY1() >= p1.y && f.getY2() <= p1.y) return f;
		}
		if(nw != null){
			if(p1.x <= center.x && p1.y <= center.y){
				ret = nw.findWallR(p1, p2);
				if(ret != null) return ret;
			}
			if((p1.x > center.x || p2.x > center.x) && p1.y <= center.y){
				ret = ne.findWallR(p1, p2);
				if(ret != null) return ret;
			}
			if(p1.x <= center.x && p1.y > center.y){
				ret = sw.findWallR(p1, p2);
				if(ret != null) return ret;
			}
			if((p1.x > center.x || p2.x > center.x) && p1.y > center.y){
				ret = se.findWallR(p1, p2);
				if(ret != null) return ret;
			}
		}
		return null;
	}

	public MapleFoothold findWall(Point p1, Point p2){
		if(p1.y != p2.y){ throw new IllegalArgumentException(); }
		return findWallR(p1, p2);
	}

	public MapleFoothold findBelow(Point p){
		List<MapleFoothold> relevants = getRelevants(p);
		List<MapleFoothold> xMatches = new LinkedList<MapleFoothold>();
		for(MapleFoothold fh : relevants){
			if(fh.getX1() <= p.x && fh.getX2() >= p.x){
				xMatches.add(fh);
			}
		}
		Collections.sort(xMatches);
		for(MapleFoothold fh : xMatches){
			if(!fh.isWall() && fh.getY1() != fh.getY2()){
				int calcY;
				double s1 = Math.abs(fh.getY2() - fh.getY1());
				double s2 = Math.abs(fh.getX2() - fh.getX1());
				double s4 = Math.abs(p.x - fh.getX1());
				double alpha = Math.atan(s2 / s1);
				double beta = Math.atan(s1 / s2);
				double s5 = Math.cos(alpha) * (s4 / Math.cos(beta));
				if(fh.getY2() < fh.getY1()){
					calcY = fh.getY1() - (int) s5;
				}else{
					calcY = fh.getY1() + (int) s5;
				}
				if(calcY >= p.y){
					relevants = null;
					xMatches = null;
					return fh;
				}
			}else if(!fh.isWall()){
				if(fh.getY1() >= p.y){
					relevants = null;
					xMatches = null;
					return fh;
				}
			}
		}
		relevants = null;
		xMatches = null;
		return null;
	}

	public Point getPoint1(){
		return p1;
	}

	public Point getPoint2(){
		return p2;
	}

	public int getX1(){
		return p1.x;
	}

	public int getX2(){
		return p2.x;
	}

	public int getY1(){
		return p1.y;
	}

	public int getY2(){
		return p2.y;
	}

	public int getMaxDropX(){
		return maxDropX;
	}

	public int getMinDropX(){
		return minDropX;
	}
}
