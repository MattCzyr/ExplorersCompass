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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class ExplorersCompassScreen extends Screen {

	private Level level;
	private Player player;
	private List<ResourceLocation> allowedStructureKeys;
	private List<ResourceLocation> structureKeysMatchingSearch;
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

	public ExplorersCompassScreen(Level level, Player player, ItemStack stack, ExplorersCompassItem explorersCompass, List<ResourceLocation> allowedStructureKeys) {
		super(Component.translatable("string.explorerscompass.selectStructure"));
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.explorersCompass = explorersCompass;
		
		this.allowedStructureKeys = new ArrayList<ResourceLocation>(allowedStructureKeys);
		structureKeysMatchingSearch = new ArrayList<ResourceLocation>(this.allowedStructureKeys);
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
		if (allowedStructureKeys.size() != ExplorersCompass.allowedStructureKeys.size()) {
			removeWidget(selectionList);
			allowedStructureKeys = new ArrayList<ResourceLocation>(ExplorersCompass.allowedStructureKeys);
			structureKeysMatchingSearch = new ArrayList<ResourceLocation>(allowedStructureKeys);
			selectionList = new StructureSearchList(this, minecraft, width + 110, height - 40, 40, 45);
			addRenderableWidget(selectionList);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		guiGraphics.drawCenteredString(font, title, 65, 15, 0xffffff);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
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

	public void selectStructure(StructureSearchEntry entry) {
		boolean enable = entry != null;
		searchButton.active = enable;
		searchGroupButton.active = enable;
	}

	public void searchForStructure(ResourceLocation key) {
		PacketDistributor.SERVER.noArg().send(new SearchPacket(key, List.of(key), player.blockPosition()));
		minecraft.setScreen(null);
	}
	
	public void searchForGroup(ResourceLocation key) {
		PacketDistributor.SERVER.noArg().send(new SearchPacket(key, ExplorersCompass.typeKeysToStructureKeys.get(key), player.blockPosition()));
		minecraft.setScreen(null);
	}

	public void teleport() {
		PacketDistributor.SERVER.noArg().send(new TeleportPacket());
		minecraft.setScreen(null);
	}

	public void processSearchTerm() {
		structureKeysMatchingSearch = new ArrayList<ResourceLocation>();
		String searchTerm = searchTextField.getValue().toLowerCase();
		for (ResourceLocation key : allowedStructureKeys) {
			if (searchTerm.startsWith("@")) {
				if (StructureUtils.getPrettyStructureSource(key).toLowerCase().contains(searchTerm.substring(1))) {
					structureKeysMatchingSearch.add(key);
				}
			} else if (StructureUtils.getPrettyStructureName(key).toLowerCase().contains(searchTerm)) {
				structureKeysMatchingSearch.add(key);
			}
		}
		selectionList.refreshList();
	}

	public List<ResourceLocation> sortStructures() {
		final List<ResourceLocation> structures = structureKeysMatchingSearch;
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
			selectionList = new StructureSearchList(this, minecraft, width + 110, height - 40, 40, 45);
		}
		addRenderableWidget(selectionList);
	}

}
