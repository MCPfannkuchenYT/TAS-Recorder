package work.mgnet.tasrecorder.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import work.mgnet.tasrecorder.TASRecorder;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	
	@Inject(method = "runTick", at = @At(value="RETURN"), cancellable = true)
	public void redorunTick(CallbackInfo ci) {
		if (TASRecorder.isRecording.get()) TASRecorder.takeScreenshot = true;
	 }
	
}
