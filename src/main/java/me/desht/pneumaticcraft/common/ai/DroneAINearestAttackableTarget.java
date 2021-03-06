package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;

import java.util.Collections;
import java.util.List;

public class DroneAINearestAttackableTarget extends EntityAITarget {
    private final EntityDrone drone;
    private final ProgWidget widget;

    /**
     * Instance of EntityAINearestAttackableTargetSorter.
     */
    private final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;

    private EntityLivingBase targetEntity;

    public DroneAINearestAttackableTarget(EntityDrone drone, int par3, boolean checkSight, ProgWidget widget) {
        this(drone, checkSight, false, widget);
    }

    public DroneAINearestAttackableTarget(EntityDrone drone, boolean checkSight, boolean easyTargetsOnly,
                                          ProgWidget widget) {
        super(drone, checkSight, easyTargetsOnly);
        this.drone = drone;
        this.widget = widget;
        theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(drone);
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        if (drone.hasMinigun() && drone.getAmmo() == null) return false;
        List<Entity> list = ((IEntityProvider) widget).getValidEntities(drone.world);
        Collections.sort(list, theNearestAttackableTargetSorter);
        for (Entity entity : list) {
            if (entity != taskOwner && entity instanceof EntityLivingBase) {
                targetEntity = (EntityLivingBase) entity;
                return true;
            }
        }
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        taskOwner.setAttackTarget(targetEntity);
        super.startExecuting();
    }
}
