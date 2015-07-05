package com.game;

public class Player {
	public String pid;
	public int jetton;
	public int gold;
	public int bet;
	public int type;  //button smallblind bigblind
	public int lastaction,prevaction=State.call;
	public boolean win=false;
	public int position=0;
	boolean isAlive=true;
	
	
	public int getBet() {
		return bet;
	}
	public void setBet(int bet) {
		this.bet = bet;
	}
	public int getLastaction() {
		return lastaction;
	}
	public void setLastaction(int lastaction) {
		this.lastaction = lastaction;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}

	public int getJetton() {
		return jetton;
	}
	public void setJetton(int jetton) {
		this.jetton = jetton;
	}
	public int getGold() {
		return gold;
	}
	public void setGold(int gold) {
		this.gold = gold;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public Player(String pid, int jetton, int gold, int type) {
		super();
		this.pid = pid;
		this.jetton = jetton;
		this.gold = gold;
		this.type = type;
	}

	
	public boolean isAlive() {
		if(lastaction!=State.fold)
			return true;
		else
			return false;
	}
	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
}
