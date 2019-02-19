/*
 * This file is part of the Aion-Emu project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.model.instance.playerreward;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceBuff;

/**
 * @author Ranastic (Encom)
 */
public class LandMarkPlayerReward extends InstancePlayerReward
{
	private int timeBonus;
	private long logoutTime;
	private final float timeBonusModifier;
	private final Race race;
	private int rewardAp;
	private int rewardGp;
	private int rewardExp;
	private int bonusAp;
	private int bonusGp;
	private int bonusExp;
	private int landMarkBox;
	private int bloodMark;
	private int bonusReward;
	private int bonusReward2;
	private float rewardCount;
	private int AdditionalReward;
	private float AdditionalRewardCount;
	private final InstanceBuff boostMorale;
	
	public LandMarkPlayerReward(Integer object, int timeBonus, byte buffId, Race race)
	{
		super(object);
		this.timeBonus = timeBonus;
		timeBonusModifier = ((float) this.timeBonus / (float) 660000);
		this.race = race;
		boostMorale = new InstanceBuff(buffId);
	}
	
	public float getParticipation()
	{
		return (float) getTimeBonus() / timeBonus;
	}
	
	public int getScorePoints()
	{
		return timeBonus + getPoints();
	}
	
	public int getTimeBonus()
	{
		return timeBonus > 0 ? timeBonus : 0;
	}
	
	public void updateLogOutTime()
	{
		logoutTime = System.currentTimeMillis();
	}
	
	public void updateBonusTime()
	{
		final int offlineTime = (int) (System.currentTimeMillis() - logoutTime);
		timeBonus -= offlineTime * timeBonusModifier;
	}
	
	public Race getRace()
	{
		return race;
	}
	
	public int getLandMarkBox()
	{
		return landMarkBox;
	}
	
	public int getBloodMark()
	{
		return bloodMark;
	}
	
	public int getBonusReward()
	{
		return bonusReward;
	}
	
	public int getRewardCount()
	{
		return (int) rewardCount;
	}
	
	public void setLandMarkBox(int reward)
	{
		landMarkBox = reward;
	}
	
	public void setBloodMark(int reward)
	{
		bloodMark = reward;
	}
	
	public void setBonusReward(int reward)
	{
		bonusReward = reward;
	}
	
	public void setRewardCount(float rewardCount)
	{
		this.rewardCount = rewardCount;
	}
	
	// Ap
	public int getRewardAp()
	{
		return rewardAp;
	}
	
	public void setRewardAp(int rewardAp)
	{
		this.rewardAp = rewardAp;
	}
	
	public int getBonusAp()
	{
		return bonusAp;
	}
	
	public void setBonusAp(int bonusAp)
	{
		this.bonusAp = bonusAp;
	}
	
	// Gp
	public int getRewardGp()
	{
		return rewardGp;
	}
	
	public void setRewardGp(int rewardGp)
	{
		this.rewardGp = rewardGp;
	}
	
	public int getBonusGp()
	{
		return bonusGp;
	}
	
	public void setBonusGp(int bonusGp)
	{
		this.bonusGp = bonusGp;
	}
	
	// Exp
	public int getRewardExp()
	{
		return rewardExp;
	}
	
	public void setRewardExp(int rewardExp)
	{
		this.rewardExp = rewardExp;
	}
	
	public int getBonusExp()
	{
		return bonusExp;
	}
	
	public void setBonusExp(int bonusExp)
	{
		this.bonusExp = bonusExp;
	}
	
	public int getBonusReward2()
	{
		return bonusReward2;
	}
	
	public void setBonusReward2(int bonusReward2)
	{
		this.bonusReward2 = bonusReward2;
	}
	
	public int getAdditionalReward()
	{
		return AdditionalReward;
	}
	
	public void setAdditionalReward(int additionalReward)
	{
		AdditionalReward = additionalReward;
	}
	
	public int getAdditionalRewardCount()
	{
		return (int) AdditionalRewardCount;
	}
	
	public void setAdditionalRewardCount(float rewardCount)
	{
		AdditionalRewardCount = rewardCount;
	}
	
	public boolean hasBoostMorale()
	{
		return boostMorale.hasInstanceBuff();
	}
	
	public void applyBoostMoraleEffect(Player player)
	{
		boostMorale.applyEffect(player, 20000);
	}
	
	public void endBoostMoraleEffect(Player player)
	{
		boostMorale.endEffect(player);
	}
	
	public int getRemaningTime()
	{
		final int time = boostMorale.getRemaningTime();
		if ((time >= 0) && (time < 20))
		{
			return 20 - time;
		}
		return 0;
	}
}