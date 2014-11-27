package chylex.hee.entity.mob.ai;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import chylex.hee.entity.projectile.EntityProjectileCorruptedEnergy;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.MathUtil;

public class EntityAIRangedEnergyAttack extends EntityAIBase{
	private final EntityLiving entity;
	private final double moveSpeed;
	
	private EntityLivingBase target;
	private byte attackCooldown, attackShotTimer, attackShots;
	
	public EntityAIRangedEnergyAttack(EntityLiving entity, double moveSpeed){
		this.entity = entity;
		this.moveSpeed = moveSpeed;
	}
	
	@Override
	public boolean shouldExecute(){
		EntityLivingBase target = entity.getAttackTarget();
		if (target == null)return false;
		
		this.target = target;
		return true;
	}
	
	@Override
	public boolean continueExecuting(){
		return shouldExecute() || entity.getNavigator().noPath();
	}
	
	@Override
	public void resetTask(){
		target = null;
		attackCooldown = attackShotTimer = attackShots = 0;
	}
	
	@Override
	public void updateTask(){
		double dist = entity.getDistanceSqToEntity(target);
		
		if (dist > 400D){
			entity.getNavigator().clearPathEntity();
			return;
		}
		
		if (dist > 64D)entity.getNavigator().tryMoveToEntityLiving(target,moveSpeed);
		
		entity.getLookHelper().setLookPositionWithEntity(target,25F,25F);
		
		if (entity.getEntitySenses().canSee(target)){
			if (attackShots == 0){
				if (++attackShotTimer > (ModCommonProxy.opMobs ? 4 : 5)){
					shootProjectile();
					attackShotTimer = 0;
					if (++attackShots > 4+entity.worldObj.rand.nextInt(4))attackCooldown = attackShots = 0;
				}
			}
			else if (++attackCooldown > 80-entity.worldObj.difficultySetting.getDifficultyId()*5-(ModCommonProxy.opMobs ? 15 : 0))attackShots = 1;
		}
		else attackCooldown = attackShotTimer = attackShots = 0;
	}
	
	private void shootProjectile(){
		double x, y, z; // TODO shoot from the wand (check)
		x = entity.posX+MathHelper.cos(MathUtil.toRad(entity.rotationYaw+20F))*0.5F;
		y = entity.posY+2.4F;
		z = entity.posZ+MathHelper.sin(MathUtil.toRad(entity.rotationYaw+20F))*0.5F;
		entity.worldObj.spawnEntityInWorld(new EntityProjectileCorruptedEnergy(entity.worldObj,entity,x,y,z,target));
	}
}