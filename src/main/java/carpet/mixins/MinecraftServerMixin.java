package carpet.mixins;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.MinecraftServer;
import carpet.CarpetServer;
import carpet.helpers.TickSpeed;

import javax.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin
{
    @Final
    @Shadow
    private static Logger LOGGER;
    @Shadow
    private long serverTime;
    @Shadow
    private boolean serverRunning;
    @Final
    @Shadow
    private ServerStatusResponse statusResponse;
    @Shadow
    public void applyServerIconToResponse(ServerStatusResponse response){}
    @Shadow
    public void tick(BooleanSupplier p_71217_1_){}
    @Shadow
    public abstract boolean init();
    @Shadow
    private boolean serverIsRunning;
    @Shadow
    private long timeOfLastWarning;
    @Shadow
    protected abstract boolean isAheadOfTime();
    @Shadow
    public abstract void finalTick(@Nullable CrashReport report);
    @Shadow
    public abstract CrashReport addServerInfoToCrashReport(CrashReport report);
    @Shadow
    public abstract File getDataDirectory();
    @Shadow
    private boolean serverStopped;
    @Shadow
    public abstract void stopServer();
    @Shadow
    public abstract void systemExitNow();

    @Inject(method = "<init>", at = @At(value = "RETURN")
    )
    private void onSetupServer(CallbackInfo ci)
    {
        CarpetServer.init((MinecraftServer)(Object) this);
    }


    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;updateTimeLightAndEntities(Ljava/util/function/BooleanSupplier;)V",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )
    )
    private void onTick(BooleanSupplier booleanSupplier_1, CallbackInfo ci)
    {
        CarpetServer.tick((MinecraftServer) (Object) this);
    }

    /**
     * @author carpet
     */
    @Overwrite
    public void run()
    {
        try
        {
            if (this.init())
            {
                this.serverTime = Util.milliTime();
                // [CM] End -- CustomMOTD
                this.statusResponse.setVersion(new ServerStatusResponse.Version("1.13.2", 404));
                this.applyServerIconToResponse(this.statusResponse);

                while (this.serverRunning)
                {

                    //todo check if this check is necessary
                    if (TickSpeed.time_warp_start_time != 0)
                    {
                        if (TickSpeed.continueWarp())
                        {
                            this.tick( ()->true );
                            this.serverTime = Util.milliTime();
                            this.serverIsRunning = true;
                        }
                        continue;
                    }
                    long i = Util.milliTime() - this.serverTime;

                    if (i > 2000L && this.serverTime - this.timeOfLastWarning >= 15000L)
                    {
                        long j = i / TickSpeed.mspt;//50L;
                        LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
                        this.serverTime += j * TickSpeed.mspt;//50L;
                        this.timeOfLastWarning = this.serverTime;
                    }

                    this.tick(this::isAheadOfTime);
                    //[CM] NOTE: serverTime doesn't indicate current time, but server wannabe-time
                    // only corrected if it falls behind more than 2000 and manages to catch the warning
                    // which releases accrued time it falls behind, not 1 tick, but MULTIPLE ticks
                    this.serverTime += TickSpeed.mspt;//50L;

                    while (this.isAheadOfTime())
                    {
                        Thread.sleep(1L);
                    }

                    this.serverIsRunning = true;
                }
            }
            else
            {
                this.finalTick((CrashReport)null);
            }
        }
        catch (Throwable throwable1)
        {
            LOGGER.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport;

            if (throwable1 instanceof ReportedException)
            {
                crashreport = this.addServerInfoToCrashReport(((ReportedException)throwable1).getCrashReport());
            }
            else
            {
                crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
            }

            File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.saveToFile(file1))
            {
                LOGGER.error("This crash report has been saved to: {}", (Object)file1.getAbsolutePath());
            }
            else
            {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.finalTick(crashreport);
        }
        finally
        {
            try
            {
                this.serverStopped = true;
                this.stopServer();
            }
            catch (Throwable throwable)
            {
                LOGGER.error("Exception stopping the server", throwable);
            }
            finally
            {
                this.systemExitNow();
            }
        }
    }


}