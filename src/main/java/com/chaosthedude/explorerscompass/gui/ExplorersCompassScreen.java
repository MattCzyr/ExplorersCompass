package com.chaosthedude.explorerscompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.CompassSearchForNextPacket;
import com.chaosthedude.explorerscompass.network.CompassSearchPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.chaosthedude.explorerscompass.sorting.ISorting;
import com.chaosthedude.explorerscompass.sorting.NameSorting;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExplorersCompassScreen extends Screen {

	private Level level;
	private Player player;
	private List<ResourceLocation> allowedStructureKeys;
	private List<ResourceLocation> structureKeysMatchingSearch;
	private ItemStack stack;
	private ExplorersCompassItem explorersCompass;
	private ResourceLocation foundStructureKey;
	private Button searchButton;
	private Button searchForNextButton;
	private Button searchForGroupButton;
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

		if (explorersCompass.getState(stack) == CompassState.FOUND) {
			foundStructureKey = explorersCompass.getStructureKey(stack);
		}
	}

	@Override
	public boolean mouseScrolled(double scroll1, double scroll2, double scroll3) {
		return selectionList.mouseScrolled(scroll1, scroll2, scroll3);
	}

	@Override
	protected void init() {
		setupWidgets();
	}

	@Override
	public void tick() {
		searchTextField.tick();
		teleportButton.active = selectionList.hasSelection() && selectionList.getSelected().getStructureKey() != null && selectionList.getSelected().getStructureKey().equals(foundStructureKey);
		searchForNextButton.active = teleportButton.active;
		searchButton.active = searchForGroupButton.active = selectionList.hasSelection();

		// Check if sync packet has been received
		if (ExplorersCompass.synced) {
			removeWidget(selectionList);
			allowedStructureKeys = new ArrayList<ResourceLocation>(ExplorersCompass.allowedStructureKeys);
			structureKeysMatchingSearch = new ArrayList<ResourceLocation>(allowedStructureKeys);
			selectionList = new StructureSearchList(this, minecraft, foundStructureKey, 130, 40, width - 140, height - 50, 50);
			addRenderableWidget(selectionList);

			teleportButton.visible = ExplorersCompass.canTeleport;
			searchForNextButton.visible = ExplorersCompass.maxNextSearches > 0;
			if (searchForNextButton.visible) {
				sortByButton.setPosition(10, 125);
			} else {
				sortByButton.setPosition(10, 100);
			}

			ExplorersCompass.synced = false;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(font, title, 65, 15, 0xffffff);
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

	public void searchForStructure(ResourceLocation key) {
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		ExplorersCompass.network.sendToServer(new CompassSearchPacket(key, false));
		minecraft.setScreen(null);
	}

	public void searchForGroup(ResourceLocation key) {
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		ExplorersCompass.network.sendToServer(new CompassSearchPacket(key, true));
		minecraft.setScreen(null);
	}

	public void searchForNext() {
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		ExplorersCompass.network.sendToServer(new CompassSearchForNextPacket());
		minecraft.setScreen(null);
	}

	public void teleport() {
		ExplorersCompass.network.sendToServer(new TeleportPacket());
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
		selectionList.refreshList(true);
	}

	public Player getPlayer() {
		return player;
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
				searchForStructure(selectionList.getSelected().getStructureKey());
			}
		}));
		searchButton.active = false;

		searchForGroupButton = addRenderableWidget(new TransparentButton(10, 65, 110, 20, Component.translatable("string.explorerscompass.searchForGroup"), (onPress) -> {
			if (selectionList.hasSelection()) {
				searchForGroup(selectionList.getSelected().getGroupKey());
			}
		}));
		searchForGroupButton.active = false;

		searchForNextButton = addRenderableWidget(new TransparentButton(10, 90, 110, 20, Component.translatable("string.explorerscompass.searchForNext"), (onPress) -> {
			searchForNext();
		}));
		searchForNextButton.visible = ExplorersCompass.maxNextSearches > 0;
		searchForNextButton.active = false;

		sortByButton = addRenderableWidget(new TransparentButton(10, 125, 110, 20, Component.translatable("string.explorerscompass.sortBy").append(Component.literal(": " + sortingCategory.getLocalizedName())), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(Component.translatable("string.explorerscompass.sortBy").append(Component.literal(": " + sortingCategory.getLocalizedName())));
			selectionList.refreshList(true);
		}));
		if (!searchForNextButton.visible) {
			sortByButton.setPosition(10, 100);
		}

		teleportButton = addRenderableWidget(new TransparentButton(width - 120, 10, 110, 20, Component.translatable("string.explorerscompass.teleport"), (onPress) -> {
			teleport();
		}));
		teleportButton.visible = ExplorersCompass.canTeleport;
		teleportButton.active = false;

		cancelButton = addRenderableWidget(new TransparentButton(10, height - 30, 110, 20, Component.translatable("gui.cancel"), (onPress) -> {
			minecraft.setScreen(null);
		}));

		searchTextField = new TransparentTextField(font, 130, 10, 140, 20, Component.translatable("string.explorerscompass.search"));
		addRenderableWidget(searchTextField);

		selectionList = new StructureSearchList(this, minecraft, foundStructureKey, 130, 40, width - 140, height - 50, 50);
		addRenderableWidget(selectionList);
	}

}
