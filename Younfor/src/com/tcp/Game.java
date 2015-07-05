package com.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import com.ai.Opponent;
import com.ai.PokerLib;
import com.bot.Bot;
import com.bot.Bot81;
import com.bot.Bot81c;
import com.bot.Bot82;
import com.bot.Bot82c;
import com.bot.Bot82d;
import com.bot.Bot83a;
import com.bot.Bot85;
import com.bot.Bot85a;
import com.bot.nebot0;
import com.bot.CleverBot;
import com.game.Card;
import com.game.Player;
import com.game.State;
import com.util.Log;

public class Game {

	State state=new State();
	Bot bot=new CleverBot();
	int lastJetton=0,lastGold=0;
	long totaltime=0;
	public Game(String id,String name)
	{
		state.pid=id;
		state.pname=name;
		//test
		if(name.equals("81"))
			bot=new Bot81();
		else if(name.equals("81c"))
			bot=new Bot81c();
		else if(name.equals("82"))
			bot=new Bot82();
		else if(name.equals("82c"))
			bot=new Bot82c();
		else if(name.equals("82d"))
			bot=new Bot82d();
		else if(name.equals("83a"))
			bot=new Bot83a();
		else if(name.equals("85"))
			bot=new Bot85();
		else if(name.equals("85a"))
		    bot=new Bot85a();			
	  else if(name.equals("nebot0"))
		    bot=new nebot0();
		else
			bot=new CleverBot();
	}
	public void reg(OutputStream out) throws IOException
	{
		String cmd="reg: "+state.pid+" "+state.pname+" need_notify \n";
		out.write(cmd.getBytes());
		out.flush();
		debug("start to reg");
	}
	public void start(BufferedReader in,OutputStream out)
	{
		try{

		String s=null;
		PokerLib.init();
		while((s=in.readLine())!=null)
		{
			if(s.startsWith("game-over"))
			{
				gameover(in,out);
				break;
			}
			else if(s.startsWith("seat"))
			{
				seat(in,out);
			}
			else if(s.startsWith("blind"))
			{
				blind(in,out);
			}else if(s.startsWith("hold"))
			{
				hold(in,out);

			}else if(s.startsWith("inquire")||s.startsWith("notify"))
			{
				inquire(s,in,out);

			}else if(s.startsWith("flop"))
			{
				flop(in,out);
			}
			else if(s.startsWith("turn"))
			{
				turn(in,out);
			}
			else if(s.startsWith("river"))
			{
				river(in,out);
			}
			else if(s.startsWith("showdown"))
			{
				showdown(in,out);
			}
			else if(s.startsWith("pot-win"))
			{
				potwin(in,out);
			}
		}
		}catch(Exception e)
		{
			try {
				debug(e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	//喊话消息
	public void inquire(String s,BufferedReader in,OutputStream out) throws IOException
	{
		boolean requireAction=true;
		if(s.startsWith("inquire"))
		{
			debug("get inquire");
			requireAction=true;
		}else if(s.startsWith("notify"))
		{
			debug("get notify");
			requireAction=false;
		}
		s=in.readLine();
		//清空堆栈
		state.msgstack.clear();
		while((!s.startsWith("/inquire"))&&(!s.startsWith("/notify")))
		{
			/*
			inquire/
			4444 2000 8000 0 fold
			5555 2000 8000 0 fold
			6666 2000 8000 0 fold
			2222 2000 8000 0 fold
			3333 2000 8000 0 fold
			7777 1900 8000 100 blind
			8888 1950 8000 50 blind
			total pot: 150
			/inquire
			 */
			String data[]=s.split(" ");
			if(data[0].startsWith("total"))
			{
				debug("get total"+data[2]);
				state.totalpot=Integer.parseInt(data[2]);
			}else{
				//栈式接收消息
				insertMsgStack(s);
			}
			s=in.readLine();
		}
		//处理消息
		popMsgStack(requireAction);
		//reply action
		/*
		 check | call | raise num | all_in | fold eol
		 */
		if(requireAction)
		{
			int len=0;
			if(state.currentState==State.baseState)
				len=0;
			else if(state.currentState==State.flopState)
				len=3;
			else if(state.currentState==State.turnState)
				len=4;
			else if(state.currentState==State.riverState)
				len=5;
			state.setComm(len);
			long times=System.currentTimeMillis();
			int ans=bot.getBestAction(state, 168);
			debug("timeout: "+(System.currentTimeMillis()-times));

			String action="fold";
			if(ans==State.fold)
				action="fold";
			else if(ans==State.call)
				action="call";
			else if(ans==State.check)
				action="check";
			else if(ans==State.raise)
			{
				if(State.raisebet==0)
					action="check";
				else
					action="raise "+State.raisebet;
			}
			else if(ans==State.all_in)
				action="all_in";
			action+=" \n";
			debug("action: "+action);
			out.write(action.getBytes());
			out.flush();
		}
	}
	//河牌信息
	public void river(BufferedReader in,OutputStream out) throws IOException
	{
		String s;
		debug("get river");
		state.currentState=State.riverState;
		state.newround=true;
		state.round=0;
		state.raisenum=0;
		state.callnum=0;
		state.myraisenum=0;
		s=in.readLine();
		int i=4;
		while(!s.startsWith("/river"))
		{
			/*
			river/ eol
			color point eol
			/river eol
			 */
			String data[]=s.split(" ");
			state.hostcard[i]=new Card(data[0],data[1]);
			debug("river card: "+state.hostcard[i].getColor()+"-"+state.hostcard[i].getPoint());
			s=in.readLine();
			i++;
		}
	}
	//转牌信息
	public void turn(BufferedReader in,OutputStream out) throws IOException
	{
		String s;
		debug("get turn");
		state.currentState=State.turnState;
		state.newround=true;
		state.round=0;
		state.raisenum=0;
		state.callnum=0;
		state.myraisenum=0;
		s=in.readLine();
		int i=3;
		while(!s.startsWith("/turn"))
		{
			/*
			turn/ eol
			color point eol
			/turn eol
			 */
			String data[]=s.split(" ");
			state.hostcard[i]=new Card(data[0],data[1]);
			debug("turn card: "+state.hostcard[i].getColor()+"-"+state.hostcard[i].getPoint());
			s=in.readLine();
			i++;
		}
	}
	//翻牌信息
	public void flop(BufferedReader in,OutputStream out) throws IOException
	{
		debug("get flop");
		String s;
		state.currentState=State.flopState;
		state.newround=true;
		state.round=0;
		state.raisenum=0;
		state.callnum=0;
		state.myraisenum=0;
		s=in.readLine();
		int i=0;
		while(!s.startsWith("/flop"))
		{
			/*
			flop/ eol
			color point eol
			color point eol
			color point eol
			/flop eol
			 */
			String data[]=s.split(" ");
			state.hostcard[i]=new Card(data[0],data[1]);
			debug("flop card: "+state.hostcard[i].getColor()+"-"+state.hostcard[i].getPoint());
			s=in.readLine();
			i++;
		}
	}
	//摊牌消息
	public void showdown(BufferedReader in,OutputStream out) throws IOException
	{
		debug("get showdown");
		String s;
		s=in.readLine();
		if(s.startsWith("common"))
		{
			/*
			showdown/ eol
			common/ eol
			(color point eol)5
			/common eol
			(rank: pid color point color point nut_hand eol)2-8
			/showdown eol
			 	showdown/
				common/
				SPADES 2
				HEARTS 9
				CLUBS J
				DIAMONDS 10
				SPADES K
				/common
				1: 8888 SPADES Q DIAMONDS J STRAIGHT
				2: 6666 HEARTS 7 DIAMONDS 4 HIGH_CARD
				1: 5555 HEARTS 8 DIAMONDS Q STRAIGHT
				/showdown
			 */
			//bys
			while(!s.startsWith("/common"))
				s=in.readLine();
			s=in.readLine();
			while(!s.startsWith("/showdown"))
			{
				debug(s);
				s=in.readLine();
			}

		}
	}
	//手牌消息
	public void hold(BufferedReader in,OutputStream out) throws IOException
	{
		debug("get hold");
		String s;
		state.currentState=State.baseState;
		state.newround=false;
		state.round=0;
		s=in.readLine();
		int i=0;
		while(!s.startsWith("/hold") && (i<2))
		{
			/*
			hold/
			HEARTS Q
			SPADES 10
			/hold
			 */
			String data[]=s.split(" ");
			state.handcard[i]=new Card(data[0],data[1]);
			debug("base card: "+state.handcard[i].getColor()+"-"+state.handcard[i].getPoint());
			i++;
			s=in.readLine();
		}
		state.setHand(state.handcard[0].getValue(), state.handcard[1].getValue());
	}
	//底池消息
	public void potwin(BufferedReader in,OutputStream out) throws IOException
	{
		debug("get pot-win");
		String s;
		s=in.readLine();
		while(!s.startsWith("/pot-win"))
		{
			/*
			pot-win/ eol
			(pid: num eol)0-8
			/pot-win eol
			 */
			String data[]=s.split(" ");
			debug(s);
			String id=data[0].substring(0, data[0].length()-1);
			s=in.readLine();
		}
	}
	//游戏结束消息
	public void gameover(BufferedReader in,OutputStream out) throws IOException
	{
		/*
		 game-over eol
		 */
		in.close();
		out.close();
		debug("game over");
	}
	//处理盲注消息
	public void blind(BufferedReader in,OutputStream out) throws IOException
	{
		debug("start to blind");
		String s;
		s=in.readLine();
		while(!s.startsWith("/blind"))
		{
			/*
			 blind/
			8888: 50
			7777: 100
			/blind
			 * */
			String data[]=s.split(" ");
			data[0]=data[0].substring(0, data[0].length()-1);
			Player temp=findById(data[0]);
			if(temp.getType()==State.smallblind)
			{
				debug(temp.getPid()+" is small blind:"+data[1]);
				state.smallblindbet=Integer.parseInt(data[1]);
			}
			else if (temp.getType()==State.bigblind)
			{
				debug(temp.getPid()+" is big blind:"+data[1]);
				state.bigblindbet=Integer.parseInt(data[1]);
			}
			s=in.readLine();
		}
	}
	//处理座位消息
	public void seat(BufferedReader in,OutputStream out) throws IOException
	{
		/*
		seat/
		button: 1111 2000 8000
		small blind: 8888 2000 8000
		big blind: 7777 2000 8000
		3333 2000 8000
		2222 2000 8000
		6666 2000 8000
		5555 2000 8000
		4444 2000 8000
		/seat
		 **/
		String s;
		State.seatnum++;
		State.scarenum=0;
		state.clear();
		State.handnum++;
		state.seatplayer=0;
		debug("start to seat "+State.handnum);
		s=in.readLine();
		int loc=0;
		//计算位置
		int position=1; //1庄2小盲3大盲4,5,6以次
		while(!s.startsWith("/seat"))
		{
			Player player=null;
			state.seatplayer++;
			//State.newseat=true;
			if(s.startsWith("button"))
			{
				String []data=s.split(" ");
				player=new Player(data[1],Integer.parseInt(data[2]),Integer.parseInt(data[3]),State.button);
				state.players.add(player);
				debug("button "+data[1]+","+data[2]+","+data[3]);
			}
			else if(s.startsWith("small"))
			{
				String []data=s.split(" ");
				player=new Player(data[2],Integer.parseInt(data[3]),Integer.parseInt(data[4]),State.smallblind);
				state.players.add(player);
				debug("small "+data[2]+","+data[3]+","+data[4]);
			}
			else if(s.startsWith("big"))
			{
				String []data=s.split(" ");
				player=new Player(data[2],Integer.parseInt(data[3]),Integer.parseInt(data[4]),State.bigblind);
				state.players.add(player);
				debug("big "+data[2]+","+data[3]+","+data[4]);
			}else
			{
				loc++;
				String []data=s.split(" ");
				player=new Player(data[0],Integer.parseInt(data[1]),Integer.parseInt(data[2]),State.normal);
				state.players.add(player);
				debug("normal  "+data[0]+","+data[1]+","+data[2]);
			}
			//初始化位置
			player.position=position++;
			if(player.position<=3)
				player.position+=state.seatplayer;
			//初始化对手模型
			initOpponent(player.getPid());
			if(player.getPid().equals(state.pid))
			{
				if(state.initGold==0)
				{
					state.initGold=player.getGold();
					state.initJet=player.getJetton();
				}
				int losejetton=player.getJetton()+player.getGold()-lastJetton;
				lastJetton=player.getJetton()+player.getGold();
				if(losejetton<-5000)
					debug("lose5000+ with "+losejetton);
				else if(losejetton<-2000)
					debug("lose2000+ with"+ losejetton);
				else if(losejetton<-1000)
					debug("lose1000+with "+losejetton);
				else if(losejetton<-100)
					debug("lose0:"+losejetton);
				else if(losejetton>0)
					debug("win:"+losejetton);
				if(losejetton<0&&State.xiahu)
					State.losegold+=losejetton;
				else if(State.xiahu)
					State.wingold+=losejetton;
				debug("wien"+State.wingold+", loes"+State.losegold);
				State.xiahu=false;
				if(loc==3||loc==2||loc==1)
					state.myloc=State.EP;
				else if(loc==4||loc==5)
					state.myloc=State.MP;
				if(player.getType()==State.button)
					state.myloc=State.button;
				if(player.getType()==State.bigblind||player.getType()==State.smallblind)
					state.myloc=player.getType();
				state.setInitjetton(player.getJetton());
				state.setJetton(player.getJetton());
			}
			s=in.readLine();
		}
	}
	public Player findById(String id)
	{
		for(int i=0;i<state.players.size();i++)
		{
			if(state.players.get(i).getPid().equals(id))
				return state.players.get(i);
		}
		return null;
	}
	//初始化对手模型
	public void initOpponent(String id)
	{
		if(!state.opponent.containsKey(id))
		{
			state.opponent.put(id, new Opponent());
		}
	}
	//通知消息-消息堆栈
	public void insertMsgStack(String s)
	{
		state.msgstack.push(s);
	}
	//通知消息-取出堆栈
	public void popMsgStack(boolean requireAction) throws IOException
	{
		//新的一轮inquire,只能从小盲开始,未到小盲的时候是我上一轮的.但是上一轮并没记录.
		//从现在到小盲的前一位是上一轮的,如果raise数量和call数量与上一轮不同,才记录,不然notify重复了
		state.round++;
		if(state.newround&&requireAction)
		{
			int curState=state.currentState;
			state.newround=false;
			//去掉重复
			while(!state.msgstack.isEmpty())
			{
				String data[]=state.msgstack.peek().split(" ");
				Player p=findById(data[0]);
				if(p.getType()!=State.smallblind)
				{
					//如果没和上一轮重复,就接收为上一轮的消息
					state.msgstack.pop();
					if(p.getLastaction()==State.raise&&State.getAction(data[4])==State.raise&&p.getBet()==Integer.parseInt(data[3]))
						continue;
					if(p.getLastaction()==State.call&&State.getAction(data[4])==State.call&&p.getBet()==Integer.parseInt(data[3]))
						continue;
					if(state.currentState==State.baseState)
						continue;
					state.currentState--;
					//处理上一轮没处理的消息
					processMsg(data,true);
				}else
				{
					break;
				}
			}
			//恢复正常状态
			state.currentState=curState;
			while(!state.msgstack.isEmpty())
			{
				String data[]=state.msgstack.peek().split(" ");
				processMsg(data,false);
				state.msgstack.pop();
			}
		}else
		{//不是新的一轮,读到加注值相同为止
			while(!state.msgstack.isEmpty())
			{
				String data[]=state.msgstack.peek().split(" ");
				Player p=findById(data[0]);
				if(p.getLastaction()==State.raise&&State.getAction(data[4])==State.raise&&p.getBet()==Integer.parseInt(data[3]))
					break;
				processMsg(data,false);
				state.msgstack.pop();
			}
		}
	}
	//通知消息-处理消息
	public void processMsg(String data[],boolean prevround) throws IOException
	{
		Player p=findById(data[0]);
		p.setJetton(Integer.parseInt(data[1]));
		p.setGold(Integer.parseInt(data[2]));
		p.setBet(Integer.parseInt(data[3]));
		p.setLastaction(State.getAction(data[4]));
		if(p.getLastaction()==State.raise||p.getLastaction()==State.all_in)
		{
			if(!p.getPid().equals(state.pid)&&(!prevround))
			{
				state.raisenum++;
			}
			state.opponent.get(p.getPid()).raisenum[state.currentState-State.baseState]++;
			//5
			state.opponent.get(p.getPid()).raisebet[state.currentState-State.baseState]+=p.getBet()-state.getPrebet();
		}
		if(p.getLastaction()==State.call||p.getLastaction()==State.check)
		{
			if(!p.getPid().equals(state.pid) &&(!prevround))
				state.callnum++;
			state.opponent.get(p.getPid()).callnum[state.currentState-State.baseState]++;
		}
		if(p.getLastaction()==State.fold&&p.prevaction!=State.fold)
			state.opponent.get(p.getPid()).foldnum[state.currentState-State.baseState]++;
		if(p.getPid().equals(state.pid))
		{
			//self
			if(p.getLastaction()==State.raise||p.getLastaction()==State.all_in)
				state.myraisenum++;
			state.setJetton(p.getJetton());
			state.setPrebet(p.getBet());
		}
		//存储前一次动作
		p.prevaction=p.getLastaction();
		debug("get player"+p.getPid()+" "+p.getJetton()+" "+p.getGold()+" "+p.getBet()+" "+p.getLastaction());
	}
	private void debug(String s) throws IOException
	{
		Log.getIns(state.pid).log(s);
	}
}
