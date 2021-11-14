package work.mgnet.tasrecorder.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.EntityRenderer;
import work.mgnet.tasrecorder.TASRecorder;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	
	@Inject(method = "updateCameraAndRender", at = @At(value="RETURN"), cancellable = true)
	public void redoupdateCameraAndRender(CallbackInfo ci) {
		if (TASRecorder.takeScreenshot) {
			TASRecorder.takeScreenshot = false;
			TASRecorder.takeScreenshot();
		}
	 }
	
}
