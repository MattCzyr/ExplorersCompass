package com.chaosthedude.explorerscompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.SearchPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.chaosthedude.explorerscompass.sorting.ISorting;
import com.chaosthedude.explorerscompass.sorting.NameSorting;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExplorersCompassScreen extends Screen {

	private Level level;
	private Player player;
	private List<Identifier> allowedStructures;
	private List<Identifier> structuresMatchingSearch;
	private ItemStack stack;
	private ExplorersCompassItem explorersCompass;
	private Button searchButton;
	private Button searchGroupButton;
	private Button sortByButton;
	private Button teleportButton;
	private Button cancelButton;
	private TransparentTextField searchTextField;
	private StructureSearchList selectionList;
	private ISorting sortingCategory;

	public ExplorersCompassScreen(Level level, Player player, ItemStack stack, ExplorersCompassItem explorersCompass, List<Identifier> allowedStructures) {
		super(Component.translatable("string.explorerscompass.selectStructure"));
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.explorersCompass = explorersCompass;
		
		this.allowedStructures = new ArrayList<Identifier>(allowedStructures);
		structuresMatchingSearch = new ArrayList<Identifier>(this.allowedStructures);
		sortingCategory = new NameSorting();
	}

	@Override
	public boolean mouseScrolled(double par1, double par2, double par3, double par4) {
		return selectionList.mouseScrolled(par1, par2, par3, par4);
	}

	@Override
	protected void init() {
		setupWidgets();
	}

	@Override
	public void tick() {
		teleportButton.active = explorersCompass.getState(stack) == CompassState.FOUND;
		
		// Check if the allowed structure list has synced
		if (allowedStructures.size() != ExplorersCompass.allowedStructures.size()) {
			removeWidget(selectionList);
			allowedStructures = new ArrayList<Identifier>(ExplorersCompass.allowedStructures);
			structuresMatchingSearch = new ArrayList<Identifier>(allowedStructures);
			selectionList = new StructureSearchList(this, minecraft, player, width + 110, height - 50, 40, 50);
			addRenderableWidget(selectionList);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(font, title, 65, 15, 0xffffffff);
	}
	
	@Override
	public boolean keyPressed(KeyEvent event) {
		boolean ret = super.keyPressed(event);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	public void selectStructure(StructureSearchEntry entry) {
		boolean enable = entry != null;
		searchButton.active = enable;
		searchGroupButton.active = enable;
	}

	public void searchForStructure(Identifier structureID) {
		ClientPlayNetworking.send(new SearchPacket(structureID, List.of(structureID), player.blockPosition()));
		minecraft.setScreen(null);
	}
	
	public void searchForStructureGroup(Identifier structureID) {
		ClientPlayNetworking.send(new SearchPacket(structureID, ExplorersCompass.groupIdsToStructureIds.get(structureID), player.blockPosition()));
		minecraft.setScreen(null);
	}

	public void teleport() {
		ClientPlayNetworking.send(new TeleportPacket());
		minecraft.setScreen(null);
	}

	public void processSearchTerm() {
		structuresMatchingSearch = new ArrayList<Identifier>();
		String searchTerm = searchTextField.getValue().toLowerCase();
		for (Identifier structureId : allowedStructures) {
			if (searchTerm.startsWith("@")) {
				if (StructureUtils.getStructureSource(structureId).toLowerCase().contains(searchTerm.substring(1))) {
					structuresMatchingSearch.add(structureId);
				}
			} else if (StructureUtils.getStructureName(structureId).toLowerCase().contains(searchTerm)) {
				structuresMatchingSearch.add(structureId);
			}
		}
		selectionList.refreshList();
	}

	public List<Identifier> sortStructures() {
		final List<Identifier> structures = structuresMatchingSearch;
		Collections.sort(structures, new NameSorting());
		Collections.sort(structures, sortingCategory);
		return structures;
	}

	private void setupWidgets() {
		clearWidgets();
		searchButton = addRenderableWidget(new TransparentButton(10, 40, 110, 20, Component.translatable("string.explorerscompass.search"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelected().searchForStructure();
			}
		}));
		searchGroupButton = addRenderableWidget(new TransparentButton(10, 65, 110, 20, Component.translatable("string.explorerscompass.searchForGroup"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelected().searchForGroup();
			}
		}));
		sortByButton = addRenderableWidget(new TransparentButton(10, 90, 110, 20, Component.translatable("string.explorerscompass.sortBy").append(Component.literal(": " + sortingCategory.getLocalizedName())), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(Component.translatable("string.explorerscompass.sortBy").append(Component.literal(": " + sortingCategory.getLocalizedName())));
			selectionList.refreshList();
		}));
		cancelButton = addRenderableWidget(new TransparentButton(10, height - 30, 110, 20, Component.translatable("gui.cancel"), (onPress) -> {
			minecraft.setScreen(null);
		}));
		teleportButton = addRenderableWidget(new TransparentButton(width - 120, 10, 110, 20, Component.translatable("string.explorerscompass.teleport"), (onPress) -> {
			teleport();
		}));

		searchButton.active = false;
		searchGroupButton.active = false;

		teleportButton.visible = ExplorersCompass.canTeleport;
		
		searchTextField = new TransparentTextField(font, width / 2 - 82, 10, 140, 20, Component.translatable("string.explorerscompass.search"));
		addRenderableWidget(searchTextField);
		
		if (selectionList == null) {
			selectionList = new StructureSearchList(this, minecraft, player, width + 110, height - 50, 40, 50);
		}
		addRenderableWidget(selectionList);
	}

}
