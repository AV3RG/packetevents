/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.retrooper.packetevents.packetwrappers.in.blockplace;

import io.github.retrooper.packetevents.exceptions.WrapperFieldNotFoundException;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

final class WrappedPacketInBlockPlace_1_8 extends WrappedPacket {
    private static Class<?> blockPlaceClass, blockPositionClass, blockPositionSuperClass;
    private Object blockPosObj;

    WrappedPacketInBlockPlace_1_8(final Object packet) {
        super(packet);
    }

    protected static void load() {
        blockPlaceClass = PacketTypeClasses.Client.BLOCK_PLACE;
        try {
            blockPositionClass = NMSUtils.getNMSClass("BlockPosition");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        blockPositionSuperClass = blockPositionClass.getSuperclass();
    }

    public int getX() {
        if(blockPosObj == null) {
            blockPosObj = new WrappedPacket(packet).readObject(0, blockPositionClass);
        }
        try {
            return (int)Reflection.getMethod(blockPositionSuperClass, "getX", 0).invoke(blockPosObj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getY() {
        if(blockPosObj == null) {
            blockPosObj = new WrappedPacket(packet).readObject(0, blockPositionClass);
        }
        try {
            return (int)Reflection.getMethod(blockPositionSuperClass, "getY", 0).invoke(blockPosObj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getZ() {
        if(blockPosObj == null) {
            blockPosObj = new WrappedPacket(packet).readObject(0, blockPositionClass);
        }
        try {
            return (int)Reflection.getMethod(blockPositionSuperClass, "getZ", 0).invoke(blockPosObj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ItemStack getItemStack() {
        return NMSUtils.toBukkitItemStack(readObject(0, NMSUtils.nmsItemStackClass));
    }
}
