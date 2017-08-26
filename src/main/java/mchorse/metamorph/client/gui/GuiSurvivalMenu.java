package mchorse.metamorph.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import mchorse.metamorph.ClientProxy;
import mchorse.metamorph.client.gui.elements.GuiSurvivalMorphs;
import mchorse.metamorph.client.gui.elements.GuiSurvivalMorphs.MorphCell;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * Survival morph menu GUI
 * 
 * This is menu which allows users to manage their acquired morphs.
 */
public class GuiSurvivalMenu extends GuiScreen
{
    private GuiButton close;
    private GuiButton favorite;
    private GuiButton remove;
    private GuiButton onlyFavorites;
    private GuiButton morph;

    private GuiSurvivalMorphs morphs;

    /* Initiate GUI */

    public GuiSurvivalMenu(GuiSurvivalMorphs morphs)
    {
        this.morphs = morphs;
        this.morphs.inGUI = true;
    }

    @Override
    public void initGui()
    {
        int x = width - 20;
        int y = 10;

        remove = new GuiButton(0, 20, this.height - 30, 60, 20, I18n.format("metamorph.gui.remove"));
        favorite = new GuiButton(1, this.width - 145, this.height - 30, 60, 20, "");
        close = new GuiButton(2, x - 60, y, 60, 20, I18n.format("metamorph.gui.close"));
        onlyFavorites = new GuiButton(3, x - 155, y, 90, 20, "");
        morph = new GuiButton(4, this.width - 80, this.height - 30, 60, 20, I18n.format("metamorph.gui.morph"));

        this.buttonList.add(remove);
        this.buttonList.add(favorite);
        this.buttonList.add(close);
        this.buttonList.add(onlyFavorites);
        this.buttonList.add(morph);

        this.updateFavorites();
        this.updateButtons();
    }

    private void updateFavorites()
    {
        this.onlyFavorites.displayString = this.morphs.showFavorites ? I18n.format("metamorph.gui.all_morphs") : I18n.format("metamorph.gui.only_favorites");
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.morphs.clickMorph(mouseX, mouseY, this.width, this.height);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            this.exit();
        }

        super.keyTyped(typedChar, keyCode);

        if (ClientProxy.keys.keyPrevVarMorph.getKeyCode() == keyCode)
        {
            this.morphs.down();
            this.updateButtons();
        }
        else if (ClientProxy.keys.keyNextVarMorph.getKeyCode() == keyCode)
        {
            this.morphs.up();
            this.updateButtons();
        }
        else if (ClientProxy.keys.keyPrevMorph.getKeyCode() == keyCode)
        {
            this.morphs.advance(-1);
            this.updateButtons();
        }
        else if (ClientProxy.keys.keyNextMorph.getKeyCode() == keyCode)
        {
            this.morphs.advance(1);
            this.updateButtons();
        }
        else if (ClientProxy.keys.keyDemorph.getKeyCode() == keyCode)
        {
            this.morphs.skip(-1);
            this.updateButtons();
        }
        else if (ClientProxy.keys.keySelectMorph.getKeyCode() == keyCode)
        {
            this.morphs.selectCurrent();
            this.exit();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (this.morphs.index != -1)
        {
            if (button.id == 0)
            {
                this.morphs.remove();
                this.updateButtons();
            }
            else if (button.id == 1)
            {
                this.morphs.favorite(this.morphs.morphs.get(this.morphs.index).current());
                this.updateButtons();
            }
        }

        if (button.id == 2)
        {
            this.exit();
        }
        else if (button.id == 3)
        {
            this.morphs.toggleFavorites();
            this.updateFavorites();
        }
        else if (button.id == 4)
        {
            this.morphs.selectCurrent();
            this.exit();
        }
    }

    /**
     * Exit from this GUI 
     */
    private void exit()
    {
        this.morphs.exitGUI();
        this.mc.displayGuiScreen(null);
    }

    private void updateButtons()
    {
        int index = this.morphs.index;

        this.favorite.enabled = index >= 0;
        this.remove.enabled = index >= 0;

        if (this.favorite.enabled)
        {
            MorphCell cell = this.morphs.getCurrent();

            if (cell != null)
            {
                this.favorite.displayString = cell.favorite ? I18n.format("metamorph.gui.unfavorite") : I18n.format("metamorph.gui.favorite");
            }
            else
            {
                this.favorite.enabled = false;
            }
        }
    }

    /* Drawing code */

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        /* Background and stuff */
        this.drawDefaultBackground();

        Gui.drawRect(0, 0, width, 40, 0x88000000);
        this.drawString(fontRenderer, I18n.format("metamorph.gui.survival_title"), 20, 16, 0xffffff);

        this.morphs.render(this.width, this.height);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}