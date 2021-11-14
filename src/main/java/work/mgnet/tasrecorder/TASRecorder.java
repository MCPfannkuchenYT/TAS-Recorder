package work.mgnet.tasrecorder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import work.mgnet.tasrecorder.commands.RecordCommand;

@Mod(modid = TASRecorder.MODID, name = TASRecorder.NAME, version = TASRecorder.VERSION)
public class TASRecorder {
	
	public static final String MODID = "tasrecorder";
	public static final String NAME = "TASRecorder";
	public static final String VERSION = "1.4";
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@EventHandler
	public void start(FMLServerStartingEvent event) {
		event.registerServerCommand(new RecordCommand());
	}
	
	@SubscribeEvent
	public void keybind(InputEvent.KeyInputEvent event) {
		
	}
	
	public static final File videosFolder = new File(System.getenv("userprofile"), "Videos");
	
	public static AtomicBoolean isRecording = new AtomicBoolean(false);
	
	/**
	 * Starts a recording
	 */
	public static void startRecording() {
		if (!videosFolder.exists()) videosFolder.mkdir();
		System.gc();
		width = Display.getWidth();
		height = Display.getHeight();
		list = new SecureList(32, width*height*3);
		isRecording.set(true);
		new Thread(() -> {
			try {
				String ffmpeg = "C:\\Users\\games\\Downloads\\ffmpeg-N-104544-gbfbd5954e5-win64-gpl\\ffmpeg-N-104544-gbfbd5954e5-win64-gpl\\bin\\ffmpeg.exe ";
				ffmpeg += "-y -hwaccel cuda -hwaccel_output_format cuda ";
				ffmpeg += "-f rawvideo -c:v rawvideo ";
				ffmpeg += "-s " + width + "x" + height + " -pix_fmt rgb24 -r 20 ";
				ffmpeg += "-i - ";
				ffmpeg += "-vf vflip ";
				ffmpeg += "-b:v 20M -c:v h264_nvenc ";
				ffmpeg += "output.mp4";
				final ProcessBuilder pb = new ProcessBuilder(ffmpeg.split(" "));
				pb.redirectOutput(Redirect.INHERIT);
				pb.redirectErrorStream(true);
				pb.redirectError(Redirect.INHERIT);
				final Process p = pb.start();
				OutputStream stream = p.getOutputStream();
				System.out.println("Process started.");
				ByteBuffer b;
				byte[] array = new byte[width*height*3];
				while (isRecording.get()) {
					if (list.containsFilledUnlocked()) {
						int i = list.findFilled();
						if (i == 32) continue;
						b = list.getAndLock(i, false);
						b.get(array);
						stream.write(array);
						list.unlock(i);
					}
				}
				System.out.println("Process stopped.");
				stream.flush();
				stream.close();
				p.getInputStream().close();
				p.getErrorStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	public static void endRecording() {
		isRecording.set(false);
		list.clear();
	}
	
	// Screenshot Variables
	public static int width;
	public static int height;
	public static SecureList list;
	public static boolean takeScreenshot;
	
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
