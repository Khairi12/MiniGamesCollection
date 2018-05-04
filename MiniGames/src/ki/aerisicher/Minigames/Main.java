package ki.aerisicher.Minigames;

import java.util.Random;
import javafx.util.Pair;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ki.aerisicher.Minigames.CP.ControlPoint;
import ki.aerisicher.Minigames.CtF.CaptureTheFlag;
import ki.aerisicher.Minigames.MA.MobArena;
import ki.aerisicher.Minigames.Utils.Utils;

public class Main extends JavaPlugin
{
	public static Main plugin;
	public static CaptureTheFlag ctf;
	public static ControlPoint cp;
	public static MobArena ma;
	
	public Pair<Integer, String> nextEvent;
	public String currentEvent;
	public Random rand;
	
	public int numOfEvents;
	public int cDDuration;
	public int countDownTime;
	public int preEventWarningTime;
	
	public void onEnable()
	{
		initialize();
		registerEvents();
		startEventRunner();
	}
	
	public void initialize()
	{
		plugin = this;
		rand = new Random();
		nextEvent = new Pair<Integer, String>(0, "Capture The Flag");
		currentEvent = "CD";
		countDownTime = 3;
		preEventWarningTime = 5;
		cDDuration = 10;
		numOfEvents = 3;
	}
	
	public void registerEvents()
	{
		this.getServer().getPluginManager().registerEvents(new CaptureTheFlag(), this);
	}
	
	public void startEventRunner()
	{
		new EventCoolDown();
	}
	
	public void activateEvent()
	{
		switch (nextEvent.getKey())
		{
		case 0:
			Main.ctf.initialize();
			Main.ctf.eventActive = true;
			currentEvent = "Capture The Flag";
			break;
		case 1:
			currentEvent = "Control Point";
			break;
		case 2:
			currentEvent = "Mob Arena";
			break;
		}
	}
	
	public void assignNextEvent()
	{
		int num = rand.nextInt(numOfEvents);

		num = 0;
		
		switch (num)
		{
		case 0:
			nextEvent = new Pair<Integer, String>(0, "Capture The Flag");
			break;
		case 1:
			nextEvent = new Pair<Integer, String>(1, "Control Point");
			break;
		case 2:
			nextEvent = new Pair<Integer, String>(2, "Mob Arena");
			break;
		}
		
		currentEvent = nextEvent.getValue();
	}
	
	// =============================================================================================
	// PROMPT METHODS
	// =============================================================================================
	
	public void promptBattleStarting(int counter)
	{
		String message = ChatColor.GREEN + "Battle begins in " + ChatColor.YELLOW + counter + 
				ChatColor.GREEN + " seconds.";
		Utils.sendParticipantsMessage(message, Main.ctf.totalPlayers);
	}
	
	public void promptBattleEnding(int counter)
	{
		String message = ChatColor.GREEN + "Battle ending in " + ChatColor.YELLOW + counter + 
				ChatColor.GREEN + " seconds.";
		Utils.sendParticipantsMessage(message, Main.ctf.totalPlayers);
	}
	
	// =============================================================================================
	// CONDITION METHODS
	// =============================================================================================
	
	public boolean checkActiveEvents()
	{
		if (Main.ctf.eventActive)
		{
			return true;
		}
		
		// check CP active
		// check MA active
		
		return false;
	}
	
	// =============================================================================================
	// TERMINATE EVENT METHODS
	// =============================================================================================
	
	public void terminateCTFEvent()
	{
		Bukkit.broadcastMessage(ChatColor.RED + "The event [" + ChatColor.YELLOW + nextEvent.getValue() + 
				ChatColor.RED + "] is closed.");
		
		Main.ctf.closeEvent();
		
		currentEvent = "CD";
	}
	
	// =============================================================================================
	// PROCESS STATE METHODS
	// =============================================================================================
	
	public int processCD(int i)
	{
		if (i == preEventWarningTime)
		{
			Bukkit.broadcastMessage(ChatColor.GREEN + "The event [" + ChatColor.YELLOW + nextEvent.getValue() + 
					ChatColor.GREEN + "] is about to start.");
		}
		
		else if (i == 0)
		{
			i = Main.ctf.lobbyDuration;
			
			Bukkit.broadcastMessage(ChatColor.GREEN + "The event [" + ChatColor.YELLOW + nextEvent.getValue() + 
					ChatColor.GREEN + "] is now open! Type " + ChatColor.RED + "/join" + ChatColor.GREEN + " to join!");
			
			activateEvent();
			assignNextEvent();
		}
		
		return i;
	}
	
	public int processCTF(int i)
	{
		if (Main.ctf.eventLobby)
		{
			if (i == preEventWarningTime || i <= countDownTime && i > 0)
			{
				promptBattleStarting(i);
			}
			
			if (i == 0)
			{
				Main.ctf.startEvent();
				i = Main.ctf.eventDuration;
			}
		}
		else
		{
			if (i == preEventWarningTime || i <= countDownTime && i > 0)
			{
				promptBattleEnding(i);
			}
			
			if (i == 0)
			{
				terminateCTFEvent();
				i = cDDuration;
			}
		}
		
		return i;
	}
	
	public int processCP(int i)
	{
		return i;
	}
	
	public int  processMA(int i)
	{
		return i;
	}
	
	// =============================================================================================
	// UPDATE (Every Second)
	// =============================================================================================
	
	public class EventCoolDown implements Runnable
	{
		int i;
		
		public EventCoolDown()
		{
			currentEvent = "CD";
			i = cDDuration;
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, this, 0, 20);
		}
		
		public void run()
		{
			i--;
			
			if (currentEvent.equals("CD"))
			{
				i = processCD(i);
			}
			else if (currentEvent.equals("Capture The Flag"))
			{
				i = processCTF(i);
			}
			else if (currentEvent.equals("Control Point"))
			{
				i = processCP(i);
			}
			else if (currentEvent.equals("Mob Arena"))
			{
				i = processMA(i);
			}
		}
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
    {
    	if (!(sender instanceof Player))
    	{
    		return false;
    	}
    	
    	Player player = (Player) sender;
    	
    	// =================================================================
    	// JOIN EVENT COMMAND
    	// =================================================================
    	
    	if (command.getName().equalsIgnoreCase("join"))
    	{
        	if (!checkActiveEvents())
        	{
        		player.sendMessage(ChatColor.RED + "There are no open events right now.");
            	return false;
        	}
        	
        	if (args.length > 0)
        	{
            	if (args[0].toLowerCase().equalsIgnoreCase("capturetheflag") || args[0].toLowerCase().equalsIgnoreCase("ctf"))
            	{
            		Main.ctf.registerPlayer(player);
            	}
            	else if (args[0].toLowerCase().equalsIgnoreCase("controlpoint") || args[0].toLowerCase().equalsIgnoreCase("cp"))
            	{
            		
            	}
            	else if (args[0].toLowerCase().equalsIgnoreCase("mobarena") || args[0].toLowerCase().equalsIgnoreCase("ma"))
            	{
            		
            	}
        	}
        	else
        	{
            	player.sendMessage(ChatColor.RED + "To join an event, do: /join [" + ChatColor.YELLOW + "capturetheflag || ctf" + ChatColor.RED + "], [" + 
            		ChatColor.YELLOW + "controlpoint || cp" + ChatColor.RED + "], [" + ChatColor.YELLOW + "mobarena || ma" + ChatColor.RED + "].");
            	
            	return false;
        	}
        	
        	return false;
    	}
    	
    	// =================================================================
    	// LEAVE EVENT COMMAND
    	// =================================================================
    	
    	else if (command.getName().equalsIgnoreCase("leave"))
    	{
        	if (!checkActiveEvents())
        	{
        		player.sendMessage(ChatColor.RED + "There are no open events right now.");
            	return false;
        	}
        	
        	if (args.length > 0)
        	{
            	if (args[0].toLowerCase().equalsIgnoreCase("capturetheflag") || args[0].toLowerCase().equalsIgnoreCase("ctf"))
            	{
            		if (Main.ctf.hasParticipant(player))
            		{
                    	player.sendMessage(ChatColor.RED + "You are no longer in this event.");
                    	Main.ctf.unRegisterPlayer(player);
            		}
            		else
            		{
                    	player.sendMessage(ChatColor.RED + "You are not a part of any event.");
            		}
            		
        			return false;
            	}
            	else if (args[0].toLowerCase().equalsIgnoreCase("controlpoint") || args[0].toLowerCase().equalsIgnoreCase("cp"))
            	{
            		
            	}
            	else if (args[0].toLowerCase().equalsIgnoreCase("mobarena") || args[0].toLowerCase().equalsIgnoreCase("ma"))
            	{
            		
            	}
        	}
        	else
        	{
            	player.sendMessage(ChatColor.RED + "To leave an event, do: /leave [" + ChatColor.YELLOW + "capturetheflag / ctf" + 
            		ChatColor.RED + "], [" + ChatColor.YELLOW + "controlpoint / cp" + ChatColor.RED + "], [" + 
            		ChatColor.YELLOW + "mobarena / ma" + ChatColor.RED + "].");
            	
            	return false;
        	}
        	
        	return false;
    	}
    	
    	return false;
    }
	
}
