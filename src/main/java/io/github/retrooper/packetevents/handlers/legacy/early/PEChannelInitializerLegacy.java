/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

package io.github.retrooper.packetevents.handlers.legacy.early;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.handlers.legacy.PacketDecoderLegacy;
import io.github.retrooper.packetevents.handlers.legacy.PacketEncoderLegacy;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelInitializer;

import java.lang.reflect.Method;

public class PEChannelInitializerLegacy extends ChannelInitializer<Channel> {
    private final ChannelInitializer<?> oldChannelInitializer;
    private Method initChannelMethod;

    public PEChannelInitializerLegacy(ChannelInitializer<?> oldChannelInitializer) {
        this.oldChannelInitializer = oldChannelInitializer;
        load();
    }

    private void load() {
        initChannelMethod = Reflection.getMethod(oldChannelInitializer.getClass(), "initChannel", 0);
    }

    public ChannelInitializer<?> getOldChannelInitializer() {
        return oldChannelInitializer;
    }

    public static void postInitChannel(Channel channel) {
        channel.pipeline().addBefore("decoder", PacketEvents.get().decoderName, new PacketDecoderLegacy());
        channel.pipeline().addBefore("encoder", PacketEvents.get().encoderName, new PacketEncoderLegacy());
    }

    public static void postDestroyChannel(Channel channel) {
        channel.pipeline().remove(PacketEvents.get().decoderName);
        channel.pipeline().remove(PacketEvents.get().encoderName);
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        initChannelMethod.invoke(oldChannelInitializer, channel);
        postInitChannel(channel);
    }
}