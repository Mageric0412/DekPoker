package com.bot;

import java.io.IOException;
import java.util.*;
import com.ai.ProbValue;
import com.bot.Bot;
import com.game.Card;
import com.game.Player;
import com.game.State;
import com.util.Log;

public class Bot82c implements Bot {

	//精度
	java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");  
	State state;
	//手牌
	Card handcard[];
	long time;
	//我
	Player me = null;
	//胜率
	double prob=0;
	//除开我的存活人
	int activeplayer = 0;
	//葫芦娃
	int crazeplayer=0;
	//玩的松且激进的人
	int looseplayer=0;
	//最高跟住
	double hightobet=0,prebet=0;
	//手牌
	double level=0;
	double size=0;
	public boolean isSmallJetton()
	{
		if(state.getInitjetton()<15*state.bigblindbet)
			return true;
		return false;
	}
	public int getBestAction(State state, long time) {

		//初始化
		this.state = state;
		this.time = time;
		initArgs();
		//对手模型
		 initOpponent();
		//翻牌前策略
		if(state.currentState==State.baseState)
			return getPreAction();
		else{
		//翻牌后策略
			return getAction();
		}
	}
	//参数初始化
	public void initArgs()
	{
		this.handcard = state.handcard;
		int hightocall = 0;
		for (Player p : state.players) {
			if (p.getPid().equals(state.pid))
				me = p;
			hightocall = Math.max(p.getBet(), hightocall);
		}
		hightobet=hightocall;
		debug("hightobet:"+hightobet);
		try {
			prebet = state.getPrebet();
		} catch (Exception e) {
			prebet = 0;
		}
		debug("prebet:"+prebet);
		activeplayer=state.getNonFolded()-1;
		debug("activeplayer:"+activeplayer);
		//求手牌范围
		level=ProbValue.getPower(new int[]{handcard[0].getValue(),handcard[1].getValue()});
		debug("level:"+level);
		state.bigblindbet=(int)(state.initJet/50.0);
		//资金
		size=Math.sqrt(Math.min(4.0, state.getInitjetton()/(state.bigblindbet*50.0)));
		debug("size:"+size);
	}
	//对手模型
	public void initOpponent()
	{
		looseplayer=0;
		crazeplayer=0;
		for(Player p:state.players)
		{
			if(p.getPid().equals(me.getPid()))
				continue;
			boolean loose=false,preflopscare=false,postflopscare=false;
			double vpip=state.opponent.get(p.getPid()).getVPIP();
			if(vpip>0.21)
				loose=true;
			else
				loose=false;
			double af=state.opponent.get(p.getPid()).getAF();
			if(af>2.4)
				postflopscare=true;
			else
				postflopscare=false;
			double prf=state.opponent.get(p.getPid()).getPFR();
			if(prf<0.06)
				preflopscare=false;
			else if(prf>0.5*vpip)
				preflopscare=true;
			else
				preflopscare=false;
			//判断葫芦娃
			if(prf>0.9||vpip>0.9)
				crazeplayer++;
			
			String style=p.getPid();
			if(loose)
				style+=" 松";
			else
				style+=" 紧";
			if(preflopscare)
				style+=" 翻牌前凶";
			else
				style+=" 翻牌前弱";
			if(postflopscare)
				style+=" 翻牌后凶";
			else
				style+=" 翻牌后弱";
			if(loose&&(preflopscare)&&p.isAlive())
				looseplayer++;
			debug(p.getPid()+style+" VPIP("+df.format(vpip)+")  AF("+df.format(af)+")  PRF("+df.format(prf)+")");
		}
		debug("looseplayer:"+looseplayer);
		debug("crazeplayer:"+crazeplayer);
	}

	//翻牌前策略
	public int getPreAction()
	{
		debug("round:"+state.round);
		//6
		//新一轮
		if(state.round>1&&hightobet-prebet<=(size+1)*3*state.bigblindbet&&level>=5)
			return State.call;
		if(level>=10&&state.round>1)
			return State.call;
		
		//关煞位置
		if(state.seatplayer==me.position)
		{
			debug("关煞位");
			if(isFoldAll())
			{
				//3
				State.raisebet=(int)size*state.bigblindbet;
				return State.raise;
			}
			if(level>5&&(!isRaise1Call0()))
				return State.call;
			//3
			else if(level>=7&&hightobet-prebet<=size*2*state.bigblindbet)
				return State.call;
		}
		//按钮位置
		else if(state.myloc==State.button)
		{
			debug("按钮位置");
			if(level>=8)
			{
				//1
				if(state.myraisenum>size)
					return State.call;
				State.raisebet=2*state.bigblindbet;
				return State.raise;
			}
			if(level>6&&hightobet-prebet<=size*4*state.bigblindbet)
			{
				//1
				if(state.myraisenum>size)
					return State.call;
				State.raisebet=(int)(size*4*state.bigblindbet);
				return State.raise;
			}
			if(level>5&&(isFoldAll()||isCall1()||isCall2()))
			{
				State.raisebet=state.bigblindbet;
				return State.raise;
			}
			if((isCall1()||isFoldAll())&&(!isSmallJetton())&&level>=looseplayer+2)
			{
				//8
				if(crazeplayer>0)
				{
					if(level>=12)
						return State.call;
					return State.fold;
				}
				State.raisebet=(int)(8*state.bigblindbet);
				return State.raise;
			}
		}
		//大盲位置
		else if(state.myloc==State.bigblind)
		{
			debug("大盲");
			if(level>=8)
			{
				//1
				if(state.myraisenum>size)
					return State.call;
				State.raisebet=2*state.bigblindbet;
				return State.raise;
			}
			if(level>6&&hightobet-prebet<=size*4*state.bigblindbet)
			{
				//1
				if(state.myraisenum>size)
					return State.call;
				State.raisebet=2*state.bigblindbet;
				return State.raise;
			}
			if(level>5&&(isFoldAll()||isCall1()||isCall2()))
			{
				State.raisebet=4*state.bigblindbet;
				return State.raise;
			}
			if(isCall2()||(isCall1()||isFoldAll())||(isRaise1Call0()&&hightobet-prebet<=size*4*state.bigblindbet))
			{
				if(level<=looseplayer)
					return State.fold;
				//8
				if(crazeplayer>0)
				{
					if(level>=12)
						return State.call;
					return State.fold;
				}
				//1
				if(state.myraisenum>size)
					return State.call;
				//6
				if(isRaise1Call0()&&isSmallJetton())
					return State.fold;
				State.raisebet=(int)(10*state.bigblindbet);
				return State.raise;
			}
		}
		//小盲位置
		else if(state.myloc==State.smallblind)
		{
			debug("小盲");
			if(level>=8)
			{
				//1
				if(state.myraisenum>size)
					return State.call;
				State.raisebet=2*state.bigblindbet;
				return State.raise;
			}
			if(level>6&&hightobet-prebet<=4*state.bigblindbet)
			{
				State.raisebet=2*state.bigblindbet;
				return State.raise;
			}
			if(level>5&&(isFoldAll()||isCall1()||isCall2()))
			{
				State.raisebet=4*state.bigblindbet;
				return State.raise;
			}
			if(isCall2()||isCall1()||isFoldAll()||(isRaise1Call0()&&hightobet-prebet<=size*4*state.bigblindbet))
			{
				if(level<looseplayer+1)
					return State.fold;
				//8
				if(crazeplayer>0)
				{
					if(level>=12)
						return State.call;
					return State.fold;
				}
				//1
				if(state.myraisenum>size)
					return State.call;
				State.raisebet=(int)(8*state.bigblindbet);
				//6
				if((isRaise1Call0()||isRaise1Call1())&&isSmallJetton())
					return State.fold;
				else if(isSmallJetton())
					State.raisebet=(int)(size*4*state.bigblindbet);
				return State.raise;
			}
		}
		//枪口位置
		if(state.myloc==State.EP)
		{
			debug("枪口");
			if((isCall1()||isCall2()||isFoldAll())&&level>=7)
			{
				State.raisebet=state.bigblindbet;
				return State.raise;
			}
			if(level>=8)
			{
				if(hightobet-prebet<=state.bigblindbet)
				{
					State.raisebet=state.bigblindbet;
					return State.raise;
				}else
					return State.call;
			}
		}
		//中间位置
		if(state.myloc==State.MP)
		{
			debug("中间");
			if((isCall1()||isCall2()||isFoldAll()||isRaise1Call1())&&level>=5)
			{
				State.raisebet=state.bigblindbet;
				return State.raise;
			}
			if(level>=6)
			{
				//1
				//3
				if(state.myraisenum>size)
					return State.call;
				//3
				if(hightobet-prebet<=size*state.bigblindbet)
				{
					State.raisebet=(int)(size*state.bigblindbet);
					return State.raise;
				}
				//State.raisebet=state.bigblindbet;
				//return State.raise;
			}
				
		}
		
		return State.fold;
	}

	//翻牌后策略
	public int getAction() {
		try {
				if(raise())
				{
					debug("raise");
					if(state.currentState==State.flopState||state.currentState==State.turnState)
					{
						if(hightobet-prebet<=size*4*state.bigblindbet&&prob<0.6)
						{
							debug("raise:"+State.raisebet);
							State.raisebet=(int)(size*5*state.bigblindbet);
							return State.raise;
						}//3
						else if(prob>0.6&&hightobet-prebet<=size*4*state.bigblindbet&&state.myraisenum<2)//5
						{
							//3
							State.raisebet=(int)(size*2*state.bigblindbet);
							return State.raise;
						}else 
						{
							return State.call;
						}
					}else{
						if(hightobet-prebet<=2*state.bigblindbet)
						{
							State.raisebet=2*state.bigblindbet;
							return State.raise;
						}else
							return State.call;
					}
				}else
				{
					//吓唬
					debug("吓唬");
					//8
					if(crazeplayer==0)
					{
						if((state.currentState==State.flopState||state.currentState==State.turnState)&&hightobet-prebet<=size*3*state.bigblindbet)
						{
							State.raisebet=(int)(size*5*state.bigblindbet);
							return State.raise;
						}
						if(hightobet-prebet<=state.bigblindbet &&state.myraisenum<2)//7
						{
							State.raisebet=(int)(state.bigblindbet);
							return State.raise;
						}
					}
					if(ev())
					{
						debug("call");
						return State.call;
						
					}else
					{
						debug("fold");
						return State.fold;
						
					}
				}
		} catch (Exception e) {
			debug("exception fold");
			e.printStackTrace();
			return State.call;
		}
	}
    //判断期望
	public boolean ev()
	{
		double hightocall=hightobet;
		if (state.getInitjetton() < hightocall)
			hightocall = state.getInitjetton() - 1;
		double prob1 = prob
				* Math.log(((double) state.getInitjetton() + ((double) activeplayer * hightocall))
						/ state.getInitjetton());
		double prob2 = (1 - prob)
				* Math.log((state.getInitjetton() - hightocall)
						/ ((double) state.getInitjetton()));
		double prob3 = Math
				.log(((double) state.getInitjetton() - prebet)
						/ state.getInitjetton());
		debug("EV :" + (prob1 + prob2) + "   " + prob3);
		if(prob1+prob2>prob3)
			return true;
		else
			return false;
	}
    //判断是否够加注
	public boolean raise() throws IOException
    {
		int[] hand = state.getHand();
		int[] comm = state.getComm();
		ProbValue  probvalue= new ProbValue(hand, activeplayer, comm);
		//得到我的胜率
		prob=probvalue.getProb();
		double add = 0.064;
		if (comm.length >= 5)
			add = 0.001;
		else if (comm.length >= 4)
			add = 0.012;
		else if (comm.length >= 3)
			add = 0.025;
		double deficit = 1.0 - prob;
		prob += deficit * add;
		double hightocall = hightobet;
		int activeIncludingSelf = activeplayer + 1;
		int tocall = (int) ((state.getInitjetton() * (prob
				* activeIncludingSelf - 1))
				/ activeplayer * 0.8);
		int maxbet=1;
		maxbet= (int) (state.getInitjetton() / 1.5);
		prebet = 0;
		try {
			prebet = state.getPrebet();
		} catch (Exception e) {
			prebet = 0;
		}
		if (tocall > maxbet)
			tocall = maxbet;
		Log.getIns(state.pid).log("hightocall: "+hightocall+" "+
				" tocall " + tocall + " maxbet " + maxbet + " prebet "
						+ prebet + " prob " + prob + "\n");
		if (tocall < hightocall) 
			return false;
		else 
			return true;
    }
    //一些常用函数
	public boolean isFoldAll()
	{
		if(state.callnum==0&&state.raisenum==0)
		{
			debug("is fold all");
			return true;
		}
		return false;
	}
	public boolean isCall1()
	{
		if(state.callnum==1&&state.raisenum==0)
		{
			debug("is call 1");
			return true;
		}
		return false;
	}
	public boolean isCall2()
	{
		if(state.raisenum==0&&state.callnum>1)
		{
			debug("is call 2");
			return true;
		}
		return false;
	}
	public boolean isRaise1Call0()
	{
		if(state.raisenum>=1&&state.callnum==0)
		{
			debug("is raise 1, call 0");
			return true;
		}
		return false;
	}
	public boolean isRaise1Call1()
	{
		if(state.raisenum>=1&&state.callnum>0)
		{
			debug("is raise 1,call 1");
			return true;
		}
		return false;
	}
	public boolean isPair()
	{
		if(handcard[0].getRank()==handcard[1].getRank())
			return true;
		return false;
	}
	public void debug(String s) {
		try {
			Log.getIns(state.pid).log(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}