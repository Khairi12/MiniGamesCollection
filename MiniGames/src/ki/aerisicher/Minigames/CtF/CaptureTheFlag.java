package ki.aerisicher.Minigames.CtF;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import ki.aerisicher.Minigames.Main;
import ki.aerisicher.Minigames.Utils.Utils;

public class CaptureTheFlag implements Listener
{
	public Material team1Base;
	public Material team2Base;
	
	public ArrayList<Player> totalPlayers;
	public HashMap<Player, ItemStack[]> playerInventory;
	public HashMap<Player, Location> playersLocations;
	public Location lobbySpawn;
	public Location team1Spawn;
	public Location team2Spawn;
	public Location team1Flag;
	public Location team2Flag;
	
	public Player team1FlagThief;
	public Player team2FlagThief;
	public ScoreboardManager manager;
	public Scoreboard scoreboard;
	public Objective objective;
	public Team team1Players;
	public Team team2Players;
	public Score redScoreBanner;
	public Score blueScoreBanner;
	public Team team1FlagPoints;
	public Team team2FlagPoints;
	
	public int team1Points;
	public int team2Points;
	public int maxPlayers;
	public int lobbyDuration;
	public int eventDuration;
	public int flagScore;
	public int killScore;
	
	public boolean eventLobby;
	public boolean eventActive;
	
	public CaptureTheFlag()
	{
		Main.ctf = this;
		initialize();
	}
	
	public void initialize()
	{
		setupScoreboard();
		
		team1Players = scoreboard.registerNewTeam("team1Players");
		team2Players = scoreboard.registerNewTeam("team2Players");
		
		team1Base = Material.REDSTONE_BLOCK;
		team2Base = Material.LAPIS_BLOCK;
		
		lobbySpawn = new Location (Bukkit.getWorlds().get(0), 641, 67, 964);
		team1Spawn = new Location (Bukkit.getWorlds().get(0), 637, 67, 966);
		team2Spawn = new Location (Bukkit.getWorlds().get(0), 637, 67, 962);
		team1Flag = new Location (Bukkit.getWorlds().get(0), 633, 67, 966);
		team2Flag = new Location (Bukkit.getWorlds().get(0), 633, 67, 962);
		
		playerInventory = new HashMap<Player, ItemStack[]>();
		playersLocations = new HashMap<Player, Location>();
		totalPlayers = new ArrayList<Player>();
		
		flagScore = 1;
		killScore = 0;
		maxPlayers = 20;
		team1Points = 0;
		team2Points = 0;
		lobbyDuration = 10;
		eventDuration = 20;
		eventLobby = true;
		eventActive = false;
	}
	
	// =============================================================================================
	// START GAME METHODS
	// =============================================================================================
	
	public void startEvent()
	{
		teleportToSpawn();
		spawnRedFlag();
		spawnBlueFlag();
		
		eventLobby = false;
		eventActive = true;
	}
	
	public void teleportToSpawn()
	{
		for (int i = 0; i < totalPlayers.size(); i++)
		{
			Player player = totalPlayers.get(i);
			Location loc = team1Players.hasEntry(player.getName()) ? team1Spawn : team2Spawn;
			player.teleport(loc);
		}
	}
	
	public void spawnRedFlag()
	{
		if (team1Flag.getBlock().getType() == Material.STANDING_BANNER)
		{
			return;
		}
		
		team1Flag.getBlock().setType(Material.STANDING_BANNER);
		Banner banner = (Banner) team1Flag.getBlock().getState();
		org.bukkit.material.Banner bannerData = (org.bukkit.material.Banner) banner.getData();
		bannerData.setFacingDirection(BlockFace.EAST);
		banner.setData(bannerData);
		banner.setBaseColor(DyeColor.RED);
		banner.update();
	}
	
	public void spawnBlueFlag()
	{
		if (team2Flag.getBlock().getType() == Material.STANDING_BANNER)
		{
			return;
		}
		
		team2Flag.getBlock().setType(Material.STANDING_BANNER);
		Banner banner = (Banner) team2Flag.getBlock().getState();
		org.bukkit.material.Banner bannerData = (org.bukkit.material.Banner) banner.getData();
		bannerData.setFacingDirection(BlockFace.EAST);
		banner.setData(bannerData);
		banner.setBaseColor(DyeColor.BLUE);
		banner.update();
	}
	
	// =============================================================================================
	// REGISTER EVENT METHODS
	// =============================================================================================
	
	public void registerPlayer(Player player)
	{
		if (isEventFull())
		{
			player.sendMessage(ChatColor.RED + "This event is full.");
			return;
		}
		
		if (hasParticipant(player))
		{
			player.sendMessage(ChatColor.RED + "You are already registered for this event.");
			return;
		}
		
		addPlayer(player);
		holdInventory(player);
		assignTeam(player);
		assignClass(player);
		teleportToLobby(player);
		showScoreboard(player);
		
    	player.sendMessage(ChatColor.GREEN + "You've joined [" + ChatColor.YELLOW + "Capture The Flag" + 
    			ChatColor.GREEN + "]!");
	}
	
	public void addPlayer(Player player)
	{
		totalPlayers.add(player);
	}
	
	public void holdInventory(Player player)
	{
		ItemStack[] inventoryContent = player.getInventory().getContents();
		playerInventory.put(player, inventoryContent);
		
		player.getInventory().clear();
		player.updateInventory();
	}
	
	public void assignTeam(Player player)
	{
		if (team1Players.getSize() <= team2Players.getSize())
		{
			team1Players.addEntry(player.getName());
		}
		else
		{
			team2Players.addEntry(player.getName());
		}
	}
	
	public void assignClass(Player player)
	{
		if (team1Players.hasEntry(player.getName()))
		{
			Utils.giveColoredLeather(player, Color.RED);
		}
		else
		{
			Utils.giveColoredLeather(player, Color.BLUE);
		}
	}
	
	public void teleportToLobby(Player player)
	{
		Location location = player.getLocation();
		playersLocations.put(player, location);
		
		if (eventLobby)
		{
			player.teleport(lobbySpawn);
		}
		else
		{
			if (team1Players.hasEntry(player.getName()))
			{
				player.teleport(team1Spawn);
			}
			else
			{
				player.teleport(team2Spawn);
			}
		}
	}
	
	// =============================================================================================
	// UNREGISTER EVENT METHODS
	// =============================================================================================
	
	public void unRegisterPlayer(Player player)
	{
		restoreFlag(player);
		teleportOut(player);
		returnInventory(player);
		unassignClass(player);
		unassignTeam(player);
		hideScoreboard(player);
		removePlayer(player);
	}
	
	public void removePlayer(Player player)
	{
		totalPlayers.remove(totalPlayers.indexOf(player));
	}
	
	public void returnInventory(Player player)
	{
		player.getInventory().clear();
		player.getInventory().setContents(playerInventory.get(player));
		player.updateInventory();
		
		playerInventory.remove(player);
	}
	
	public void unassignTeam(Player player)
	{
		if (team1Players.hasEntry(player.getName()))
		{
			team1Players.removeEntry(player.getName());
		}
		else
		{
			team2Players.removeEntry(player.getName());
		}
	}
	
	public void unassignClass(Player player)
	{
		for (int i = 0; i < player.getInventory().getArmorContents().length; i++)
		{
			player.getInventory().getArmorContents()[i] = null;
		}
		
		player.updateInventory();
	}
	
	public void teleportOut(Player player)
	{
		player.teleport(playersLocations.get(player));
	}
	
	// =============================================================================================
	// CLOSE EVENT METHODS
	// =============================================================================================
	
	public void closeEvent()
	{
		eventLobby = true;
		eventActive = false;
		
		teleportAllBack();
		removeAllClasses();
		removeAllTeams();
		returnAllInventories();
		hideAllScoreboards();
		dispose();
	}
	
	public void teleportAllBack()
	{
		Utils.teleportAll(totalPlayers, playersLocations);
	}
	
	public void removeAllClasses()
	{
		for (int i = 0; i < totalPlayers.size(); i++)
		{
			Player player = totalPlayers.get(i);
			
			player.getInventory().clear();
			player.updateInventory();
		}
	}
	
	public void removeAllTeams()
	{
		for (int i = 0; i < totalPlayers.size(); i++)
		{
			if (team1Players.hasEntry(totalPlayers.get(i).getName()))
			{
				team1Players.removeEntry(totalPlayers.get(i).getName());
			}
			else
			{
				team2Players.removeEntry(totalPlayers.get(i).getName());
			}
		}
	}
	
	public void returnAllInventories()
	{
		for (int i = 0; i < totalPlayers.size(); i++)
		{
			Player player = totalPlayers.get(i);
			player.getInventory().setContents(playerInventory.get(player));
			player.updateInventory();
		}
	}
	
	/*
	public void determineWinner()
	{
		if (t1Score.team1Score > t1Score.team2Score)
		{
			Utils.sendParticipantsMessage(ChatColor.YELLOW + "TEAM 1 WINS!", totalPlayers);
		}
		else if (t1Score.team2Score > t1Score.team1Score)
		{
			Utils.sendParticipantsMessage(ChatColor.YELLOW + "TEAM 2 WINS!", totalPlayers);
		}
		else
		{
			Utils.sendParticipantsMessage(ChatColor.YELLOW + "DRAW!", totalPlayers);
		}
	}
	*/
	
	// =============================================================================================
	// CTF SCORE BOARD
	// =============================================================================================
	
	public void setupScoreboard()
	{
		// setup general scoreboard layout
		manager = Bukkit.getScoreboardManager();
		scoreboard = manager.getNewScoreboard();
		objective = scoreboard.registerNewObjective("Flag", "");
		objective.setDisplayName(">> Capture The Flag <<");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.getScore("" + ChatColor.BLACK).setScore(15);
		objective.getScore("" + ChatColor.WHITE).setScore(12);
		
		// setup team1 layout
		redScoreBanner = objective.getScore(">> " + ChatColor.RED + "Team 1 Flags: ");
		redScoreBanner.setScore(14);
		
		team1FlagPoints = scoreboard.registerNewTeam("team1FlagPoints");
		team1FlagPoints.addEntry("" + ChatColor.RED);
		team1FlagPoints.setPrefix("-> " + team1Points);
		objective.getScore("" + ChatColor.RED).setScore(13);
		
		// setup team2 layout
		blueScoreBanner = objective.getScore(">> " + ChatColor.BLUE + "Team 2 Flags: ");
		blueScoreBanner.setScore(11);
		
		team2FlagPoints = scoreboard.registerNewTeam("team2FlagPoints");
		team2FlagPoints.addEntry("" + ChatColor.BLUE);
		team2FlagPoints.setPrefix("-> " + team2Points);
		objective.getScore("" + ChatColor.BLUE).setScore(10);
	}
	
	public void disposeScoreboard()
	{
		team1Points = 0;
		team2Points = 0;
		redScoreBanner = null;
		blueScoreBanner = null;
		team1FlagPoints = null;
		team2FlagPoints = null;
		team1Players = null;
		team2Players = null;
		objective = null;
		scoreboard = null;
		manager = null;
	}
	
	public void showScoreboard(Player player)
	{
		player.setScoreboard(scoreboard);
	}
	
	public void hideScoreboard(Player player)
	{
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}
	
	public void hideAllScoreboards()
	{
		for (int i = 0; i < totalPlayers.size(); i++)
		{
			totalPlayers.get(i).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}
	
	public void addScore(Player player, int amount)
	{
		if (team1Players.hasEntry(player.getName()))
		{
			team1Points += amount;
		}
		else
		{
			team2Points += amount;
		}
	}
	
	public void updateAllScores()
	{
		for (int i = 0; i < totalPlayers.size(); i++)
		{
			totalPlayers.get(i).getScoreboard().getTeam("team1FlagPoints").setPrefix("-> " + team1Points);
			totalPlayers.get(i).getScoreboard().getTeam("team2FlagPoints").setPrefix("-> " + team2Points);
		}
	}
	
	public void subtractScore(Player player, int amount)
	{
		if (team1Players.hasEntry(player.getName()))
		{
			team1Points = team1Points - amount < 0 ? 0 : team1Points - amount;
		}
		else
		{
			team2Points = team2Points - amount < 0 ? 0 : team2Points - amount;
		}
	}
	
	// =============================================================================================
	// CTF GAMEPLAY METHODS
	// =============================================================================================
	
	public void team2StealFlag(Player player)
	{
		String message = player.getName() + " is attempting to steal your flag!";
		Utils.sendParticipantsMessage(message, team1Players.getEntries(), totalPlayers);
		team2FlagThief = player;
	}
	
	public void team1StealFlag(Player player)
	{
		String message = player.getName() + " is attempting to steal your flag!";
		Utils.sendParticipantsMessage(message, team2Players.getEntries(), totalPlayers);
		team1FlagThief = player;
	}
	
	public void team1ScoreFlag(Player player)
	{
		String message = ChatColor.YELLOW + player.getName() + ChatColor.GRAY + 
				" successfully stole team 2's flag!";
		Utils.sendParticipantsMessage(message, totalPlayers);

		addScore(player, flagScore);
		updateAllScores();
		restoreFlag(player);
	}
	
	public void team2ScoreFlag(Player player)
	{
		String message = ChatColor.YELLOW + player.getName() + ChatColor.GRAY + 
				" successfully stole team 1's flag!";
		Utils.sendParticipantsMessage(message, totalPlayers);

		addScore(player, flagScore);
		updateAllScores();
		restoreFlag(player);
	}
	
	public void lostFlag(Player player)
	{
		String message = player.getName() + " lost the flag.";
		Utils.sendParticipantsMessage(message, totalPlayers);
		restoreFlag(player);
	}
	
	public void restoreFlag(Player player)
	{
		if (player == team1FlagThief)
		{
			team1FlagThief = null;
			spawnBlueFlag();
		}
		else if (player == team2FlagThief)
		{
			team2FlagThief = null;
			spawnRedFlag();
		}
	}
	
	// =============================================================================================
	// CONDITIONS
	// =============================================================================================
	
	public boolean isOnBase(Player player, Material block)
	{
		return player.getLocation().getBlock().getType() == block ? true : false;
	}
	
	public boolean isEventFull()
	{
		return totalPlayers != null && totalPlayers.size() > maxPlayers ? true : false;
	}
	
	public boolean hasParticipant(Player player)
	{
		return totalPlayers != null && totalPlayers.contains(player) ? true : false;
	}
	
	public boolean isEnemy(Player attacker, Player attacked)
	{
		if (team1Players.getEntries().contains(attacker.getName()) && team2Players.getEntries().contains(attacked.getName()) || 
				team1Players.getEntries().contains(attacked.getName()) && team2Players.getEntries().contains(attacker.getName()))
		{
			return true;
		}
		
		return false;
	}
	
	// =============================================================================================
	// DISPOSE
	// =============================================================================================
	
	public void dispose()
	{
		disposeScoreboard();
		
		team1Base = null;
		team2Base = null;
		lobbySpawn = null;
		team1Spawn = null;
		team2Spawn = null;
		team1Flag = null;
		team2Flag = null;
		totalPlayers = null;
		playerInventory = null;
		playersLocations = null;
		team1FlagThief = null;
		team2FlagThief = null;
	}
	
	// =============================================================================================
	// EVENT HANDLERS
	// =============================================================================================
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		if (!eventActive || !hasParticipant(player))
		{
			return;
		}
		
		restoreFlag(player);
		unRegisterPlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		
		if (!eventActive || !hasParticipant(player))
		{
			return;
		}
		
		if (team1Players.hasEntry(player.getName()))
		{
			if (team1FlagThief == null)
			{
				return;
			}
			
			if (team1FlagThief == player && block.getType() == team1Base)
			{
				team1ScoreFlag(player);
			}
		}
		else
		{
			if (team2FlagThief == null)
			{
				return;
			}
			
			if (team2FlagThief == player && block.getType() == team2Base)
			{
				team2ScoreFlag(player);
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().getType() == Material.BANNER)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		
		if (!eventActive || !hasParticipant(player))
		{
			return;
		}
		
		if (team1Players.hasEntry(player.getName()))
		{
			if (team1FlagThief != null && team1FlagThief == player)
			{
				subtractScore(player, killScore);
				updateAllScores();
				lostFlag(player);
			}
		}
		else
		{
			if (team2FlagThief != null && team2FlagThief == player)
			{
				subtractScore(player, killScore);
				updateAllScores();
				lostFlag(player);
			}
		}
		
		event.setKeepInventory(true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		if (!eventActive || !hasParticipant(player) || !(block.getType() == Material.STANDING_BANNER))
		{
			return;
		}
		
		Banner sbanner = (Banner) block.getState();
		
		if (sbanner.getBaseColor() == DyeColor.RED)
		{
			if (!team1Players.hasEntry(player.getName()))
			{
				team2StealFlag(player);
				return;
			}
		}
		else if (sbanner.getBaseColor() == DyeColor.BLUE)
		{
			if (!team2Players.hasEntry(player.getName()))
			{
				team1StealFlag(player);
				return;
			}
		}
		
		event.setCancelled(true);
		return;
	}
	
	@EventHandler
	public void onItemToss(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		
		if (!eventActive || !hasParticipant(player))
		{
			return;
		}
		
		if (event.getItemDrop().getItemStack().getType() == Material.BANNER)
		{
			player.sendMessage(ChatColor.RED + "You need to return the flag to your base.");
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You can not drop starter class items");
		}
		
		event.setCancelled(true);
		return;
	}
	
	@EventHandler
	public void onBlockTouch(BlockDamageEvent event)
	{
		if (!eventActive || !hasParticipant(event.getPlayer()) || !(event.getBlock().getType() == Material.STANDING_BANNER))
		{
			return;
		}
		
		Banner sbanner = (Banner) event.getBlock().getState();
		
		if (sbanner.getBaseColor() == DyeColor.RED || sbanner.getBaseColor() == DyeColor.BLUE)
		{
			event.setInstaBreak(true);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		
		if (!eventActive || !hasParticipant(player))
		{
			return;
		}
		
		if (eventLobby)
		{
			event.setRespawnLocation(lobbySpawn);
		}
		else
		{
			if (team1Players.hasEntry(player.getName()))
			{
				event.setRespawnLocation(team1Spawn);
				Utils.giveColoredLeather(player, Color.RED);
			}
			else
			{
				event.setRespawnLocation(team2Spawn);
				Utils.giveColoredLeather(player, Color.BLUE);
			}
		}
	}
	
	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event)
	{
		if (!eventActive)
		{
			return;
		}
		
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Entity)
		{
			return;
		}
		else if (event.getDamager() instanceof Entity && event.getEntity() instanceof Player)
		{
			return;
		}
		else if (event.getDamager() instanceof Player || event.getEntity() instanceof Player)
		{
			Player attacker = (Player) event.getDamager();
			Player attacked = (Player) event.getEntity();
			
			if (!hasParticipant(attacker) || !hasParticipant(attacked))
			{
				return;
			}
			
			if (!isEnemy(attacker, attacked))
			{
				event.setCancelled(true);
			}
		}
	}
	
}
