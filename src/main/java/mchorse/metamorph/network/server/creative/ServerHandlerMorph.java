package mchorse.metamorph.network.server.creative;

import mchorse.mclib.network.ServerMessageHandler;
import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.network.common.creative.PacketMorph;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerMorph extends ServerMessageHandler<PacketMorph>
{
    @Override
    public void run(EntityPlayerMP player, PacketMorph message)
    {
        if (player.isCreative())
        {
            MorphAPI.morph(player, message.morph, false);
        }
    }
}