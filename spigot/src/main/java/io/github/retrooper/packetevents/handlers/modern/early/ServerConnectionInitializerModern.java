/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.handlers.modern.early;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.channel.ChannelAbstract;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.reflection.ClassUtil;
import com.github.retrooper.packetevents.util.reflection.ReflectionObject;
import io.github.retrooper.packetevents.handlers.modern.PacketDecoderModern;
import io.github.retrooper.packetevents.handlers.modern.PacketEncoderModern;
import io.github.retrooper.packetevents.utils.dependencies.viaversion.ViaVersionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;


public class ServerConnectionInitializerModern {
    public static void postInitChannel(Object ch, ConnectionState connectionState) {
        Channel channel = (Channel) ch;
        //System.out.println("Pre Channel handlers: " + Arrays.toString(channel.pipeline().names().toArray(new String[0])));
        ChannelAbstract channelAbstract = PacketEvents.getAPI().getNettyManager().wrapChannel(channel);
        User user = new User(channelAbstract, new UserProfile(null, null));
        PacketEvents.getAPI().getPlayerManager().setUser(channelAbstract, user);
        channel.pipeline().addAfter("splitter", PacketEvents.DECODER_NAME, new PacketDecoderModern(user, connectionState));
        channel.pipeline().addBefore("encoder", PacketEvents.ENCODER_NAME, new PacketEncoderModern(user));
        //System.out.println("Post Channel handlers: " + Arrays.toString(channel.pipeline().names().toArray(new String[0])));
    }

    public static void postDestroyChannel(Object ch) {
        Channel channel = (Channel) ch;
        ChannelHandler viaDecoder = channel.pipeline().get("decoder");
        if (ViaVersionUtil.isAvailable() && ViaVersionUtil.getBukkitDecodeHandlerClass().isInstance(viaDecoder)) {
            ReflectionObject reflectViaDecoder = new ReflectionObject(viaDecoder);
            ByteToMessageDecoder decoder = reflectViaDecoder.readObject(0, ByteToMessageDecoder.class);
            if (decoder instanceof PacketDecoderModern) {
                PacketDecoderModern decoderModern = (PacketDecoderModern) decoder;
                //No decoders injected into our decoder
                if (decoderModern.decoders.isEmpty()) {
                    reflectViaDecoder.write(ByteToMessageDecoder.class, 0, decoderModern.mcDecoder);
                }
                //Some decoders injected into our decoder, lets do some cleaning up
                else {
                    //This decoder will now be injected into ViaVersion
                    ByteToMessageDecoder newDecoderModern = decoderModern.decoders.get(0);
                    ReflectionObject reflectNewDecoderModern = new ReflectionObject(newDecoderModern);
                    decoderModern.decoders.remove(0);

                    //Write custom decoders
                    reflectNewDecoderModern.writeList(0, decoderModern.decoders);

                    //Write mc decoder
                    reflectNewDecoderModern.write(ByteToMessageDecoder.class, 0, decoderModern.mcDecoder);

                    //Write player
                    reflectNewDecoderModern.write(Player.class, 0, decoderModern.player);

                    //Write connection state
                    reflectNewDecoderModern.write(ConnectionState.class, 0, decoderModern.connectionState);

                    //Write bypassCompression
                    reflectNewDecoderModern.write(boolean.class, 0, decoderModern.bypassCompression);

                    //Write handledCompression
                    reflectNewDecoderModern.write(boolean.class, 1, decoderModern.handledCompression);

                    //Write skipDoubleTransform
                    reflectNewDecoderModern.write(boolean.class, 2, decoderModern.skipDoubleTransform);
                }
            } else if (ClassUtil.getClassSimpleName(decoder.getClass()).equals("PacketDecoderModern")) {
                //Possibly another instance of packetevents has already injected into ViaVersion.
                //Let us try to remove our own decoder without breaking the other instance.
                ReflectionObject reflectDecoder = new ReflectionObject(decoder);
                List<Object> decoders = reflectDecoder.readList(0);
                int targetIndex = -1;
                for (int i = 0; i < decoders.size(); i++) {
                    if (decoders.get(i) instanceof PacketDecoderModern) {
                        targetIndex = i;
                    }
                }
                if (targetIndex != -1) {
                    decoders.remove(targetIndex);
                    reflectDecoder.writeList(0, decoders);
                }
            } else {
                //ViaVersion is present, we didn't inject into ViaVersion yet, because we haven't needed to.
                channel.pipeline().remove(PacketEvents.DECODER_NAME);
            }
        } else {
            channel.pipeline().remove(PacketEvents.DECODER_NAME);
        }
        channel.pipeline().remove(PacketEvents.ENCODER_NAME);
    }
}
