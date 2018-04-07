package com.iamshift.mineaddons.utils;

import java.util.Random;

import com.iamshift.mineaddons.blocks.BlockInvLight;
import com.iamshift.mineaddons.core.Config;
import com.iamshift.mineaddons.core.Refs;
import com.iamshift.mineaddons.entities.EntityAncientCarp;
import com.iamshift.mineaddons.entities.EntityBrainlessShulker;
import com.iamshift.mineaddons.entities.EntityHellhound;
import com.iamshift.mineaddons.fluids.blocks.BlockCursedWater;
import com.iamshift.mineaddons.fluids.blocks.BlockSacredWater;
import com.iamshift.mineaddons.init.ModEntities;
import com.iamshift.mineaddons.init.ModItems;
import com.iamshift.mineaddons.interfaces.IUncapturable;
import com.iamshift.mineaddons.items.tools.ItemBreaker;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod.EventBusSubscriber(modid = Refs.ID)
public class EventHandler
{
	@SubscribeEvent
	public static void createFluidSource(BlockEvent.CreateFluidSourceEvent event)
	{
		if(event.getState().getBlock() instanceof BlockSacredWater)
		{
			event.setResult(Event.Result.ALLOW);
			return;
		}

		if(event.getState().getBlock() instanceof BlockCursedWater)
		{
			event.setResult(Event.Result.ALLOW);
			return;
		}
	}

	@SubscribeEvent
	public static void onLootTableLoad(LootTableLoadEvent event)
	{
		if (event.getName().equals(LootTableList.ENTITIES_WITHER_SKELETON)) 
		{
			final LootPool pool2 = event.getTable().getPool("pool2");

			if (pool2 != null)
				pool2.addEntry(new LootEntryItem(ModItems.WitherDust, 5, 0, new LootFunction[0], new LootCondition[0], "loottable:witherdust"));
		}

		if (event.getName().equals(LootTableList.ENTITIES_GUARDIAN)) 
		{
			final LootPool pool2 = event.getTable().getPool("pool2");

			if (pool2 != null)
				pool2.addEntry(new LootEntryItem(ModItems.RainbowBottle, 5, 0, new LootFunction[0], new LootCondition[0], "loottable:rainbowbottle"));
		}
	}

	@SubscribeEvent
	public static void onEntityDie(LivingDeathEvent event)
	{
		if(event.getEntity().world.isRemote)
			return;

		if(!event.getEntity().world.getGameRules().getBoolean("doMobLoot"))
			return;

		Entity entity = event.getEntity();
		Random rand = new Random();

		if(entity instanceof EntityWolf)
		{
			if(rand.nextInt(15) == 0)
			{
				entity.dropItem(Items.FEATHER, 1);
				return;
			}

			if(!(entity instanceof EntityHellhound))
			{
				if(rand.nextInt(15) == 0)
				{
					Item wool = Item.getItemFromBlock(Blocks.WOOL);
					ItemStack stack = new ItemStack(wool, 1, rand.nextInt(16));
					entity.entityDropItem(stack, 0.0F);
					return;
				}
			}
			else
			{
				if(rand.nextInt(15) == 0)
					entity.dropItem(Item.getItemFromBlock(Blocks.MAGMA), 1);
			}
		}

		if(entity instanceof EntityBrainlessShulker)
		{
			EntityBrainlessShulker e = (EntityBrainlessShulker) entity;

			ItemStack drop = new ItemStack(ModItems.BrainlessShulkerEgg, 1, e.getColor().getDyeDamage());
			e.entityDropItem(drop, 0.0F);
		}

	}

	@SubscribeEvent
	public static void onEntityJoin(EntityJoinWorldEvent event)
	{
		if(event.getWorld().isRemote)
			return;

		if(event.getEntity() instanceof EntityAncientCarp)
		{
			if(ModEntities.ANCIENT_LIMIT < Config.MaxAncientCarps)
				ModEntities.ANCIENT_LIMIT++;
			else
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onFireworkUse(RightClickItem event) 
	{
		if(event.getItemStack() == null)
			return;

		EntityPlayer player = event.getEntityPlayer();
		if(player == null)
			return;

		if((event.getItemStack().getItem() instanceof ItemFirework))
		{	
			if(player.isSneaking() && !player.isElytraFlying())
			{
				player.addVelocity(0D, 3D, 0D);
				player.playSound(SoundEvents.ENTITY_FIREWORK_LAUNCH, 1.0F, 1.0F);

				if(!player.capabilities.isCreativeMode)
					event.getItemStack().shrink(1);
			}

			return;
		}
	}

	@SubscribeEvent
	public static void onBlockMined(LeftClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;

		if(!player.capabilities.isCreativeMode)
		{
			if((player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemBreaker))
			{
				if(!world.isRemote)
				{
					BlockPos pos = event.getPos();
					IBlockState state = world.getBlockState(pos);
					boolean unbreakable = state.getBlockHardness(world, pos) < 0.0F ? true : false;

					if(!unbreakable && !(state.getBlock() instanceof BlockInvLight))
					{
						event.setCanceled(true);
						return;
					}

					world.setBlockToAir(pos);
					world.playEvent(2001, pos, Block.getStateId(state));

					ItemStack stack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().getMetaFromState(state));
					world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack));
				}
			}
		}
	}

	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public static void onRightClick(EntityInteract event)
	{
		Item item = event.getItemStack().getItem();
		Entity target = event.getTarget();

		if(target instanceof IUncapturable)
		{
			if(Config.isCaptureItem(item.getRegistryName().getResourceDomain() + ":" + item.getRegistryName().getResourcePath()))
			{
				event.setCanceled(true);
				return;
			}
		}

		if(target instanceof EntityAncientCarp)
		{
			if(Config.CaptureAncientCarps)
			{
				if(Config.isCaptureItem(item.getRegistryName().getResourceDomain() + ":" + item.getRegistryName().getResourcePath()))
				{
					event.setCanceled(true);
					return;
				}
			}
		}
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public static void onLeftClick(AttackEntityEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();
		Entity target = event.getTarget();
		Item item = player.getHeldItem(EnumHand.MAIN_HAND).getItem();

		if(target instanceof IUncapturable)
		{
			if(Config.isCaptureItem(item.getRegistryName().getResourceDomain() + ":" + item.getRegistryName().getResourcePath()))
			{
				event.setCanceled(true);
				return;
			}
		}

		if(target instanceof EntityAncientCarp)
		{
			if(Config.CaptureAncientCarps)
			{
				if(Config.isCaptureItem(item.getRegistryName().getResourceDomain() + ":" + item.getRegistryName().getResourcePath()))
				{
					event.setCanceled(true);
					return;
				}
			}
		}
	}

	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public static void onEntityCollide(ProjectileImpactEvent.Throwable event)
	{
		Entity target = event.getRayTraceResult().entityHit;
		EntityEntry entity = EntityRegistry.getEntry(event.getThrowable().getClass());

		if(entity == null)
			return;

		if(target instanceof IUncapturable)
		{
			if(Config.isCaptureEntity(entity.getRegistryName().getResourceDomain() + ":" + entity.getRegistryName().getResourcePath()))
			{
				event.setCanceled(true);
				return;
			}
		}

		if(target instanceof EntityAncientCarp)
		{
			if(Config.CaptureAncientCarps)
			{
				if(Config.isCaptureEntity(entity.getRegistryName().getResourceDomain() + ":" + entity.getRegistryName().getResourcePath()))
				{
					event.setCanceled(true);
					return;
				}
			}
		}
	}
}
