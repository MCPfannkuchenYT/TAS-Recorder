package work.mgnet.tasrecorder.commands;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import work.mgnet.tasrecorder.TASRecorder;

/**
 * Command that starts the recording
 * @author Pancake
 */
public class RecordCommand extends CommandBase {
	
	/**
	 * Returns the name of the command
	 */
	@Override
	public String getName() {
		return "record";
	}

	/**
	 * Returns the way how to use the command (there is none)
	 */
	@Override
	public String getUsage(ICommandSender sender) {
		return "/record";
	}

	/**
	 * Returns the permission level (0)
	 */
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    /**
     * Returns other names for the command
     */
    @Override
    public List<String> getAliases() {
        return ImmutableList.of("record", "r");
    }
    
    /**
     * Returns null tab completions
     */
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
    	return null;
    }
	
    /**
     * Executes the command, starts or stops the recording
     */
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (TASRecorder.isRecording.get()) {
			TASRecorder.endRecording();
			sender.sendMessage(new TextComponentString("Recording was stopped."));
		} else {
			TASRecorder.startRecording();
			sender.sendMessage(new TextComponentString("Recording was started."));
		}
	}
	
}
