package com.chaosthedude.explorerscompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.CompassSearchPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.chaosthedude.explorerscompass.sorting.ISorting;
import com.chaosthedude.explorerscompass.sorting.NameSorting;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExplorersCompassScreen extends Screen {

	private World world;
	private PlayerEntity player;
	private List<Structure<?>> allowedStructures;
	private List<Structure<?>> structuresMatchingSearch;
	private ItemStack stack;
	private ExplorersCompassItem explorersCompass;
	private Button startSearchButton;
	private Button sortByButton;
	private Button teleportButton;
	private Button cancelButton;
	private TransparentTextField searchTextField;
	private StructureSearchList selectionList;
	private ISorting sortingCategory;

	public ExplorersCompassScreen(World world, PlayerEntity player, ItemStack stack, ExplorersCompassItem explorersCompass, List<Structure<?>> allowedStructures) {
		super(new TranslationTextComponent("string.explorerscompass.selectStructure"));
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.explorersCompass = explorersCompass;
		this.allowedStructures = allowedStructures;

		structuresMatchingSearch = new ArrayList<Structure<?>>(allowedStructures);
		sortingCategory = new NameSorting();
	}

	@Override
	public boolean mouseScrolled(double scroll1, double scroll2, double scroll3) {
		return selectionList.mouseScrolled(scroll1, scroll2, scroll3);
	}

	@Override
	protected void init() {
		minecraft.keyboardListener.enableRepeatEvents(true);
		setupButtons();
		setupTextFields();
		if (selectionList == null) {
			selectionList = new StructureSearchList(this, minecraft, width + 110, height, 40, height, 45);
		}
		children.add(selectionList);
	}

	@Override
	public void tick() {
		searchTextField.tick();
		teleportButton.active = explorersCompass.getState(stack) == CompassState.FOUND;
		
		// Check if the allowed structure list has synced
		if (allowedStructures.size() != ExplorersCompass.allowedStructures.size()) {
			children.remove(selectionList);
			allowedStructures = ExplorersCompass.allowedStructures;
			structuresMatchingSearch = new ArrayList<Structure<?>>(allowedStructures);
			selectionList = new StructureSearchList(this, minecraft, width + 110, height, 40, height, 45);
			children.add(selectionList);
		}
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack);
		selectionList.render(stack, mouseX, mouseY, partialTicks);
		searchTextField.render(stack, mouseX, mouseY, partialTicks);
		drawCenteredString(stack, font, title, 65, 15, 0xffffff);
		super.render(stack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean keyPressed(int par1, int par2, int par3) {
		boolean ret = super.keyPressed(par1, par2, par3);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		boolean ret = super.charTyped(typedChar, keyCode);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	@Override
	public void onClose() {
		super.onClose();
		minecraft.keyboardListener.enableRepeatEvents(false);
	}

	public void selectStructure(StructureSearchEntry entry) {
		boolean enable = entry != null;
		startSearchButton.active = enable;
	}

	public void searchForStructure(Structure<?> structure) {
		ExplorersCompass.network.sendToServer(new CompassSearchPacket(StructureUtils.getKeyForStructure(structure), player.getPosition()));
		minecraft.displayGuiScreen(null);
	}

	public void teleport() {
		ExplorersCompass.network.sendToServer(new TeleportPacket());
		minecraft.displayGuiScreen(null);
	}

	public void processSearchTerm() {
		structuresMatchingSearch = new ArrayList<Structure<?>>();
		for (Structure<?> structure : allowedStructures) {
			if (StructureUtils.getStructureName(structure).toLowerCase().contains(searchTextField.getText().toLowerCase())) {
				structuresMatchingSearch.add(structure);
			}
		}
		selectionList.refreshList();
	}

	public List<Structure<?>> sortStructures() {
		final List<Structure<?>> structures = structuresMatchingSearch;
		Collections.sort(structures, new NameSorting());
		Collections.sort(structures, sortingCategory);
		return structures;
	}

	private void setupButtons() {
		buttons.clear();
		startSearchButton = addButton(new TransparentButton(10, 40, 110, 20, new TranslationTextComponent("string.explorerscompass.startSearch"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelected().searchForBiome();
			}
		}));
		sortByButton = addButton(new TransparentButton(10, 65, 110, 20, new TranslationTextComponent("string.explorerscompass.sortBy").append(new StringTextComponent(": " + sortingCategory.getLocalizedName())), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(new TranslationTextComponent("string.explorerscompass.sortBy").append(new StringTextComponent(": " + sortingCategory.getLocalizedName())));
			selectionList.refreshList();
		}));
		cancelButton = addButton(new TransparentButton(10, height - 30, 110, 20, new TranslationTextComponent("gui.cancel"), (onPress) -> {
			minecraft.displayGuiScreen(null);
		}));
		teleportButton = addButton(new TransparentButton(width - 120, 10, 110, 20, new TranslationTextComponent("string.explorerscompass.teleport"), (onPress) -> {
			teleport();
		}));

		startSearchButton.active = false;

		teleportButton.visible = ExplorersCompass.canTeleport;
	}

	private void setupTextFields() {
		searchTextField = new TransparentTextField(font, width / 2 - 82, 10, 140, 20, new TranslationTextComponent("string.explorerscompass.search"));
		children.add(searchTextField);
	}

}
