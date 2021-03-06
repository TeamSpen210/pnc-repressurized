package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerPlasticMixer;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiPlasticMixer extends GuiPneumaticContainerBase<TileEntityPlasticMixer> {
    private GuiButtonSpecial[] buttons;
    private GuiCheckBox lockSelection;

    public GuiPlasticMixer(InventoryPlayer player, TileEntityPlasticMixer te) {
        super(new ContainerPlasticMixer(player, te), te, Textures.GUI_PLASTIC_MIXER);
    }

    @Override
    public void initGui() {
        super.initGui();

        addWidget(new WidgetTemperature(0, guiLeft + 55, guiTop + 25, 295, 500, te.getLogic(0)));
        addWidget(new WidgetTemperature(1, guiLeft + 82, guiTop + 25, 295, 500, te.getLogic(1), PneumaticValues.PLASTIC_MIXER_MELTING_TEMP));
        addWidget(new WidgetTank(3, guiLeft + 152, guiTop + 14, te.getTank()));

        GuiAnimatedStat stat = addAnimatedStat("gui.tab.plasticMixer.plasticSelection", new ItemStack(Itemss.PLASTIC, 1, 1), 0xFF005500, false);
        List<String> text = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            text.add("                      ");
        }
        stat.setTextWithoutCuttingString(text);

        buttons = new GuiButtonSpecial[16];
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                int index = y * 4 + x;
                ItemStack plastic = new ItemStack(Itemss.PLASTIC, 1, index);
                buttons[index] = new GuiButtonSpecial(index + 1, x * 21 + 4, y * 21 + 30, 20, 20, "").setRenderStacks(plastic).setTooltipText(plastic.getDisplayName());
                stat.addWidget(buttons[index]);
            }
        }
        stat.addWidget(lockSelection = new GuiCheckBox(17, 4, 18, 0xFF000000, "gui.plasticMixer.lockSelection").setChecked(te.lockSelection).setTooltip(I18n.format("gui.plasticMixer.lockSelection.tooltip")));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].enabled = te.selectedPlastic != i;
        }
        lockSelection.checked = te.lockSelection;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        fontRenderer.drawString("Upgr.", 15, 19, 4210752);
        fontRenderer.drawString("Hull", 56, 16, 4210752);
        fontRenderer.drawString("Item", 88, 16, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        for (int i = 0; i < 3; i++) {
            double percentage = (double) te.dyeBuffers[i] / TileEntityPlasticMixer.DYE_BUFFER_MAX;
            drawVerticalLine(guiLeft + 123, guiTop + 37 + i * 18, guiTop + 37 - MathHelper.clamp((int) (percentage * 16), 1, 15) + i * 18, 0xFF000000 | 0xFF0000 >> 8 * i);
        }
    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -1);
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        ItemStack stack = te.getPrimaryInventory().getStackInSlot(0);
        if (te.getTank().getFluidAmount() == 0) {
            if (stack.isEmpty()) {
                curInfo.add("gui.tab.problems.plasticMixer.noPlastic");
            } else {
                curInfo.add("gui.tab.problems.notEnoughHeat");
            }
        } else {
            if (!stack.isEmpty()) {
                if (te.getLogic(1).getTemperature() >= PneumaticValues.PLASTIC_MIXER_MELTING_TEMP && te.getTank().getCapacity() - te.getTank().getFluidAmount() < 1000) {
                    curInfo.add("gui.tab.problems.plasticMixer.plasticLiquidOverflow");
                }
            }
        }
        if (te.getPrimaryInventory().getStackInSlot(TileEntityPlasticMixer.INV_DYE_RED).isEmpty()) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.DYE, 1, 1).getDisplayName()));
        }
        if (te.getPrimaryInventory().getStackInSlot(TileEntityPlasticMixer.INV_DYE_GREEN).isEmpty()) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.DYE, 1, 2).getDisplayName()));
        }
        if (te.getPrimaryInventory().getStackInSlot(TileEntityPlasticMixer.INV_DYE_BLUE).isEmpty()  ) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noDye", new ItemStack(Items.DYE, 1, 4).getDisplayName()));
        }
    }

    @Override
    protected void addInformation(List<String> curInfo) {
        if (curInfo.size() == 0) {
            curInfo.add(I18n.format("gui.tab.problems.plasticMixer.noProblems"));
        }
    }
}
