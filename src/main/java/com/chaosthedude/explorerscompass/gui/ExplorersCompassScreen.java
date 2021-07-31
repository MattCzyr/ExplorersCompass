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
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExplorersCompassScreen extends Screen {

	private Level level;
	private Player player;
	private List<StructureFeature<?>> allowedStructures;
	private List<StructureFeature<?>> structuresMatchingSearch;
	private ItemStack stack;
	private ExplorersCompassItem explorersCompass;
	private Button startSearchButton;
	private Button sortByButton;
	private Button teleportButton;
	private Button cancelButton;
	private TransparentTextField searchTextField;
	private StructureSearchList selectionList;
	private ISorting sortingCategory;

	public ExplorersCompassScreen(Level level, Player player, ItemStack stack, ExplorersCompassItem explorersCompass, List<StructureFeature<?>> allowedStructures) {
		super(new TranslatableComponent("string.explorerscompass.selectStructure"));
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.explorersCompass = explorersCompass;
		this.allowedStructures = allowedStructures;

		structuresMatchingSearch = new ArrayList<StructureFeature<?>>(allowedStructures);
		sortingCategory = new NameSorting();
	}

	@Override
	public boolean mouseScrolled(double scroll1, double scroll2, double scroll3) {
		return selectionList.mouseScrolled(scroll1, scroll2, scroll3);
	}

	@Override
	protected void init() {
		minecraft.keyboardHandler.setSendRepeatsToGui(true);
		setupWidgets();
		if (selectionList == null) {
			selectionList = new StructureSearchList(this, minecraft, width + 110, height, 40, height, 45);
		}
		addRenderableWidget(selectionList);
	}

	@Override
	public void tick() {
		searchTextField.tick();
		teleportButton.active = explorersCompass.getState(stack) == CompassState.FOUND;
		
		// Check if the allowed structure list has synced
		if (allowedStructures.size() != ExplorersCompass.allowedStructures.size()) {
			removeWidget(selectionList);
			allowedStructures = ExplorersCompass.allowedStructures;
			structuresMatchingSearch = new ArrayList<StructureFeature<?>>(allowedStructures);
			selectionList = new StructureSearchList(this, minecraft, width + 110, height, 40, height, 45);
			addRenderableWidget(selectionList);
		}
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
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
		minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	public void selectStructure(StructureSearchEntry entry) {
		boolean enable = entry != null;
		startSearchButton.active = enable;
	}

	public void searchForStructure(StructureFeature<?> structure) {
		ExplorersCompass.network.sendToServer(new CompassSearchPacket(StructureUtils.getKeyForStructure(structure), player.blockPosition()));
		minecraft.setScreen(null);
	}

	public void teleport() {
		ExplorersCompass.network.sendToServer(new TeleportPacket());
		minecraft.setScreen(null);
	}

	public void processSearchTerm() {
		structuresMatchingSearch = new ArrayList<StructureFeature<?>>();
		for (StructureFeature<?> structure : allowedStructures) {
			if (StructureUtils.getStructureName(structure).toLowerCase().contains(searchTextField.getValue().toLowerCase())) {
				structuresMatchingSearch.add(structure);
			}
		}
		selectionList.refreshList();
	}

	public List<StructureFeature<?>> sortStructures() {
		final List<StructureFeature<?>> structures = structuresMatchingSearch;
		Collections.sort(structures, new NameSorting());
		Collections.sort(structures, sortingCategory);
		return structures;
	}

	private void setupWidgets() {
		clearWidgets();
		startSearchButton = addRenderableWidget(new TransparentButton(10, 40, 110, 20, new TranslatableComponent("string.explorerscompass.startSearch"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelected().searchForBiome();
			}
		}));
		sortByButton = addRenderableWidget(new TransparentButton(10, 65, 110, 20, new TranslatableComponent("string.explorerscompass.sortBy").append(new TextComponent(": " + sortingCategory.getLocalizedName())), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(new TranslatableComponent("string.explorerscompass.sortBy").append(new TextComponent(": " + sortingCategory.getLocalizedName())));
			selectionList.refreshList();
		}));
		cancelButton = addRenderableWidget(new TransparentButton(10, height - 30, 110, 20, new TranslatableComponent("gui.cancel"), (onPress) -> {
			minecraft.setScreen(null);
		}));
		teleportButton = addRenderableWidget(new TransparentButton(width - 120, 10, 110, 20, new TranslatableComponent("string.explorerscompass.teleport"), (onPress) -> {
			teleport();
		}));

		startSearchButton.active = false;

		teleportButton.visible = ExplorersCompass.canTeleport;
		
		searchTextField = new TransparentTextField(font, width / 2 - 82, 10, 140, 20, new TranslatableComponent("string.explorerscompass.search"));
		addRenderableWidget(searchTextField);
	}

}
