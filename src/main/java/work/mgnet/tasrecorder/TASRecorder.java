package work.mgnet.tasrecorder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import work.mgnet.tasrecorder.commands.RecordCommand;
import work.mgnet.tasrecorder.utils.SecureList;

/**
 * TAS Recorder Class manages recording, setting up ffmpeg and hooks up events to the Game
 * @author Pancake
 */
@Mod(modid = TASRecorder.MODID, name = TASRecorder.NAME, version = TASRecorder.VERSION)
public class TASRecorder {
	
	/**
	 * Mod ID for Forge
	 */
	public static final String MODID = "tasrecorder";
	
	/**
	 * Name of the Mod
	 */
	public static final String NAME = "TASRecorder";
	
	/**
	 * Current Version of the Mod
	 */
	public static final String VERSION = "1.0.0-SNAPSHOT";
	
	/**
	 * Universal Videos Folder. Windows.
	 */
	public static final File VIDEOS = new File(System.getenv("userprofile"), "Videos");
	
	/**
	 * This List of GuiScreens are being recorded during recording. All others will be filtered out
	 */
	public static final List<String> ALLOWED_GUI = Arrays.asList("GuiChat", "GuiSleepMP", "GuiCommandBlock", "GuiContainer", "GuiBeacon", "GuiBrewingStand", "GuiChest", "GuiCrafting", "GuiDispenser",
			"GuiEnchantment", "GuiFurnace", "GuiHopper", "GuiMerchant", "GuiRepair", "GuiScreenHorseInventory", "GuiShulkerBox", "InventoryEffectRenderer", "GuiContainerCreative", "GuiInventory",
			"GuiEditCommandBlockMinecart", "GuiEditSign", "GuiGameOver", "GuiScreenBook", "GuiScreenDemo", "GuiWinGame", "GuiConfirmOpenLink");
	
	/**
	 * Thread-Safe Atomic Boolean to see if the recording is running
	 */
	public static AtomicBoolean isRecording = new AtomicBoolean(false);

	/**
	 * Width of the Screen
	 */
	public static int width;
	
	/**
	 * Height of the Screen
	 */
	public static int height;
	
	/**
	 * Thread-Safe List for raw screenshots
	 */
	public static SecureList list;
	
	/**
	 * Whether a Screenshot should be taken.
	 */
	public static boolean takeScreenshot;
	
	/**
	 * Ran when minecraft initializes, registers events.
	 * @param event
	 */
	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/**
	 * Registers a new Command when the Integrated Server starts.
	 * @param event
	 */
	@EventHandler
	public void start(FMLServerStartingEvent event) {
		event.registerServerCommand(new RecordCommand());
	}
	
	/**
	 * Takes a screenshot if necessary after the game overlay was rendered.
	 * @param event
	 */
	@SubscribeEvent
	public void onRenderWorld(RenderGameOverlayEvent.Post event) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		// Update GuiScreen with NULL if the Gui is an allowed gui.
		// Done to pass the next check screen == null
		if (screen != null) if (ALLOWED_GUI.contains(screen.getClass().getSimpleName())) screen = null;
		if (TASRecorder.takeScreenshot && screen == null) {
			TASRecorder.takeScreenshot = false;
			TASRecorder.takeScreenshot();
		}
	}
	
	/**
	 * Starts a recording
	 */
	public static void startRecording() {
		if (!VIDEOS.exists()) VIDEOS.mkdir();
		System.gc();
		width = Display.getWidth();
		height = Display.getHeight();
		list = new SecureList(32, width*height*3);
		isRecording.set(true);
		/* Starts a Thread for sending the images from the buffer list and ffmpeg */
		new Thread(() -> {
			try {
				// ffmpeg command line
				String ffmpeg = "C:\\Users\\games\\Downloads\\ffmpeg-N-104544-gbfbd5954e5-win64-gpl\\ffmpeg-N-104544-gbfbd5954e5-win64-gpl\\bin\\ffmpeg.exe ";
				ffmpeg += "-y -hwaccel cuda -hwaccel_output_format cuda ";
				ffmpeg += "-f rawvideo -c:v rawvideo ";
				ffmpeg += "-s " + width + "x" + height + " -pix_fmt rgb24 -r 60 ";
				ffmpeg += "-i - ";
				ffmpeg += "-vf vflip ";
				ffmpeg += "-b:v 50M -c:v h264_nvenc ";
				ffmpeg += "output.mp4";
				// start process
				final ProcessBuilder pb = new ProcessBuilder(ffmpeg.split(" "));
				pb.redirectOutput(Redirect.INHERIT);
				pb.redirectErrorStream(true);
				pb.redirectError(Redirect.INHERIT);
				final Process p = pb.start();
				OutputStream stream = p.getOutputStream();
				System.out.println("Process started.");
				
				// resuse buffers and arrays for optimal memory usage
				ByteBuffer b;
				byte[] array = new byte[width*height*3];
				while (isRecording.get()) {
					/* Find and lock a Buffer in the list */
					if (list.containsFilledUnlocked()) {
						int i = list.findFilled();
						if (i == 32) continue;
						// obtain buffer and load into byte array
						b = list.getAndLock(i, false);
						b.get(array);
						// send that byte array
						stream.write(array);
						list.unlock(i);
					}
				}
				
				// After /r /record has been run again stop the process by closing the streams, causing SIGINT
				System.out.println("Process stopped.");
				stream.flush();
				stream.close();
				p.getInputStream().close();
				p.getErrorStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
		/* Screenshot every 16 Milliseconds for 60 fps */
		new Thread(() ->  {
			try {
				while (isRecording.get()) {
					takeScreenshot = true;
					Thread.sleep(16L);
				}
			} catch (Exception e) {}
		}).start();
	}
	
	/**
	 * Stops the recording and frees memory. Not Really.
	 */
	public static void endRecording() {
		isRecording.set(false);
		list.clear();
	}
	
	/**
	 * Takes a screenshot and adds it to the list of buffers
	 */
	public static void takeScreenshot() {
		if (list.containsUnfilledUnlocked()) {
			int unfilled = list.findUnfilled();
			ByteBuffer b = list.getAndLock(unfilled, true);
			GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, b);
			list.unlock(unfilled);
		}
	}
	
}
