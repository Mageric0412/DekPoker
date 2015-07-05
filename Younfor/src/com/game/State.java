package com.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import com.ai.Opponent;

public class State {
	//一些常量
	public final static int button=10,smallblind=11,bigblind=12,normal=13;
	public final static int blind=30,check=31,call=32,raise=33,all_in=34,fold=35,no=36;
	public final static int baseState=40,flopState=41,turnState=42,riverState=43;
	public final static int LP=50,MP=51,EP=52;
	public static boolean isRegistered=false;
	//每局都重新初始化的参数
	public static double aveenterpot=0,seatnum=0,enterprob=0;
	public static boolean newseat=false;
	public static boolean xiahu=false;
	public static int scarenum=0;
	public static double wingold=0,losegold=0;
	public static int handnum=0;
	public String pid,pname;//
	public boolean isFold=false;
	public int bigblindbet,smallblindbet,totalpot;//
	public Card handcard[]=new Card[2];//
	public Card hostcard[]=new Card[5];//
	public int currentState=-1;//
	public int initGold=0,initJet=0;
	private int[] hand;//
	private int[] comm;//
	private int jetton,bet,prebet,initjetton;//
	public static int raisebet=0; //show action raise
	public int myraisenum=0;
	public int raisenum=0,callnum=0;
	public int seatplayer=0;
	public int myloc=0;
	public List<Player>  players=new ArrayList<Player>();
	//消息栈
	public Stack<String> msgstack=new Stack<String>();
	//表示新的一轮
	public boolean newround=false;
	public int round=0;
	//对手模型
	public Map<String,Opponent> opponent=new HashMap<String, Opponent>();
	//每局都清理
	public void clear()
	{
		bigblindbet=0;
		seatplayer=0;
		smallblindbet=0;
		handcard=new Card[2];
		hostcard=new Card[5];
		totalpot=0;
		hand=null;
		comm=null;
		isFold=false;
		raisenum=0;
		callnum=0;
		myraisenum=0;
		myloc=State.LP;
		jetton=0;
		bet=0;
		prebet=0;
		raisebet=0;
		//每局都清理
		players.clear();
	}
	
	public int getInitjetton() {
		return initjetton;
	}

	public void setInitjetton(int initjetton) {
		this.initjetton = initjetton;
	}

	public int getPrebet() {
		return prebet;
	}

	public void setPrebet(int prebet) {
		this.prebet = prebet;
	}

	public int getToCall() {
		return bet;
	}
	public void setToCall(int bet) {
		this.bet = bet;
	}
	public int getJetton()
	{
		return jetton;
	}
	public void setJetton(int n)
	{
		this.jetton=n;
	}
	//存活人数包括自己
	public int getNonFolded()
	{
		int i=0;
		for(Player p:players)
		{
			if(p.isAlive())
				i++;
		}
		return i;
	}
	public int[] getHand()
	{
		return hand;
	}
	//手牌编码
	public void setHand(int h1,int h2)
	{
		hand=new int[2];
		hand[0]=h1;
		hand[1]=h2;
	}
	public int[] getComm()
	{
		return comm;
	}
	//公牌编码
	public void setComm(int n) throws IOException
	{
		comm=new int[n];
		for(int i=0;i<n;i++)
			comm[i]=hostcard[i].getValue();
	}
	public static int getAction(String s)
	{
		if(s.equals("blind"))
			return blind;
		else if(s.equals("check"))
			return check;
		else if (s.equals("call"))
			return call;
		else if (s.equals("raise"))
			return raise;
		else if (s.equals("all_in"))
			return all_in;
		else if (s.equals("fold"))
			return fold;
		else
			return -1;
	}
}