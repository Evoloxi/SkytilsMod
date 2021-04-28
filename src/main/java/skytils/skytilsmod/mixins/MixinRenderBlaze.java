/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.mixins;

import net.minecraft.client.renderer.entity.RenderBlaze;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(RenderBlaze.class)
public abstract class MixinRenderBlaze {

    private static final ResourceLocation BLANK_BLAZE_TEXTURE = new ResourceLocation("skytils", "blankblaze.png");

    @Inject(method = "getEntityTexture", at = @At("RETURN"), cancellable = true)
    private void setBlazeColor(EntityBlaze entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (Skytils.config.blazeSolver && Utils.inDungeons) {
            cir.setReturnValue(BLANK_BLAZE_TEXTURE);
        }
    }
}
