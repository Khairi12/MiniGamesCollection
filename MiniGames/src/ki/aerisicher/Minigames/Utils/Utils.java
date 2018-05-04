package ki.aerisicher.Minigames.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Utils 
{
	public static void giveColoredLeather(Player player, Color color)
	{
		ItemStack helm = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		
		LeatherArmorMeta lamh = (LeatherArmorMeta) helm.getItemMeta();
		LeatherArmorMeta lamc = (LeatherArmorMeta) chest.getItemMeta();
		LeatherArmorMeta laml = (LeatherArmorMeta) leggings.getItemMeta();
		LeatherArmorMeta lamb = (LeatherArmorMeta) boots.getItemMeta();
		
		lamh.setColor(color);
		lamc.setColor(color);
		laml.setColor(color);
		lamb.setColor(color);
		
		helm.setItemMeta(lamh);
		chest.setItemMeta(lamc);
		leggings.setItemMeta(laml);
		boots.setItemMeta(lamb);
		
		player.getInventory().setHelmet(helm);
		player.getInventory().setChestplate(chest);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);
		
		player.updateInventory();
	}
	
	public static void sendParticipantsMessage(String message, ArrayList<Player> alp)
	{
		for (int i = 0; i < alp.size(); i++)
		{
			alp.get(i).sendMessage(message);
		}
	}
	
	public static void sendParticipantsMessage(String message, Set<String> playerNames, ArrayList<Player> alp)
	{
		for (int i = 0; i < alp.size(); i++)
		{
			if (playerNames.contains(alp.get(i).getName()))
			{
				alp.get(i).sendMessage(message);
			}
		}
	}
	
	public static void teleportAll(ArrayList<Player> alp, HashMap<Player, Location> hmpl)
	{
		for (int i = 0; i < alp.size(); i++)
		{
			Player player = alp.get(i);
			Location location = hmpl.get(player);
			player.teleport(location);
		}
	}
}
