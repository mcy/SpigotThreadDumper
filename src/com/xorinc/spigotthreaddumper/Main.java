package com.xorinc.spigotthreaddumper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class Main extends JavaPlugin{

	private Thread dumper = new Thread(new DumpTask(), "Xor Stacktrace Logger");
	
	private volatile boolean isLogging = false; // default off
	
	@Override
	public void onEnable() {
		
		new BukkitRunnable() {

			@Override
			public void run() {

				dumper.start();	
			}
			
		}.runTask(this);
		
	}
	
	@Override
	public void onDisable() {
		
		dumper.interrupt();
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args){
		
		isLogging = !isLogging;
		
		s.sendMessage("Thread dumping state: " + isLogging);
		
		return true;
	}
	
	private class DumpTask implements Runnable {

		File logs = new File(Main.this.getDataFolder(), "dumpLogs");
		
		Thread mainThread = Thread.currentThread();
		
		{						
			logs.mkdirs();			
		}
		
		DateFormat day = new SimpleDateFormat("yyyy.MM.dd");
		DateFormat hour = new SimpleDateFormat("'h'HH'.log'");
		DateFormat time = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
		
		@Override
		public void run() {

			while(true) {
				
				try {
						
					if(Thread.currentThread().isInterrupted())
						return;
					
					if(!isLogging)
						continue;
					
					Date now = new Date();
					
					File today = new File(logs, day.format(now));
					File log = new File(today, hour.format(now));
					
					if(!today.exists())
						today.mkdirs();
					
					if(!log.exists())
						log.createNewFile();
					
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(log, true)));
							
					StackTraceElement[] dump = mainThread.getStackTrace();
					
					out.println("Dump at time " + time.format(now));
					
					for(StackTraceElement el : dump){
						
						out.println(el);
					}
					
					out.println();
				
					out.flush();
					
					out.close();
					
					Thread.sleep(500);
				}
				
				catch (InterruptedException e){
					return;
				}
				catch (Throwable t) {
					
					System.err.println("Error dumping main thread!");
					t.printStackTrace();
				}
			}
		}
	}
}
